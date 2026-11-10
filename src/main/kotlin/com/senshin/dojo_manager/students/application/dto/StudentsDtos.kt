package com.senshin.dojo_manager.students.application.dto

import com.senshin.dojo_manager.students.domain.model.EnrollmentStatus
import com.senshin.dojo_manager.students.domain.model.Student


import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import java.time.LocalDate
import java.util.UUID



data class RegisterStudentsRequest(

    @field:NotBlank(message = "El nombre es obligatorio")
    val firstName: String,

    @field:NotBlank(message = "El apellido es obligatorio")
    val lastName: String,

    @field:NotBlank(message = "El DNI es obligatorio")
    val identificationNumber: String,

    @field:NotNull(message = "La fecha de nacimiento es requerida")
    @field:Past(message = "La fecha de nacimiento debe ser pasada")
    val birthDate: LocalDate,

    @field:NotBlank(message = "El teléfono es obligatorio")
    val phone: String,

    @field:NotBlank(message = "El contacto de emergencia es obligatorio")
    val emergencyContactName: String,

    @field:NotBlank(message = "El teléfono de emergencia es obligatorio")
    val emergencyContactPhone: String,

    val medicalNotes: String? = null

)

data class PromotionStudentRequest(
    @field:NotNull(message = "El nuevo rango es obligatorio")
    val targetRank: RankDto,

    @field:NotNull(message = "La fecha de examen es obligatoria")
    val promotionDate: LocalDate,

    @field:NotBlank(message = "El nombre del examinador es obligatorio")
    val examiner: String,

    val observations: String? = null
)

data class RankDto(
    val type: String, // "KYU" o "DAN"
    val level: Int,
    val color: String,
    val orderIndex: Int
)

data class StudentsResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val identificationNumber: String,
    val birthDate: LocalDate,
    val phone: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val medicalNotes: String?,
    val status: EnrollmentStatus,
    val currentRank: RankDetailResponse,
    val promotionHistoryCount: Int
)

data class RankDetailResponse(
    val type: String,
    val level: Int,
    val beltColor: String,
    val orderIndex: Int
)

fun Student.toResponse(): StudentsResponse = StudentsResponse(
    id = this.id.value,
    firstName = this.personalInfo.firstName,
    lastName = this.personalInfo.lastName,
    identificationNumber = this.personalInfo.identificationNumber,
    birthDate = this.personalInfo.birthDate,
    phone = this.personalInfo.phone,
    emergencyContactName = this.personalInfo.emergencyContactName,
    emergencyContactPhone = this.personalInfo.emergencyContactPhone,
    medicalNotes = this.personalInfo.medicalNotes,
    status = this.status,
    currentRank = RankDetailResponse(
        type = this.currentRank.type.name,
        level = this.currentRank.level,
        beltColor = this.currentRank.color.displayName,
        orderIndex = this.currentRank.orderIndex
    ),
    promotionHistoryCount = this.promotionHistory.size
)

