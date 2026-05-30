package es.miw.tfm.linkal.infrastructure.resources.httperrors;

import es.miw.tfm.linkal.domain.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
public class ApiExceptionHandler {

    @ResponseBody
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error(">>> ERROR 400 HttpMessageNotReadable: {}", ex.getMessage());
        return new ErrorMessage(ex.getClass().getSimpleName(),
                "Body inválido o mal formateado: " + ex.getMostSpecificCause().getMessage(),
                HttpStatus.BAD_REQUEST.value());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleValidationException(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .reduce((a, b) -> a + ", " + b)
                .orElse(ex.getMessage());
        return new ErrorMessage(ex.getClass().getSimpleName(), detail, HttpStatus.BAD_REQUEST.value());
    }
    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(NotFoundException ex) {
        return new ErrorMessage(ex, HttpStatus.NOT_FOUND.value());
    }

    @ResponseBody
    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleBadRequestException(BadRequestException ex) {
        return new ErrorMessage(ex, HttpStatus.BAD_REQUEST.value());
    }

    @ResponseBody
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleConflictException(ConflictException ex) {
        return new ErrorMessage(ex, HttpStatus.CONFLICT.value());
    }

    @ResponseBody
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleForbiddenException(ForbiddenException ex) {
        return new ErrorMessage(ex, HttpStatus.FORBIDDEN.value());
    }

    @ResponseBody
    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleAuthorizationDeniedException(AuthorizationDeniedException ex) {
        return new ErrorMessage(ex, HttpStatus.FORBIDDEN.value());
    }

    @ResponseBody
    @ExceptionHandler(BadGatewayException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ErrorMessage handleBadGatewayException(BadGatewayException ex) {
        return new ErrorMessage(ex, HttpStatus.BAD_GATEWAY.value());
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return new ErrorMessage(ex.getClass().getSimpleName(),
                "Parámetro inválido: " + ex.getName(), HttpStatus.BAD_REQUEST.value());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleException(Exception ex) {
        log.error(">>> ERROR 500: {}: {}", ex.getClass().getName(), ex.getMessage(), ex);
        return new ErrorMessage(ex, HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
