package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.CommentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private String id;
    private String comment;
    private CommentType type;
    private String userId;
    private String ticketId;
    private LocalDateTime createdDate;

}
