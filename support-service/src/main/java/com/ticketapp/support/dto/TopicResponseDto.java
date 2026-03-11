package com.ticketapp.support.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicResponseDto {
    private Long id;
    private String name;
    private String displayName;
}
