package com.ticketapp.support.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String departmentName;
}
