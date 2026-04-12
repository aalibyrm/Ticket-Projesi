package com.ticketapp.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String displayName;
}
