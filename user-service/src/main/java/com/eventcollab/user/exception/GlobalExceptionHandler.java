package com.eventcollab.user.exception;

import com.eventcollab.common.dto.ApiError;
import com.eventcollab.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).toList();
        return ResponseEntity.badRequest().body(ApiError.builder()
                .status(400).error("VALIDATION_FAILED")
                .message("Un ou plusieurs champs sont invalides")
                .details(details).build());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        log.warn("[{}] {}", ex.getCode(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiError.builder()
                .status(ex.getStatus().value()).error(ex.getCode())
                .message(ex.getMessage()).build());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiError.builder()
                .status(401).error("BAD_CREDENTIALS")
                .message("Email ou mot de passe incorrect").build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        log.error("Erreur non geree", ex);
        return ResponseEntity.internalServerError().body(ApiError.builder()
                .status(500).error("INTERNAL_ERROR")
                .message("Une erreur inattendue s est produite").build());
    }
}