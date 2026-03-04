package com.ticketapp.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketEventDto {
    private String ticketId;
    private String status;
    private String userId;
    private String message;
}