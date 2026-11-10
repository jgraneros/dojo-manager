package com.senshin.dojo_manager.students.domain.repository

import com.senshin.dojo_manager.students.domain.model.Student
import com.senshin.dojo_manager.students.domain.model.StudentId

interface StudentRepository {
    fun save(student: Student): Student
    fun findById(id: StudentId): Student?
    fun findByIdentificationNumber(dni: String): Student?
}