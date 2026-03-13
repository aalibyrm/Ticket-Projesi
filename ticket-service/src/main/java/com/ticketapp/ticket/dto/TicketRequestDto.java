package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketRequestDto {
    private String title;
    private String description;
    private Long topicId;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDateTime createdDate;
    private String userId;
}
