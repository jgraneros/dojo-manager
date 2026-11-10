package com.senshin.dojo_manager.students.infrastructure.web

import com.senshin.dojo_manager.students.application.dto.StudentApplicationService
import com.senshin.dojo_manager.students.domain.model.PersonalInfo
import com.senshin.dojo_manager.students.domain.model.Student
import com.senshin.dojo_manager.students.domain.model.StudentId
import com.senshin.dojo_manager.students.domain.repository.StudentRepository
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

/**
 * Valida el contrato de errores HTTP de [StudentControllerAdvice]:
 * status correcto por tipo de error, Content-Type application/problem+json
 * y forma del body (ApiProblemDetail RFC 9457).
 *
 * Se usa el [StudentApplicationService] real sobre un repositorio in-memory
 * para ejercitar la cadena completa controller -> service -> dominio -> advice.
 */
@WebMvcTest(StudentController::class)
@Import(StudentControllerAdviceTest.TestConfig::class)
class StudentControllerAdviceTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var studentRepository: InMemoryStudentRepository

    @TestConfiguration
    class TestConfig {
        @Bean
        fun studentRepository(): InMemoryStudentRepository = InMemoryStudentRepository()

        @Bean
        fun studentApplicationService(studentRepository: StudentRepository): StudentApplicationService =
            StudentApplicationService(studentRepository)
    }

    @BeforeEach
    fun resetRepository() {
        studentRepository.reset()
    }

    // ------------------------------------------------------------------
    // Happy paths (sanity del fixture)
    // ------------------------------------------------------------------

    @Test
    fun `registrar alumno valido responde 201`() {
        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.currentRank.type").value("KYU"))
    }

    @Test
    fun `promover alumno activo responde 200`() {
        val id = seedStudent()

        mockMvc.perform(post("/api/v1/students/$id/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(promotionBody()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentRank.level").value(7))
            .andExpect(jsonPath("$.promotionHistoryCount").value(1))
    }

    // ------------------------------------------------------------------
    // Errores de dominio
    // ------------------------------------------------------------------

    @Test
    fun `obtener alumno inexistente responde 404 con problem detail`() {
        val id = UUID.randomUUID()

        mockMvc.perform(get("/api/v1/students/$id"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Alumno no encontrado"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.detail").value("No se encontró el alumno con ID: $id"))
            .andExpect(jsonPath("$.instance").value("/api/v1/students/$id"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    @Test
    fun `registrar alumno con dni duplicado responde 409`() {
        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody()))
            .andExpect(status().isCreated())

        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody()))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Alumno duplicado"))
            .andExpect(jsonPath("$.detail").value("Ya existe un alumno registrado con el número de documento: 38123456"))
    }

    @Test
    fun `promover con tipo de rango invalido responde 422`() {
        val id = seedStudent()

        mockMvc.perform(post("/api/v1/students/$id/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(promotionBody(type = "SHODAN")))
            .andExpect(status().isUnprocessableContent())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Rango inválido"))
            .andExpect(jsonPath("$.detail").value("Tipo de rango inválido: 'SHODAN'. Valores permitidos: KYU, DAN"))
    }

    @Test
    fun `promover a un rango no superior responde 422`() {
        val id = seedStudent()

        mockMvc.perform(post("/api/v1/students/$id/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(promotionBody(type = "KYU", level = 8, color = "WHITE", orderIndex = 1)))
            .andExpect(status().isUnprocessableContent())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Rango inválido"))
            .andExpect(jsonPath("$.detail", containsString("estrictamente superior")))
    }

    @Test
    fun `promover alumno suspendido responde 409`() {
        val id = seedStudent { pauseEnrollment() }

        mockMvc.perform(post("/api/v1/students/$id/promotions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(promotionBody()))
            .andExpect(status().isConflict())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Operación no permitida"))
            .andExpect(jsonPath("$.detail", containsString("suspendido")))
    }

    // ------------------------------------------------------------------
    // Errores de request
    // ------------------------------------------------------------------

    @Test
    fun `registrar con campos invalidos responde 400 con detalle por campo`() {
        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody(firstName = "  ")))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Solicitud inválida"))
            .andExpect(jsonPath("$.detail").value("Uno o más campos no cumplen las validaciones"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("firstName"))
            .andExpect(jsonPath("$.errors[0].message").value("El nombre es obligatorio"))
    }

    @Test
    fun `enviar json malformado responde 400`() {
        mockMvc.perform(post("/api/v1/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{ json no valido"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Solicitud ilegible"))
            .andExpect(jsonPath("$.detail").value("El cuerpo de la solicitud no es un JSON válido"))
    }

    @Test
    fun `obtener con id mal formado responde 400`() {
        mockMvc.perform(get("/api/v1/students/no-es-un-uuid"))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Parámetro inválido"))
            .andExpect(jsonPath("$.detail").value("El valor 'no-es-un-uuid' no es válido para el parámetro 'id'"))
    }

    @Test
    fun `recurso inexistente responde 404`() {
        mockMvc.perform(get("/api/v1/recurso-inexistente"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
            .andExpect(jsonPath("$.detail", containsString("recurso-inexistente")))
    }

    // ------------------------------------------------------------------
    // Errores inesperados y protocolo
    // ------------------------------------------------------------------

    @Test
    fun `error inesperado responde 500 sin exponer detalles internos`() {
        val id = seedStudent()
        studentRepository.explodeOnIds.add(id)

        mockMvc.perform(get("/api/v1/students/$id"))
            .andExpect(status().isInternalServerError())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Error interno"))
            .andExpect(jsonPath("$.detail").value("Ocurrió un error inesperado"))
    }

    @Test
    fun `metodo no soportado responde 405 con problem detail`() {
        val id = seedStudent()

        mockMvc.perform(delete("/api/v1/students/$id"))
            .andExpect(status().isMethodNotAllowed())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.status").value(405))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private fun seedStudent(
        dni: String = "38123456",
        mutate: Student.() -> Unit = {}
    ): UUID {
        val student = Student(
            id = StudentId.generate(),
            personalInfo = PersonalInfo(
                firstName = "Lucas",
                lastName = "Benítez",
                identificationNumber = dni,
                birthDate = LocalDate.of(1995, 5, 20),
                phone = "3815551234",
                emergencyContactName = "María González",
                emergencyContactPhone = "3815559876"
            )
        ).apply(mutate)

        studentRepository.save(student)
        return student.id.value
    }

    private fun registerBody(
        firstName: String = "Lucas",
        dni: String = "38123456",
        birthDate: String = "1995-05-20"
    ): String = """
        {
          "firstName": "$firstName",
          "lastName": "Benítez",
          "identificationNumber": "$dni",
          "birthDate": "$birthDate",
          "phone": "3815551234",
          "emergencyContactName": "María González",
          "emergencyContactPhone": "3815559876"
        }
    """.trimIndent()

    private fun promotionBody(
        type: String = "KYU",
        level: Int = 7,
        color: String = "YELLOW",
        orderIndex: Int = 2
    ): String = """
        {
          "targetRank": { "type": "$type", "level": $level, "color": "$color", "orderIndex": $orderIndex },
          "promotionDate": "2026-01-15",
          "examiner": "Sensei García"
        }
    """.trimIndent()
}

/**
 * Repositorio in-memory para los tests web: sin Mockito y con dominio real.
 * `explodeOnIds` permite simular fallos inesperados de infraestructura.
 */
class InMemoryStudentRepository : StudentRepository {

    val students = LinkedHashMap<StudentId, Student>()
    val explodeOnIds = mutableSetOf<UUID>()

    fun reset() {
        students.clear()
        explodeOnIds.clear()
    }

    override fun save(student: Student): Student {
        students[student.id] = student
        return student
    }

    override fun findById(id: StudentId): Student? {
        if (id.value in explodeOnIds) {
            throw RuntimeException("fallo inesperado simulado")
        }
        return students[id]
    }

    override fun findByIdentificationNumber(dni: String): Student? =
        students.values.firstOrNull { it.personalInfo.identificationNumber == dni }
}
