package com.senshin.dojo_manager.students.infrastructure.web

import com.senshin.dojo_manager.students.application.dto.PromotionStudentRequest
import com.senshin.dojo_manager.students.application.dto.RegisterStudentsRequest
import com.senshin.dojo_manager.students.application.dto.StudentApplicationService
import com.senshin.dojo_manager.students.application.dto.StudentsResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/students")
class StudentController(private val studentApplicationService: StudentApplicationService) {

    @PostMapping
    fun register(@Valid @RequestBody request: RegisterStudentsRequest): ResponseEntity<StudentsResponse> {
        val response = studentApplicationService.registerStudent(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): ResponseEntity<StudentsResponse> {
        val response = studentApplicationService.getStudent(id)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{id}/promotions")
    fun promote(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PromotionStudentRequest
    ): ResponseEntity<StudentsResponse> {
        val response = studentApplicationService.promoteStudent(id, request)
        return ResponseEntity.ok(response)
    }
}