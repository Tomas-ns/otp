package pt.isel.otp.http.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
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
}
