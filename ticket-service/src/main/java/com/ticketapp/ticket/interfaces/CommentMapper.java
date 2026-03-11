package com.ticketapp.ticket.interfaces;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.model.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment commentDto(CommentRequestDto commentRequestDto);
    CommentResponseDto commentResponseDto(Comment comment);
    List<CommentResponseDto> toDoList (List<Comment> comment);
}
