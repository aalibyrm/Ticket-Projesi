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
import java.util.ArrayList;
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

        Comment comment = commentMapper.commentDto(request);
        Comment savedComment = commentRepository.save(comment);

        return commentMapper.commentResponseDto(savedComment);
    }

    public List<CommentResponseDto> getCommentsByTicketId(String ticketId, String role) {

        List<Comment> byTicketId = commentRepository.findByTicket_Id(ticketId);

        if (role.contains("CUSTOMER")) {
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

    public CommentResponseDto editComment(String ticketId, String commentId, String userId, CommentRequestDto request, String role, boolean changeType) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket bulunmadı"));

        List<Comment> commentList = new ArrayList<>(ticket.getComments());

        //Ticketta istenilen commenti çekmeye çalıştım
        Comment editedComment = commentList.stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst().orElseThrow(() -> new RuntimeException("Comment bulunmadı"));

        //Comment sahibi mi düzenlemeye çalışıyor kontrolü
        if (!editedComment.getUserId().equals(userId))
            throw new RuntimeException("Comment düzenlemeye yetkiniz yok!");

        editedComment.setComment(request.getComment());

        //Eğer istek atan kişi destek ekibidindense ve comment type değiştirmek istiyorsa
        if (!role.contains("CUSTOMER") && changeType) {

            //Internal External değişimi
            if (editedComment.getType().equals(CommentType.INTERNAL)) {
                editedComment.setType(CommentType.EXTERNAL);
            } else {
                editedComment.setType(CommentType.INTERNAL);
            }
        }

        Comment savedComment = commentRepository.save(editedComment);

        return commentMapper.commentResponseDto(savedComment);
    }


}
