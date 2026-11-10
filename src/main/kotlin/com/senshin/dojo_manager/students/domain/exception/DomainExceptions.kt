package com.senshin.dojo_manager.students.domain.exception

import java.util.UUID

/**
 * Se lanza cuando no existe un alumno para el identificador dado.
 */
class StudentNotFoundException(val studentId: UUID) :
    NoSuchElementException("No se encontró el alumno con ID: $studentId")

/**
 * Se lanza al intentar registrar un alumno con un número de documento ya registrado.
 */
class DuplicateIdentificationNumberException(val identificationNumber: String) :
    RuntimeException("Ya existe un alumno registrado con el número de documento: $identificationNumber")

/**
 * Se lanza cuando un rango (grado) viola las reglas del dominio:
 * valores fuera de rango, progresión no ascendente o identificadores inválidos.
 */
class InvalidRankException(message: String) : IllegalArgumentException(message)

/**
 * Se lanza cuando el estado de matrícula del alumno impide la operación solicitada.
 */
class InactiveStudentException(message: String) : IllegalStateException(message)
