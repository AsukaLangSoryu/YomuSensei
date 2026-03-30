package com.yomusensei.data.web

/**
 * 青空文库知名作品索引（预设列表）
 */
object AozoraIndex {

    data class AozoraWork(
        val title: String,
        val author: String,
        val fileUrl: String,
        val authorId: String,
        val workId: String
    )

    private val works = listOf(
        // 夏目漱石 (10篇)
        AozoraWork("こころ", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card773.html", "000148", "773"),
        AozoraWork("坊っちゃん", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card752.html", "000148", "752"),
        AozoraWork("吾輩は猫である", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card789.html", "000148", "789"),
        AozoraWork("三四郎", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card794.html", "000148", "794"),
        AozoraWork("それから", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card799.html", "000148", "799"),
        AozoraWork("草枕", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card776.html", "000148", "776"),
        AozoraWork("夢十夜", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card751.html", "000148", "751"),
        AozoraWork("文鳥", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card762.html", "000148", "762"),
        AozoraWork("門", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card800.html", "000148", "800"),
        AozoraWork("行人", "夏目漱石", "https://www.aozora.gr.jp/cards/000148/card795.html", "000148", "795"),

        // 宮沢賢治 (8篇)
        AozoraWork("銀河鉄道の夜", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card43737.html", "000081", "43737"),
        AozoraWork("注文の多い料理店", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card43754.html", "000081", "43754"),
        AozoraWork("風の又三郎", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card43752.html", "000081", "43752"),
        AozoraWork("セロ弾きのゴーシュ", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card470.html", "000081", "470"),
        AozoraWork("よだかの星", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card456.html", "000081", "456"),
        AozoraWork("雨ニモマケズ", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card45630.html", "000081", "45630"),
        AozoraWork("オツベルと象", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card473.html", "000081", "473"),
        AozoraWork("やまなし", "宮沢賢治", "https://www.aozora.gr.jp/cards/000081/card464.html", "000081", "464"),

        // 芥川龍之介 (15篇短篇)
        AozoraWork("羅生門", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card127.html", "000879", "127"),
        AozoraWork("蜘蛛の糸", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card92.html", "000879", "92"),
        AozoraWork("鼻", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card94.html", "000879", "94"),
        AozoraWork("地獄変", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card166.html", "000879", "166"),
        AozoraWork("河童", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card69.html", "000879", "69"),
        AozoraWork("藪の中", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card179.html", "000879", "179"),
        AozoraWork("トロッコ", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card42.html", "000879", "42"),
        AozoraWork("杜子春", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card158.html", "000879", "158"),
        AozoraWork("舞踏会", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card43.html", "000879", "43"),
        AozoraWork("或る日の大石内蔵助", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card44.html", "000879", "44"),
        AozoraWork("芋粥", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card45.html", "000879", "45"),
        AozoraWork("手巾", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card46.html", "000879", "46"),
        AozoraWork("秋", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card47.html", "000879", "47"),
        AozoraWork("奉教人の死", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card48.html", "000879", "48"),
        AozoraWork("煙草と悪魔", "芥川龍之介", "https://www.aozora.gr.jp/cards/000879/card49.html", "000879", "49"),

        // 太宰治 (6篇)
        AozoraWork("人間失格", "太宰治", "https://www.aozora.gr.jp/cards/000035/card301.html", "000035", "301"),
        AozoraWork("走れメロス", "太宰治", "https://www.aozora.gr.jp/cards/000035/card1567.html", "000035", "1567"),
        AozoraWork("斜陽", "太宰治", "https://www.aozora.gr.jp/cards/000035/card1565.html", "000035", "1565"),
        AozoraWork("津軽", "太宰治", "https://www.aozora.gr.jp/cards/000035/card2282.html", "000035", "2282"),
        AozoraWork("ヴィヨンの妻", "太宰治", "https://www.aozora.gr.jp/cards/000035/card1588.html", "000035", "1588"),
        AozoraWork("お伽草紙", "太宰治", "https://www.aozora.gr.jp/cards/000035/card1566.html", "000035", "1566"),

        // 森鴎外 (4篇)
        AozoraWork("舞姫", "森鴎外", "https://www.aozora.gr.jp/cards/000129/card695.html", "000129", "695"),
        AozoraWork("高瀬舟", "森鴎外", "https://www.aozora.gr.jp/cards/000129/card2768.html", "000129", "2768"),
        AozoraWork("山椒大夫", "森鴎外", "https://www.aozora.gr.jp/cards/000129/card2767.html", "000129", "2767"),
        AozoraWork("阿部一族", "森鴎外", "https://www.aozora.gr.jp/cards/000129/card696.html", "000129", "696"),

        // 梶井基次郎 (2篇)
        AozoraWork("檸檬", "梶井基次郎", "https://www.aozora.gr.jp/cards/000074/card424.html", "000074", "424"),
        AozoraWork("桜の樹の下には", "梶井基次郎", "https://www.aozora.gr.jp/cards/000074/card425.html", "000074", "425"),

        // 谷崎潤一郎 (3篇)
        AozoraWork("春琴抄", "谷崎潤一郎", "https://www.aozora.gr.jp/cards/001383/card56470.html", "001383", "56470"),
        AozoraWork("痴人の愛", "谷崎潤一郎", "https://www.aozora.gr.jp/cards/001383/card56471.html", "001383", "56471"),
        AozoraWork("陰翳礼讃", "谷崎潤一郎", "https://www.aozora.gr.jp/cards/001383/card56472.html", "001383", "56472"),

        // 坂口安吾 (3篇)
        AozoraWork("堕落論", "坂口安吾", "https://www.aozora.gr.jp/cards/001095/card42618.html", "001095", "42618"),
        AozoraWork("白痴", "坂口安吾", "https://www.aozora.gr.jp/cards/001095/card42619.html", "001095", "42619"),
        AozoraWork("桜の森の満開の下", "坂口安吾", "https://www.aozora.gr.jp/cards/001095/card42620.html", "001095", "42620")
    )

    fun search(query: String): List<AozoraWork> {
        val normalizedQuery = query.trim()
        return works.filter { work ->
            work.title.contains(normalizedQuery, ignoreCase = true) ||
            work.author.contains(normalizedQuery, ignoreCase = true)
        }
    }
}
