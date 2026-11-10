package com.senshin.dojo_manager.students.infrastructure.persistence.repository

import com.senshin.dojo_manager.students.infrastructure.persistence.entity.StudentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SpringDataStudentRepository: JpaRepository<StudentJpaEntity, UUID> {
    fun findByIdentificationNumber(identificationNumber: String): StudentJpaEntity?
}