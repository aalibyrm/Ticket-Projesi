package com.ticketapp.support.exception;

import com.ticketapp.common.exception.ResourceNotFoundException;

/**
 * Belirtilen ID veya isme göre team bulunamadığında fırlatılır.
 * GlobalExceptionHandler (common) → HTTP 404 döner.
 */
public class TeamNotFoundException extends ResourceNotFoundException {

    public TeamNotFoundException(String identifier) {
        super("Team", identifier);
    }
}
