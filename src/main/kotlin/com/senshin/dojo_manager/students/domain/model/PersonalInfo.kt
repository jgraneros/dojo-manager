package com.senshin.dojo_manager.students.domain.model

import java.time.LocalDate

data class PersonalInfo(
    val firstName: String,
    val lastName: String,
    val identificationNumber: String, // DNI
    val birthDate: LocalDate,
    val phone: String,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val medicalNotes: String? = null
) {
    init {
        require(firstName.isNotBlank() && lastName.isNotBlank()) {"First name and Last name must not be blank."}
        require(identificationNumber.isNotBlank()) { "identification number must not be blank" }
    }
}