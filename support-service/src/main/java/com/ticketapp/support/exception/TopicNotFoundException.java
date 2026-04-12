package com.ticketapp.support.exception;

import com.ticketapp.common.exception.ResourceNotFoundException;

/**
 * Belirtilen ID'ye göre topic bulunamadığında fırlatılır.
 * GlobalExceptionHandler (common) → HTTP 404 döner.
 */
public class TopicNotFoundException extends ResourceNotFoundException {

    public TopicNotFoundException(String identifier) {
        super("Topic", identifier);
    }
}
