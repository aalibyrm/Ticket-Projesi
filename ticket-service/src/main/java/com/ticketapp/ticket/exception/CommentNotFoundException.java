package com.ticketapp.ticket.exception;

import com.ticketapp.common.exception.ResourceNotFoundException;

/**
 * Belirtilen ID'ye sahip comment bulunamadığında fırlatılır.
 * GlobalExceptionHandler (common) → HTTP 404 döner.
 */
public class CommentNotFoundException extends ResourceNotFoundException {

    public CommentNotFoundException(String commentId) {
        super("Comment", commentId);
    }
}
