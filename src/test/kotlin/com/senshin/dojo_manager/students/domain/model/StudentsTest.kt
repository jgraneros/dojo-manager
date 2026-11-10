package com.senshin.dojo_manager.students.domain.model

import com.senshin.dojo_manager.students.domain.exception.InactiveStudentException
import com.senshin.dojo_manager.students.domain.exception.InvalidRankException
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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

    @Test
    fun `promover alumno suspendido lanza InactiveStudentException`() {
        val student = Student(
            id = StudentId.generate(),
            personalInfo = dummyPersonalInfo()
        ).apply { pauseEnrollment() }

        assertFailsWith<InactiveStudentException> {
            student.promoteTo(
                newRank = Rank(RankType.KYU, level = 7, color = BeltColor.YELLOW, orderIndex = 2),
                promotionDate = LocalDate.of(2026, 1, 10),
                examiner = "Sensei García"
            )
        }

        assertEquals(EnrollmentStatus.SUSPENDED, student.status)
        assertTrue(student.promotionHistory.isEmpty())
    }

    @Test
    fun `promover a un grado no superior lanza InvalidRankException`() {
        val student = Student(
            id = StudentId.generate(),
            personalInfo = dummyPersonalInfo()
        )

        assertFailsWith<InvalidRankException> {
            student.promoteTo(
                newRank = Rank(RankType.KYU, level = 8, color = BeltColor.WHITE, orderIndex = 1),
                promotionDate = LocalDate.of(2026, 1, 10),
                examiner = "Sensei García"
            )
        }

        assertTrue(student.promotionHistory.isEmpty())
    }

}