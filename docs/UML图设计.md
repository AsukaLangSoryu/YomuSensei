# YomuSensei UML图设计文档

本文档包含课程设计报告中需要的所有UML图的详细描述，可用于生成精致的图表。

---

## 1. 用例图

### 1.1 顶层用例图

**描述：** 展示用户与系统的主要交互

**元素：**
- 参与者：用户（日语学习者）
- 用例：
  1. 浏览和阅读文章
  2. 查询和学习单词
  3. 复习词库单词
  4. 配置系统设置

**关系：** 用户与4个用例之间都是关联关系

---

### 1.2 文章阅读子系统用例图

**描述：** 展示文章阅读相关的详细用例

**元素：**
- 参与者：用户
- 用例：
  1. AI推荐文章
  2. 输入URL阅读
  3. 离线文学作品阅读
  4. 调整阅读设置
  5. 向AI提问

**关系：** 用户与5个用例之间都是关联关系

---

### 1.3 词库管理子系统用例图

**描述：** 展示词库管理相关的详细用例

**元素：**
- 参与者：用户
- 用例：
  1. 查询单词
  2. 浏览词库列表
  3. 开始复习
  4. 批量管理单词
  5. 收藏单词

**关系：** 用户与5个用例之间都是关联关系

---

## 2. 类图

### 2.1 阅读模块类图

**类：**

1. **Article（文章类）**
   - 属性：title: String, content: String, url: String, source: String
   - 方法：无（数据类）

2. **ReaderViewModel（阅读视图模型）**
   - 属性：article: StateFlow<Article?>, fontSize: StateFlow<Float>, lineSpacing: StateFlow<Float>, explanation: StateFlow<TextExplanation?>
   - 方法：setArticle(article: Article), onTextSelected(text: String), adjustFontSize(delta: Float), askQuestion(question: String)

3. **AiProvider（AI提供商接口）**
   - 方法：chat(message: String, history: List<Message>): Result<String>, explainText(text: String, context: String): Result<String>, askQuestion(question: String, context: String): Result<String>

4. **GeminiProvider（Gemini实现类）**
   - 实现AiProvider接口
   - 方法：同接口

5. **OpenAICompatProvider（OpenAI兼容实现类）**
   - 实现AiProvider接口
   - 方法：同接口

**关系：**
- ReaderViewModel 使用(uses) Article
- ReaderViewModel 使用(uses) AiProvider
- GeminiProvider 实现(implements) AiProvider
- OpenAICompatProvider 实现(implements) AiProvider

---

### 2.2 词库模块类图

**类：**

1. **VocabularyWord（词库单词类）**
   - 属性：id: Long, word: String, reading: String, meaning: String, reviewLevel: Int, nextReviewTime: Long, isFavorite: Boolean
   - 方法：无（数据类）

2. **VocabularyRepository（词库仓库类）**
   - 属性：vocabularyDao: VocabularyDao
   - 方法：insertWord(word: VocabularyWord), getAllWords(): Flow<List<VocabularyWord>>, getWordsForReview(): List<VocabularyWord>, updateWord(word: VocabularyWord)

3. **VocabularyViewModel（词库视图模型）**
   - 属性：words: StateFlow<List<VocabularyWord>>, statistics: StateFlow<Statistics>
   - 方法：loadWords(), startReview(), deleteWords(ids: List<Long>)

**关系：**
- VocabularyRepository 管理(manages) VocabularyWord
- VocabularyViewModel 使用(uses) VocabularyRepository

---

## 3. 活动图

### 3.1 AI推荐文章流程活动图

**流程节点：**
1. 开始
2. 用户输入需求
3. 识别用户意图
4. 判断节点：意图类型？（找文章/问题/闲聊）
5. 找文章分支：调用Tavily搜索 → 返回文章列表
6. 问题分支：调用AI解答
7. 闲聊分支：普通对话
8. 显示结果
9. 用户点击文章
10. 抓取文章内容
11. 判断节点：抓取成功？
12. 成功分支：进入阅读页面
13. 失败分支：显示错误
14. 结束

---

## 4. 状态图

### 4.1 单词复习状态图

**状态节点：**
1. 新单词 (Level 0) - 初始状态
2. 学习中 (Level 1) - 复习间隔：1天
3. 熟悉 (Level 2) - 复习间隔：3天
4. 掌握 (Level 3) - 复习间隔：7天
5. 熟练 (Level 4) - 复习间隔：15天
6. 精通 (Level 5) - 复习间隔：30天

**状态转换：**
- 新单词 → 学习中：首次学习
- 学习中 → 熟悉：答对
- 熟悉 → 掌握：答对
- 掌握 → 熟练：答对
- 熟练 → 精通：答对
- 任何状态 → 降级：答错（降低一个等级）

---

## 5. 顺序图

### 5.1 查询单词交互顺序图

**参与对象：**
1. 用户
2. ReaderViewModel
3. DictionaryRepository
4. DictionaryDao
5. JishoAPI

**交互序列：**
1. 用户 → ReaderViewModel: 输入单词
2. ReaderViewModel → DictionaryRepository: 查询本地词典
3. DictionaryRepository → DictionaryDao: SQL查询
4. DictionaryDao → DictionaryRepository: 返回结果
5. DictionaryRepository → ReaderViewModel: 本地结果
6. 【如果本地未找到】ReaderViewModel → JishoAPI: 调用在线API
7. JishoAPI → ReaderViewModel: 返回词典数据
8. ReaderViewModel → DictionaryRepository: 缓存到本地
9. DictionaryRepository → DictionaryDao: 插入数据库
10. ReaderViewModel → 用户: 显示结果

---

### 5.2 智能复习交互顺序图

**参与对象：**
1. 用户
2. VocabularyViewModel
3. VocabularyRepository
4. ReviewScheduler
5. Database

**交互序列：**
1. 用户 → VocabularyViewModel: 开始复习
2. VocabularyViewModel → VocabularyRepository: 获取待复习单词
3. VocabularyRepository → Database: 查询
4. Database → VocabularyRepository: 返回单词
5. VocabularyRepository → VocabularyViewModel: 待复习单词
6. VocabularyViewModel → ReviewScheduler: 生成题目
7. VocabularyViewModel → 用户: 显示题目
8. 用户 → VocabularyViewModel: 选择答案
9. VocabularyViewModel: 判断正误
10. VocabularyViewModel → VocabularyRepository: 更新复习数据
11. VocabularyRepository → ReviewScheduler: 计算下次复习时间
12. ReviewScheduler → VocabularyRepository: 新的时间
13. VocabularyRepository → Database: 更新数据库
14. VocabularyViewModel → 用户: 显示反馈

---

## 6. 包图

### 6.1 系统包结构

**顶层包：** com.yomusensei

**子包：**

1. **ui包** - 表示层
   - home: 首页模块
   - reader: 阅读模块
   - vocabulary: 词库模块
   - dictionary: 词典模块
   - settings: 设置模块
   - theme: 主题配置

2. **data包** - 数据层
   - api: AI服务抽象
   - vocabulary: 词库数据
   - local: 本地词典
   - web: 网页抓取

3. **model包** - 数据模型
   - 各种数据类定义

**包依赖关系：**
- ui包 依赖 data包
- ui包 依赖 model包
- data包 依赖 model包
- ui包 依赖 theme包

---

**说明：** 以上所有图的描述可以用于生成专业的UML图表。建议使用PlantUML、draw.io或专业UML工具进行绘制。
