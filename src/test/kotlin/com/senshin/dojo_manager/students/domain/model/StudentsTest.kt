package com.senshin.dojo_manager.students.domain.model

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StudentsTest {

    private fun dummyPersonalInfo(): PersonalInfo = PersonalInfo(
        firstName = "Lucas",
        lastName = "Benítez",
        identificationNumber = "38123456",
        birthDate = LocalDate.of(1995, 5, 20),
        phone = "3815551234",
        emergencyContactName = "María González",
        emergencyContactPhone = "3815559876"
    )

    @Test
    fun `nuevo alumno ingresa en octavo kyu y estado activo`() {
        val student = Student(
            id = StudentId.generate(),
            personalInfo = dummyPersonalInfo()
        )

        assertEquals(EnrollmentStatus.ACTIVE, student.status)
        assertEquals(Rank.eigthKyu(), student.currentRank)
        assertTrue(student.promotionHistory.isEmpty())
    }


}