package com.ticketapp.support.exception;

import com.ticketapp.common.exception.ResourceNotFoundException;

/**
 * Belirtilen ID veya koşula göre department bulunamadığında fırlatılır.
 * GlobalExceptionHandler (common) → HTTP 404 döner.
 */
public class DepartmentNotFoundException extends ResourceNotFoundException {

    public DepartmentNotFoundException(String identifier) {
        super("Department", identifier);
    }
}
