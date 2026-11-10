package com.senshin.dojo_manager.students.domain.model

import java.time.LocalDate
import java.util.UUID

data class PromotionRecord(
    val id: UUID = UUID.randomUUID(),
    val fromRank: Rank,
    val toRank: Rank,
    val promotionDate: LocalDate,
    val examiner: String,
    val observations: String? = null
)
