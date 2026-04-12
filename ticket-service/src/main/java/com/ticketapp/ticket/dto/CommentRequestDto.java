package com.ticketapp.ticket.dto;

import com.ticketapp.ticket.model.CommentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDto {
    @NotBlank
    private String comment;
    @NotNull
    private CommentType type;
}
