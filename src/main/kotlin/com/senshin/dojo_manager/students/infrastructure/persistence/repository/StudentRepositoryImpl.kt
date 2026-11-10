package com.senshin.dojo_manager.students.infrastructure.persistence.repository

import com.senshin.dojo_manager.students.domain.model.Student
import com.senshin.dojo_manager.students.domain.model.StudentId
import com.senshin.dojo_manager.students.domain.repository.StudentRepository
import com.senshin.dojo_manager.students.infrastructure.persistence.mapper.toDomain
import com.senshin.dojo_manager.students.infrastructure.persistence.mapper.toJpaEntity

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Repository
@Transactional
class StudentRepositoryImpl(
    private val springDataRepo: SpringDataStudentRepository
): StudentRepository {
    override fun save(student: Student): Student {
        val entity = student.toJpaEntity()
        val savedEntity = springDataRepo.save(entity)
        return savedEntity.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findById(id: StudentId): Student? {
        return springDataRepo.findById(id.value).getOrNull()?.toDomain()
    }

    @Transactional(readOnly = true)
    override fun findByIdentificationNumber(dni: String): Student? {
        return springDataRepo.findByIdentificationNumber(dni)?.toDomain()
    }
}