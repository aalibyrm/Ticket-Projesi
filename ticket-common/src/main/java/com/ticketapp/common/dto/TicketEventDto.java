package com.ticketapp.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Kafka uzerinden ticket-service → notification-service arasinda paylasilan event DTO'su.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketEventDto {
    private String ticketId;
    private String status;
    private String userId;
    private String message;
}
