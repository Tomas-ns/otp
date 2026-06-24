package pt.isel.otp.http.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import pt.isel.otp.http.dto.ErrorResponse
import pt.isel.otp.http.dto.ValidationErrorResponse

@RestControllerAdvice(basePackages = ["pt.isel.otp.http.controller"])
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        exception: MethodArgumentNotValidException,
    ): ResponseEntity<ValidationErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ValidationErrorResponse(
                message = "Request validation failed",
                errors =
                    exception.bindingResult.fieldErrors.associate { fieldError ->
                        fieldError.field to (fieldError.defaultMessage ?: "invalid value")
                    },
            ),
        )

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        exception: ConstraintViolationException,
    ): ResponseEntity<ValidationErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ValidationErrorResponse(
                message = "Request validation failed",
                errors =
                    exception.constraintViolations.associate { violation ->
                        violation.propertyPath.toString() to (violation.message ?: "invalid value")
                    },
            ),
        )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        exception: IllegalArgumentException,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(message = exception.message ?: "Bad request"),
        )

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        exception: IllegalStateException,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(message = exception.message ?: "Unauthorized"),
        )

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNoSuchElementException(
        exception: NoSuchElementException,
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse(message = exception.message ?: "Not found"),
        )
}
