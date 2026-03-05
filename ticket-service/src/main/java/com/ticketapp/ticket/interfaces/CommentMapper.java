package com.ticketapp.ticket.interfaces;

import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.model.Comment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    CommentResponseDto commentDto(Comment comment);
    List<CommentResponseDto> toDoList (List<Comment> comment);
}
