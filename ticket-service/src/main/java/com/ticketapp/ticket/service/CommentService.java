package com.ticketapp.ticket.service;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.model.Comment;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.repository.CommentRepository;
import com.ticketapp.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public CommentResponseDto createComment(String ticketId, CommentRequestDto request, String userId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunamadı"));

        Comment comment = new Comment();
        comment.setComment(request.getComment());
        comment.setType(request.getType());
        comment.setUserId(userId);
        comment.setTicket(ticket);
        comment.setCreatedDate(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDto(
                savedComment.getId(),
                savedComment.getComment(),
                savedComment.getType(),
                userId,
                ticketId,
                LocalDateTime.now()
        );
    }
}
