package com.ticketapp.ticket.interfaces;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentMapper {
    Comment commentDto(CommentRequestDto commentRequestDto);

    CommentResponseDto commentResponseDto(Comment comment);

    List<CommentResponseDto> toDoList(List<Comment> comment);
}
