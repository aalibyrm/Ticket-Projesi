package com.ticketapp.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Tüm mikroservislerin ortak exception handler'ı.
 * <p>
 * ticket-common jar'ı her servise bağımlılık olarak eklendiğinde,
 * Spring Boot auto-configuration bu sınıfı paket taraması ile bulacaktır.
 * Bunun için servislerin component scan base-package'ı "com.ticketapp" içermeli
 * (zaten tüm servisler com.ticketapp.* altında).
 * <p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * ResourceNotFoundException ve tüm alt sınıfları (TicketNotFoundException,
         * DepartmentNotFoundException, UserNotFoundException vb.) → 404
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
                log.warn("{}: {}", ex.getCode(), ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
        }

        /**
         * UnauthorizedAccessException → 403
         */
        @ExceptionHandler(UnauthorizedAccessException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedAccessException ex) {
                log.warn("Yetkisiz erisim [{}]: {}", ex.getCode(), ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
        }

        /**
         * BusinessRuleException ve tüm alt sınıfları
         * (InvalidTicketStateException, DuplicateResourceException vb.) → 400
         */
        @ExceptionHandler(BusinessRuleException.class)
        public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex) {
                log.warn("Is kurali ihlali [{}]: {}", ex.getCode(), ex.getMessage());
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(ex.getCode(), ex.getMessage()));
        }

        /**
         * @Valid / @Validated bean validation hataları → 400
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
                String errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                log.warn("Validasyon hatasi: {}", errors);
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of("VALIDATION_ERROR", errors));
        }

        /**
         * Yakalanmayan tüm diğer exception'lar → 500
         * Detay mesajı client'a ASLA sızdırılmamalı (bilgi güvenliği).
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
                log.error("Beklenmedik hata: {}", ex.getMessage(), ex);
                return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(
                                                "INTERNAL_SERVER_ERROR",
                                                "Beklenmedik bir hata olustu. Lutfen daha sonra tekrar deneyin."));
        }
}
