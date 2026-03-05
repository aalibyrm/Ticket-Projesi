package com.ticketapp.ticket.service;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.interfaces.CommentMapper;
import com.ticketapp.ticket.model.Comment;
import com.ticketapp.ticket.model.CommentType;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.repository.CommentRepository;
import com.ticketapp.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final CommentMapper commentMapper;

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
                LocalDateTime.now()
        );
    }

    public List<CommentResponseDto> getCommentsByTicketId(String ticketId, String role) {

        //Kontrol eklencek
        List<Comment> byTicketId = commentRepository.findByTicket_Id(ticketId);

        if(role.contains("CUSTOMER")){
            //Müşteri sadece external yorumları görür
            List<Comment> externalComments = byTicketId.stream()
                    .filter(c -> c.getType() == CommentType.EXTERNAL)
                    .toList();
            return commentMapper.toDoList(externalComments);
        } else {
            //Destek ekibi tüm yorumları görür
            return commentMapper.toDoList(byTicketId);
        }
    }
}
