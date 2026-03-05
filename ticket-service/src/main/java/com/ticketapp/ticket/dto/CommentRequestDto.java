package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.CommentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
//Kullanıcıdan yorumu alırken kullan
public class CommentRequestDto {
    private String comment;
    private CommentType type;
}
