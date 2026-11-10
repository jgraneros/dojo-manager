package com.senshin.dojo_manager.students.infrastructure.web

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import java.net.URI
import java.time.Instant

/**
 * Representación RFC 9457 (application/problem+json) de los errores de la API,
 * extendiendo el [ProblemDetail] de Spring con dos propiedades adicionales:
 *
 *  - `timestamp`: instante en el que se generó el problema
 *  - `errors`:    detalle de validación por campo, para fallos de @Valid
 *
 * Subclasiar ProblemDetail está soportado oficialmente por Spring Framework
 * (los miembros extra se serializan a nivel raíz del JSON).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiProblemDetail(
    status: HttpStatusCode,
    title: String? = null,
    detail: String? = null,
    instance: URI? = null,
    val errors: List<FieldError>? = null
) : ProblemDetail(status.value()) {

    val timestamp: Instant = Instant.now()

    init {
        this.title = title
        this.detail = detail
        this.instance = instance
    }

    data class FieldError(
        val field: String,
        val message: String
    )
}
