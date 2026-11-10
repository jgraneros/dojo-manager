package com.senshin.dojo_manager.students.domain.model

enum class RankType {
    KYU, DAN
}

data class Rank(
    val type: RankType,
    val level: Int,
    val color: BeltColor,
    val orderIndex: Int
): Comparable<Rank> {

    init {
        require(level in 1..8) { "Level must be between 1 and 8" }
        require(orderIndex > 0) {"OrderIndex must be greater than or equal to 1"}
    }

    override fun compareTo(other: Rank): Int = this.orderIndex.compareTo(other.orderIndex)

    companion object {

        fun eigthKyu(): Rank = Rank(
            type = RankType.KYU,
            level = 8,
            color = BeltColor.WHITE,
            orderIndex = 1
        )

        fun kyu(level: Int, color: BeltColor, orderIndex: Int): Rank {
            require(level in 1..8)
            return Rank(RankType.KYU, level, color, orderIndex)
        }

        fun dan(level: Int, orderIndex: Int): Rank {
            require(level in 1..8)
            return Rank(RankType.DAN, level, BeltColor.BLACK, orderIndex)
        }
    }
}
