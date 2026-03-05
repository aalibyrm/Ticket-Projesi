package com.ticketapp.ticket.dto;

import java.time.LocalDateTime;

import com.ticketapp.ticket.model.CommentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//Kullanıcıya yorum döndürürken kullan
public class CommentResponseDto {
    private String id;
    private String comment;
    private CommentType type;
    private LocalDateTime createdDate;
}
