package com.yomusensei.data.vocabulary

/**
 * 复习调度器 - 简化间隔重复算法
 */
object ReviewScheduler {
    // 复习间隔（小时）
    private val REVIEW_INTERVALS = listOf(
        0,      // Level 0: 立即复习
        24,     // Level 1: 1天后
        72,     // Level 2: 3天后
        168,    // Level 3: 7天后
        720,    // Level 4: 30天后
        2160    // Level 5: 90天后（已掌握）
    )

    /**
     * 计算下次复习时间
     * @param currentLevel 当前复习等级 (0-5)
     * @param isCorrect 本次是否答对
     * @return Pair<新等级, 下次复习时间戳>
     */
    fun calculateNextReview(
        currentLevel: Int,
        isCorrect: Boolean
    ): Pair<Int, Long> {
        val newLevel = if (isCorrect) {
            // 答对：升级
            (currentLevel + 1).coerceAtMost(5)
        } else {
            // 答错：降级（但不低于0）
            (currentLevel - 1).coerceAtLeast(0)
        }

        val intervalHours = REVIEW_INTERVALS[newLevel]
        val nextReviewTime = System.currentTimeMillis() +
            intervalHours * 3600 * 1000L

        return Pair(newLevel, nextReviewTime)
    }

    /**
     * 获取等级描述
     */
    fun getLevelDescription(level: Int): String {
        return when (level) {
            0 -> "新学习"
            1 -> "初步记忆"
            2 -> "短期记忆"
            3 -> "中期记忆"
            4 -> "长期记忆"
            5 -> "已掌握"
            else -> "未知"
        }
    }
}
