package com.senshin.dojo_manager.students.infrastructure.persistence

import com.senshin.dojo_manager.students.domain.model.Student
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataStudentRepository: JpaRepository<StudentJpaEntity, UUID> {
    fun findByIdentificationNumber(identificationNumber: String): StudentJpaEntity?
}