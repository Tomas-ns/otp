package pt.isel.otp.http.exception

import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.util.NoSuchElementException

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleIllegalArgumentException returns 400`() {
        val exception = IllegalArgumentException("Invalid argument")
        val response = handler.handleIllegalArgumentException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Invalid argument", response.body?.message)
    }

    @Test
    fun `handleIllegalArgumentException with null message`() {
        val exception = IllegalArgumentException()
        val response = handler.handleIllegalArgumentException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Bad request", response.body?.message)
    }

    @Test
    fun `handleIllegalStateException returns 401`() {
        val exception = IllegalStateException("Not allowed")
        val response = handler.handleIllegalStateException(exception)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Not allowed", response.body?.message)
    }

    @Test
    fun `handleIllegalStateException with null message`() {
        val exception = IllegalStateException()
        val response = handler.handleIllegalStateException(exception)
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals("Unauthorized", response.body?.message)
    }

    @Test
    fun `handleNoSuchElementException returns 404`() {
        val exception = NoSuchElementException("Not found")
        val response = handler.handleNoSuchElementException(exception)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Not found", response.body?.message)
    }

    @Test
    fun `handleNoSuchElementException with null message`() {
        val exception = NoSuchElementException()
        val response = handler.handleNoSuchElementException(exception)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals("Not found", response.body?.message)
    }

    @Test
    fun `handleConstraintViolationException returns 400`() {
        val violations = setOf(
            TestViolation("field1", "must not be null"),
        )
        val exception = ConstraintViolationException("validation failed", violations)
        val response = handler.handleConstraintViolationException(exception)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals("Request validation failed", response.body?.message)
        assertEquals("must not be null", response.body?.errors?.get("field1"))
    }

    @Test
    fun `handleConstraintViolationException with multiple violations`() {
        val violations = setOf(
            TestViolation("field1", "must not be null"),
            TestViolation("field2", "must be positive"),
        )
        val exception = ConstraintViolationException("validation failed", violations)
        val response = handler.handleConstraintViolationException(exception)
        assertEquals(2, response.body?.errors?.size)
    }
}

class TestViolation(
    private val field: String,
    private val msg: String,
) : jakarta.validation.ConstraintViolation<Any> {
    override fun getMessage() = msg
    override fun getMessageTemplate() = msg
    override fun getRootBean() = Any()
    override fun getRootBeanClass() = Any::class.java
    override fun getLeafBean() = null
    override fun getPropertyPath(): jakarta.validation.Path = TestPath(field)
    override fun getInvalidValue() = null
    override fun getConstraintDescriptor() = null
    override fun <U : Any> unwrap(configuration: Class<U>): U? = null
    override fun getExecutableParameters() = null
    override fun getExecutableReturnValue() = null
}

class TestPath(private val field: String) : jakarta.validation.Path {
    override fun toString() = field
    override fun iterator() = mutableListOf<jakarta.validation.Path.Node>().iterator()
}
