package com.senshin.dojo_manager.students.infrastructure.web

import com.senshin.dojo_manager.students.domain.exception.DuplicateIdentificationNumberException
import com.senshin.dojo_manager.students.domain.exception.InactiveStudentException
import com.senshin.dojo_manager.students.domain.exception.InvalidRankException
import com.senshin.dojo_manager.students.domain.exception.StudentNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.net.URI

/**
 * Manejo centralizado de errores del contexto Students.
 *
 * Extiende [ResponseEntityExceptionHandler] para cubrir también las excepciones
 * estándar de Spring MVC (405, 406, 415, etc.) con el mismo formato, y las
 * normaliza todas a [ApiProblemDetail] (RFC 9457) mediante [createResponseEntity].
 */
@RestControllerAdvice
class StudentControllerAdvice : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(StudentControllerAdvice::class.java)

    // ------------------------------------------------------------------
    // Excepciones de dominio (mapeo exacto)
    // ------------------------------------------------------------------

    /** 404 — el alumno no existe. */
    @ExceptionHandler(StudentNotFoundException::class)
    fun handleStudentNotFound(ex: StudentNotFoundException, request: HttpServletRequest): ApiProblemDetail =
        problem(HttpStatus.NOT_FOUND, "Alumno no encontrado", ex.message, request)

    /** 409 — ya existe un alumno con ese número de documento. */
    @ExceptionHandler(DuplicateIdentificationNumberException::class)
    fun handleDuplicateIdentificationNumber(
        ex: DuplicateIdentificationNumberException,
        request: HttpServletRequest
    ): ApiProblemDetail =
        problem(HttpStatus.CONFLICT, "Alumno duplicado", ex.message, request)

    /** 422 — el rango indicado viola las reglas del dominio. */
    @ExceptionHandler(InvalidRankException::class)
    fun handleInvalidRank(ex: InvalidRankException, request: HttpServletRequest): ApiProblemDetail =
        problem(HttpStatus.UNPROCESSABLE_ENTITY, "Rango inválido", ex.message, request)

    /** 409 — el estado de matrícula del alumno impide la operación. */
    @ExceptionHandler(InactiveStudentException::class)
    fun handleInactiveStudent(ex: InactiveStudentException, request: HttpServletRequest): ApiProblemDetail =
        problem(HttpStatus.CONFLICT, "Operación no permitida", ex.message, request)

    // ------------------------------------------------------------------
    // Fallbacks para invariantes de dominio expresadas con la JDK
    // ------------------------------------------------------------------

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElement(ex: NoSuchElementException, request: HttpServletRequest): ApiProblemDetail =
        problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.message, request)

    /** 422 — request bien formado pero que viola una regla de negocio. */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException, request: HttpServletRequest): ApiProblemDetail =
        problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", ex.message, request)

    /** 409 — el estado actual del recurso impide la operación. */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(ex: IllegalStateException, request: HttpServletRequest): ApiProblemDetail =
        problem(HttpStatus.CONFLICT, "Conflicto con el estado del recurso", ex.message, request)

    // ------------------------------------------------------------------
    // Infraestructura
    // ------------------------------------------------------------------

    /** 409 — violación de constraints de BD (UNIQUE, FK, etc.). */
    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException, request: HttpServletRequest): ApiProblemDetail {
        log.warn(
            "Violación de integridad en {} {}: {}",
            request.method, request.requestURI, ex.mostSpecificCause.message
        )
        return problem(
            HttpStatus.CONFLICT,
            "Conflicto de datos",
            "El registro viola una restricción de la base de datos",
            request
        )
    }

    /** 500 — último recurso: se loguea el stack completo pero no se exponen detalles internos. */
    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception, request: HttpServletRequest): ApiProblemDetail {
        log.error("Error inesperado procesando {} {}", request.method, request.requestURI, ex)
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", request)
    }

    // ------------------------------------------------------------------
    // Excepciones estándar de Spring MVC (mensajes en español)
    // ------------------------------------------------------------------

    /** 400 — falló @Valid sobre el body; incluye detalle por campo. */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? =
        handleExceptionInternal(
            ex,
            ApiProblemDetail(
                status = status,
                title = "Solicitud inválida",
                detail = "Uno o más campos no cumplen las validaciones",
                instance = instanceOf(request),
                errors = ex.fieldErrors.map { ApiProblemDetail.FieldError(it.field, it.defaultMessage ?: "valor inválido") }
            ),
            headers,
            status,
            request
        )

    /** 400 — JSON malformado o ilegible. */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? =
        handleExceptionInternal(
            ex,
            ApiProblemDetail(
                status = status,
                title = "Solicitud ilegible",
                detail = "El cuerpo de la solicitud no es un JSON válido",
                instance = instanceOf(request)
            ),
            headers,
            status,
            request
        )

    /** 400 — path variable o parámetro con formato inválido (p. ej. UUID mal formado). */
    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val detail = if (ex is MethodArgumentTypeMismatchException) {
            "El valor '${ex.value}' no es válido para el parámetro '${ex.name}'"
        } else {
            "El valor '${ex.value}' no es válido"
        }
        return handleExceptionInternal(
            ex,
            ApiProblemDetail(status = status, title = "Parámetro inválido", detail = detail, instance = instanceOf(request)),
            headers,
            status,
            request
        )
    }

    /** 404 — path inexistente. */
    override fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? =
        handleExceptionInternal(
            ex,
            ApiProblemDetail(
                status = status,
                title = "Recurso no encontrado",
                detail = "No existe el recurso '${ex.resourcePath}'",
                instance = instanceOf(request)
            ),
            headers,
            status,
            request
        )

    // ------------------------------------------------------------------
    // Normalización: todo lo que produce la clase base (405, 406, 415, ...)
    // se envuelve en ApiProblemDetail para mantener una forma uniforme
    // ------------------------------------------------------------------

    override fun createResponseEntity(
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val problem = when (body) {
            is ApiProblemDetail -> body
            is ProblemDetail -> ApiProblemDetail(
                status = statusCode,
                title = body.title,
                detail = body.detail,
                instance = body.instance ?: instanceOf(request)
            )
            else -> ApiProblemDetail(status = statusCode, title = null, detail = null, instance = instanceOf(request))
        }
        return ResponseEntity(problem, headers, statusCode)
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private fun problem(
        status: HttpStatusCode,
        title: String,
        detail: String?,
        request: HttpServletRequest,
        errors: List<ApiProblemDetail.FieldError>? = null
    ): ApiProblemDetail =
        ApiProblemDetail(
            status = status,
            title = title,
            detail = detail,
            instance = URI.create(request.requestURI),
            errors = errors
        )

    private fun instanceOf(request: WebRequest): URI? =
        (request as? ServletWebRequest)?.request?.requestURI?.let(URI::create)
}
