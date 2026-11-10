package com.senshin.dojo_manager.students.infrastructure.persistence.entity

import com.senshin.dojo_manager.students.domain.model.BeltColor
import com.senshin.dojo_manager.students.domain.model.EnrollmentStatus
import com.senshin.dojo_manager.students.domain.model.RankType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "students")
class StudentJpaEntity(
    @Id
    val id: UUID,

    @Column(nullable = false, length = 100)
    var firstName: String,

    @Column(nullable = false, length = 100)
    var lastName: String,

    @Column(nullable = false, unique = true, length = 20)
    var identificationNumber: String,

    @Column(nullable = false)
    var birthDate: LocalDate,

    @Column(nullable = false, length = 30)
    var phone: String,

    @Column(nullable = false, length = 100)
    var emergencyContactName: String,

    @Column(nullable = false, length = 30)
    var emergencyContactPhone: String,

    @Column(columnDefinition = "TEXT")
    var medicalNotes: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: EnrollmentStatus,

    // Datos embebidos del rango actual para consultas rápidas
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var currentRankType: RankType,

    @Column(nullable = false)
    var currentRankLevel: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var currentRankColor: BeltColor,

    @Column(nullable = false)
    var currentRankOrder: Int,

    @OneToMany(mappedBy = "student", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    var promotionHistory: MutableList<PromotionRecordJpaEntity> = mutableListOf()
)


