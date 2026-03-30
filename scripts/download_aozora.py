#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
青空文库文章下载脚本
下载预设的 60 篇文章到 assets/aozora/ 目录
"""

import os
import sys
import time
import requests
from bs4 import BeautifulSoup

# 设置 Windows 控制台编码
if sys.platform == 'win32':
    import io
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    sys.stderr = io.TextIOWrapper(sys.stderr.buffer, encoding='utf-8')

# 文章列表（作者ID/作品ID）
ARTICLES = [
    # 夏目漱石 (10篇)
    ("000148", "773", "こころ", "夏目漱石"),
    ("000148", "752", "坊っちゃん", "夏目漱石"),
    ("000148", "789", "吾輩は猫である", "夏目漱石"),
    ("000148", "794", "三四郎", "夏目漱石"),
    ("000148", "799", "それから", "夏目漱石"),
    ("000148", "776", "草枕", "夏目漱石"),
    ("000148", "751", "夢十夜", "夏目漱石"),
    ("000148", "762", "文鳥", "夏目漱石"),
    ("000148", "800", "門", "夏目漱石"),
    ("000148", "795", "行人", "夏目漱石"),

    # 宮沢賢治 (8篇)
    ("000081", "43737", "銀河鉄道の夜", "宮沢賢治"),
    ("000081", "43754", "注文の多い料理店", "宮沢賢治"),
    ("000081", "43752", "風の又三郎", "宮沢賢治"),
    ("000081", "470", "セロ弾きのゴーシュ", "宮沢賢治"),
    ("000081", "456", "よだかの星", "宮沢賢治"),
    ("000081", "45630", "雨ニモマケズ", "宮沢賢治"),
    ("000081", "473", "オツベルと象", "宮沢賢治"),
    ("000081", "464", "やまなし", "宮沢賢治"),

    # 芥川龍之介 (15篇短篇)
    ("000879", "127", "羅生門", "芥川龍之介"),
    ("000879", "92", "蜘蛛の糸", "芥川龍之介"),
    ("000879", "94", "鼻", "芥川龍之介"),
    ("000879", "166", "地獄変", "芥川龍之介"),
    ("000879", "69", "河童", "芥川龍之介"),
    ("000879", "179", "藪の中", "芥川龍之介"),
    ("000879", "42", "トロッコ", "芥川龍之介"),
    ("000879", "158", "杜子春", "芥川龍之介"),
    ("000879", "43", "舞踏会", "芥川龍之介"),
    ("000879", "44", "或る日の大石内蔵助", "芥川龍之介"),
    ("000879", "45", "芋粥", "芥川龍之介"),
    ("000879", "46", "手巾", "芥川龍之介"),
    ("000879", "47", "秋", "芥川龍之介"),
    ("000879", "48", "奉教人の死", "芥川龍之介"),
    ("000879", "49", "煙草と悪魔", "芥川龍之介"),

    # 太宰治 (6篇)
    ("000035", "301", "人間失格", "太宰治"),
    ("000035", "1567", "走れメロス", "太宰治"),
    ("000035", "1565", "斜陽", "太宰治"),
    ("000035", "2282", "津軽", "太宰治"),
    ("000035", "1588", "ヴィヨンの妻", "太宰治"),
    ("000035", "1566", "お伽草紙", "太宰治"),

    # 森鴎外 (4篇)
    ("000129", "695", "舞姫", "森鴎外"),
    ("000129", "2768", "高瀬舟", "森鴎外"),
    ("000129", "2767", "山椒大夫", "森鴎外"),
    ("000129", "696", "阿部一族", "森鴎外"),

    # 梶井基次郎 (2篇)
    ("000074", "424", "檸檬", "梶井基次郎"),
    ("000074", "425", "桜の樹の下には", "梶井基次郎"),

    # 谷崎潤一郎 (3篇)
    ("001383", "56470", "春琴抄", "谷崎潤一郎"),
    ("001383", "56471", "痴人の愛", "谷崎潤一郎"),
    ("001383", "56472", "陰翳礼讃", "谷崎潤一郎"),

    # 坂口安吾 (3篇)
    ("001095", "42618", "堕落論", "坂口安吾"),
    ("001095", "42619", "白痴", "坂口安吾"),
    ("001095", "42620", "桜の森の満開の下", "坂口安吾"),
]

OUTPUT_DIR = "../app/src/main/assets/aozora"

def download_article(author_id, work_id, title, author):
    """下载单篇文章"""
    # 先访问 card 页面获取文本链接
    card_url = f"https://www.aozora.gr.jp/cards/{author_id}/card{work_id}.html"

    try:
        print(f"正在下载: {author} - {title}")

        response = requests.get(card_url, timeout=15)
        response.encoding = 'shift_jis'
        soup = BeautifulSoup(response.text, 'html.parser')

        # 查找 HTML 文本链接
        text_link = None
        for link in soup.find_all('a', href=True):
            href = link['href']
            if 'files' in href and href.endswith('.html'):
                text_link = f"https://www.aozora.gr.jp/cards/{author_id}/{href}"
                break

        if not text_link:
            print(f"  ❌ 未找到文本链接")
            return False

        # 下载文本内容
        text_response = requests.get(text_link, timeout=15)
        text_response.encoding = 'shift_jis'
        text_soup = BeautifulSoup(text_response.text, 'html.parser')

        # 提取正文
        main_text = text_soup.find('div', class_='main_text')
        if not main_text:
            main_text = text_soup.find('div', id='contents')

        if not main_text:
            print(f"  ❌ 未找到正文内容")
            return False

        content = main_text.get_text()

        # 保存到文件
        filename = f"{author_id}_{work_id}.txt"
        filepath = os.path.join(OUTPUT_DIR, filename)

        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(f"# {title}\n")
            f.write(f"# {author}\n\n")
            f.write(content)

        print(f"  ✓ 已保存: {filename} ({len(content)} 字)")
        return True

    except Exception as e:
        print(f"  ❌ 下载失败: {e}")
        return False

def main():
    # 创建输出目录
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    print(f"开始下载 {len(ARTICLES)} 篇文章到 {OUTPUT_DIR}\n")

    success_count = 0
    for author_id, work_id, title, author in ARTICLES:
        if download_article(author_id, work_id, title, author):
            success_count += 1
        time.sleep(2)  # 避免请求过快

    print(f"\n完成！成功下载 {success_count}/{len(ARTICLES)} 篇文章")

if __name__ == "__main__":
    main()
