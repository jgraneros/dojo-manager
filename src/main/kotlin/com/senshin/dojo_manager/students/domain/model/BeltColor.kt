package com.senshin.dojo_manager.students.domain.model

import com.senshin.dojo_manager.students.domain.exception.InvalidRankException

enum class BeltColor(val displayName: String) {
    WHITE("Blanco"),
    SKYE_BLUE("Celeste"),
    YELLOW("Amarillo"),
    ORANGE("Naranja"),
    GREEN("Verde"),
    BLUE("Azul"),
    BROWN("Marron"),
    BLACK("Negro");

    companion object {
        fun from(value: String): BeltColor =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw InvalidRankException(
                    "Color de cinturón inválido: '$value'. Valores permitidos: ${entries.joinToString()}"
                )
    }
}