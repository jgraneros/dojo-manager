package com.senshin.dojo_manager.students.infrastructure.persistence

import com.senshin.dojo_manager.students.domain.model.BeltColor
import com.senshin.dojo_manager.students.domain.model.RankType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "PROMOTIONS")
class PromotionRecordJpaEntity(

    @Id
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    val student: StudentJpaEntity,

    // Rango anterior (From)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val fromRankType: RankType,

    @Column(nullable = false)
    val fromRankLevel: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val fromRankColor: BeltColor,

    @Column(nullable = false)
    val fromRankOrder: Int,

    // Rango otorgado (To)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val toRankType: RankType,

    @Column(nullable = false)
    val toRankLevel: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val toRankColor: BeltColor,

    @Column(nullable = false)
    val toRankOrder: Int,

    @Column(nullable = false)
    val promotionDate: LocalDate,

    @Column(nullable = false, length = 100)
    val examiner: String,

    @Column(columnDefinition = "TEXT")
    val observations: String?
)