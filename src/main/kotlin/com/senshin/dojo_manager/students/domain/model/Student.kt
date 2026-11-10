package com.senshin.dojo_manager.students.domain.model

import com.senshin.dojo_manager.students.domain.exception.InactiveStudentException
import com.senshin.dojo_manager.students.domain.exception.InvalidRankException
import java.time.LocalDate
import java.util.UUID

enum class EnrollmentStatus {
    ACTIVE, SUSPENDED, INACTIVE;

    val isActive: Boolean get() = this == ACTIVE
}

@JvmInline
value class StudentId(val value: UUID) {
    companion object {
        fun generate(): StudentId = StudentId(UUID.randomUUID())
    }
}

class Student(
    val id: StudentId,
    var personalInfo: PersonalInfo,
    status: EnrollmentStatus = EnrollmentStatus.ACTIVE,
    currentRank: Rank = Rank.eigthKyu()
) {
    var status: EnrollmentStatus = status
        private set

    var currentRank: Rank = currentRank
        private set

    private val _promotionHistory = mutableListOf<PromotionRecord>()
    val promotionHistory: List<PromotionRecord> get() = _promotionHistory.toList()

    fun promoteTo(newRank: Rank, promotionDate: LocalDate, examiner: String, observations: String? = null) {
        if (!status.isActive) {
            throw InactiveStudentException("Un alumno inactivo o suspendido no puede rendir examen")
        }
        if (newRank <= currentRank) {
            throw InvalidRankException(
                "El grado otorgado ($newRank) debe ser estrictamente superior al grado actual ($currentRank)"
            )
        }
        require(examiner.isNotBlank()) { "El evaluador/examinador es obligatorio" }

        val record = PromotionRecord(
            fromRank = this.currentRank,
            toRank = newRank,
            promotionDate = promotionDate,
            examiner = examiner,
            observations = observations
        )

        _promotionHistory.add(record)
        this.currentRank = newRank
    }

    fun pauseEnrollment() {
        this.status = EnrollmentStatus.SUSPENDED
    }

    fun reactivateEnrollment() {
        this.status = EnrollmentStatus.ACTIVE
    }

    fun cancelEnrollment() {
        this.status = EnrollmentStatus.INACTIVE
    }

    internal fun loadPromotion(record: PromotionRecord) {
        _promotionHistory.add(record)
    }
}