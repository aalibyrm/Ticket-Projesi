package com.ticketapp.ticket.interfaces;

import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.dto.TicketDetailDto;
import com.ticketapp.ticket.dto.TicketRequestDto;
import com.ticketapp.ticket.dto.TicketResponseDto;
import com.ticketapp.ticket.model.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketMapper {
    @Mapping(source = "comments", target = "comments")
    TicketDetailDto toTicketDetailDto(Ticket ticket, List<CommentResponseDto> comments);

    Ticket ticketDto(TicketRequestDto requestDto);

    TicketResponseDto toTicketResponseDto(Ticket ticket);

    List<TicketResponseDto> toTicketResponseDtoList(List<Ticket> ticketList);

}
