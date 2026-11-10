package com.senshin.dojo_manager.students.domain.model

import com.senshin.dojo_manager.students.domain.exception.InvalidRankException

enum class RankType {
    KYU, DAN;

    companion object {
        fun from(value: String): RankType =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw InvalidRankException(
                    "Tipo de rango inválido: '$value'. Valores permitidos: ${entries.joinToString()}"
                )
    }
}

data class Rank(
    val type: RankType,
    val level: Int,
    val color: BeltColor,
    val orderIndex: Int
): Comparable<Rank> {

    init {
        if (level !in 1..8) throw InvalidRankException("El nivel del rango debe estar entre 1 y 8")
        if (orderIndex < 1) throw InvalidRankException("El índice de orden del rango debe ser mayor o igual a 1")
    }

    override fun compareTo(other: Rank): Int = this.orderIndex.compareTo(other.orderIndex)

    companion object {

        fun eigthKyu(): Rank = Rank(
            type = RankType.KYU,
            level = 8,
            color = BeltColor.WHITE,
            orderIndex = 1
        )

        fun kyu(level: Int, color: BeltColor, orderIndex: Int): Rank =
            Rank(RankType.KYU, level, color, orderIndex)

        fun dan(level: Int, orderIndex: Int): Rank =
            Rank(RankType.DAN, level, BeltColor.BLACK, orderIndex)
    }
}
