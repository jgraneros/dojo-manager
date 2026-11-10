package com.senshin.dojo_manager.students.application.dto

import com.senshin.dojo_manager.students.domain.exception.DuplicateIdentificationNumberException
import com.senshin.dojo_manager.students.domain.exception.StudentNotFoundException
import com.senshin.dojo_manager.students.domain.model.BeltColor
import com.senshin.dojo_manager.students.domain.model.PersonalInfo
import com.senshin.dojo_manager.students.domain.model.Rank
import com.senshin.dojo_manager.students.domain.model.RankType
import com.senshin.dojo_manager.students.domain.model.Student
import com.senshin.dojo_manager.students.domain.model.StudentId
import com.senshin.dojo_manager.students.domain.repository.StudentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class StudentApplicationService(
    private val studentRepository: StudentRepository
) {

fun registerStudent(request: RegisterStudentsRequest): StudentsResponse {

        val identificationNumber = request.identificationNumber.trim()

        if (studentRepository.findByIdentificationNumber(identificationNumber) != null) {
            throw DuplicateIdentificationNumberException(identificationNumber)
        }

        val personalInfo = PersonalInfo(
            firstName = request.firstName.trim(),
            lastName = request.lastName.trim(),
            identificationNumber = identificationNumber,
            birthDate = request.birthDate,
            phone = request.phone.trim(),
            emergencyContactName = request.emergencyContactName.trim(),
            emergencyContactPhone = request.emergencyContactPhone.trim(),
            medicalNotes = request.medicalNotes?.trim()
        )

        val newStudent = Student(
            id = StudentId.generate(),
            personalInfo = personalInfo
        )

        val saved = studentRepository.save(newStudent)

        return saved.toResponse()

    }

    @Transactional(readOnly = true)
    fun getStudent(id: UUID): StudentsResponse {

        val student = studentRepository.findById(StudentId(id))
            ?:throw StudentNotFoundException(id)
        return student.toResponse()
    }

    fun promoteStudent(id: UUID, request: PromotionStudentRequest): StudentsResponse {

        val student = studentRepository.findById(StudentId(id))
            ?:throw StudentNotFoundException(id)

        val newRank = Rank(
            type = RankType.from(request.targetRank.type),
            level = request.targetRank.level,
            color = BeltColor.from(request.targetRank.color),
            orderIndex = request.targetRank.orderIndex
        )

        student.promoteTo(
            newRank = newRank,
            promotionDate = request.promotionDate,
            examiner = request.examiner,
            observations = request.observations
        )

        val updated = studentRepository.save(student)
        return updated.toResponse()

    }

}