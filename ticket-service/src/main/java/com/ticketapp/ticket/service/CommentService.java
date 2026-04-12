package com.ticketapp.ticket.service;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.exception.CommentNotFoundException;
import com.ticketapp.ticket.exception.TicketNotFoundException;
import com.ticketapp.ticket.exception.UnauthorizedAccessException;
import com.ticketapp.ticket.interfaces.CommentMapper;
import com.ticketapp.ticket.model.Comment;
import com.ticketapp.ticket.model.CommentType;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.repository.CommentRepository;
import com.ticketapp.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final CommentMapper commentMapper;

    public CommentResponseDto createComment(String ticketId, CommentRequestDto request, String userId) {
        // Musteri icin kendisinin olmayan ticketa yorum atma kontrolu eklenecek
        if (ticketRepository.findById(ticketId).isEmpty()) {
            throw new TicketNotFoundException(ticketId);
        }

        Comment comment = commentMapper.commentDto(request);
        Comment savedComment = commentRepository.save(comment);

        log.debug("Yorum olusturuldu: ticketId={}, userId={}", ticketId, userId);
        return commentMapper.commentResponseDto(savedComment);
    }

    public List<CommentResponseDto> getCommentsByTicketId(String ticketId, String role) {

        List<Comment> byTicketId = commentRepository.findByTicket_Id(ticketId);

        if (role.contains("CUSTOMER")) {
            // Musteri sadece external yorumlari gorur
            List<Comment> externalComments = byTicketId.stream()
                    .filter(c -> c.getType() == CommentType.EXTERNAL)
                    .toList();
            return commentMapper.toDoList(externalComments);
        } else {
            // Destek ekibi tum yorumlari gorur
            return commentMapper.toDoList(byTicketId);
        }
    }

    public CommentResponseDto editComment(String ticketId, String commentId, String userId, CommentRequestDto request,
            String role, boolean changeType) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        List<Comment> commentList = new ArrayList<>(ticket.getComments());

        Comment editedComment = commentList.stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        // Comment sahibi mi duzenlemeye calisiyor kontrolu
        if (!editedComment.getUserId().equals(userId))
            throw new UnauthorizedAccessException("Comment duzenlemeye yetkiniz yok!");

        editedComment.setComment(request.getComment());

        // Eger istek atan kisi destek ekibindense ve comment type degistirmek istiyorsa
        if (!role.contains("CUSTOMER") && changeType) {
            // Internal External degisimi
            if (editedComment.getType().equals(CommentType.INTERNAL)) {
                editedComment.setType(CommentType.EXTERNAL);
            } else {
                editedComment.setType(CommentType.INTERNAL);
            }
        }

        Comment savedComment = commentRepository.save(editedComment);
        log.debug("Yorum duzenlendi: commentId={}, userId={}", commentId, userId);

        return commentMapper.commentResponseDto(savedComment);
    }
}
