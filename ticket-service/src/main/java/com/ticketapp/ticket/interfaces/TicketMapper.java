package com.ticketapp.ticket.interfaces;

import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.dto.TicketDetailDto;
import com.ticketapp.ticket.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    @Mapping(source = "comments", target = "comments")
    TicketDetailDto toTicketDetailDto (Ticket ticket, List<CommentResponseDto> comments);
}
