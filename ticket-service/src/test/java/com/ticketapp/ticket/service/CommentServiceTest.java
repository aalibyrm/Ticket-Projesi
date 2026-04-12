package com.ticketapp.ticket.service;

import com.ticketapp.common.exception.UnauthorizedAccessException;
import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.exception.CommentNotFoundException;
import com.ticketapp.ticket.exception.TicketNotFoundException;
import com.ticketapp.ticket.mapper.CommentMapper;
import com.ticketapp.ticket.model.Comment;
import com.ticketapp.ticket.model.CommentType;
import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.repository.CommentRepository;
import com.ticketapp.ticket.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * CommentService birim testleri.
 *
 * Test kapsamı:
 *  - createComment: mevcut / mevcut olmayan ticket kontrolü
 *  - getCommentsByTicketId: CUSTOMER sadece EXTERNAL görür
 *  - editComment: yetki, tip değişimi
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock CommentRepository commentRepository;
    @Mock TicketRepository ticketRepository;
    @Mock CommentMapper commentMapper;

    @InjectMocks
    CommentService commentService;

    // ── createComment ─────────────────────────────────────────────────────────

    @Test
    void createComment_whenTicketExists_shouldSaveAndReturnDto() {
        String ticketId = "ticket-id";
        String userId = "user-123";
        CommentRequestDto request = new CommentRequestDto("İyi çalışma!", CommentType.EXTERNAL);

        Comment comment = new Comment();
        comment.setId("comment-id");
        CommentResponseDto expected = new CommentResponseDto("comment-id", "İyi çalışma!", CommentType.EXTERNAL, null);

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(new Ticket()));
        given(commentMapper.commentDto(request)).willReturn(comment);
        given(commentRepository.save(comment)).willReturn(comment);
        given(commentMapper.commentResponseDto(comment)).willReturn(expected);

        CommentResponseDto result = commentService.createComment(ticketId, request, userId);

        assertThat(result.getId()).isEqualTo("comment-id");
        verify(commentRepository).save(comment);
    }

    @Test
    void createComment_whenTicketNotFound_shouldThrowTicketNotFoundException() {
        given(ticketRepository.findById("ghost-ticket")).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.createComment("ghost-ticket", new CommentRequestDto("test", CommentType.EXTERNAL), "user"))
                .isInstanceOf(TicketNotFoundException.class);
    }

    // ── getCommentsByTicketId ──────────────────────────────────────────────────

    @Test
    void getCommentsByTicketId_whenCustomer_shouldReturnOnlyExternalComments() {
        String ticketId = "ticket-id";

        Comment external = new Comment();
        external.setType(CommentType.EXTERNAL);
        Comment internal = new Comment();
        internal.setType(CommentType.INTERNAL);

        // Repoda hem internal hem external yorum var
        given(commentRepository.findByTicket_Id(ticketId))
                .willReturn(List.of(external, internal));
        given(commentMapper.toDoList(List.of(external)))
                .willReturn(List.of(new CommentResponseDto("1", "ext", CommentType.EXTERNAL, null)));

        List<CommentResponseDto> result = commentService.getCommentsByTicketId(ticketId, "[CUSTOMER]");

        // Müşteriye sadece EXTERNAL dönmeli
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(CommentType.EXTERNAL);
    }

    @Test
    void getCommentsByTicketId_whenAgent_shouldReturnAllComments() {
        String ticketId = "ticket-id";

        Comment external = new Comment();
        external.setType(CommentType.EXTERNAL);
        Comment internal = new Comment();
        internal.setType(CommentType.INTERNAL);

        List<Comment> all = List.of(external, internal);
        given(commentRepository.findByTicket_Id(ticketId)).willReturn(all);
        given(commentMapper.toDoList(all)).willReturn(
                List.of(
                        new CommentResponseDto("1", "ext", CommentType.EXTERNAL, null),
                        new CommentResponseDto("2", "int", CommentType.INTERNAL, null)
                ));

        List<CommentResponseDto> result = commentService.getCommentsByTicketId(ticketId, "[AGENT]");

        assertThat(result).hasSize(2);
    }

    // ── editComment ───────────────────────────────────────────────────────────

    @Test
    void editComment_whenUserIsOwner_shouldUpdateComment() {
        String ticketId = "ticket-id";
        String commentId = "comment-id";
        String userId = "user-123";
        CommentRequestDto request = new CommentRequestDto("Güncellenmiş yorum", CommentType.EXTERNAL);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(userId);
        comment.setComment("Eski yorum");
        comment.setType(CommentType.EXTERNAL);

        Ticket ticket = new Ticket();
        ticket.setComments(new ArrayList<>(List.of(comment)));

        given(ticketRepository.findById(ticketId)).willReturn(Optional.of(ticket));
        given(commentRepository.save(comment)).willReturn(comment);
        given(commentMapper.commentResponseDto(comment)).willReturn(
                new CommentResponseDto(commentId, "Güncellenmiş yorum", CommentType.EXTERNAL, null));

        CommentResponseDto result = commentService.editComment(ticketId, commentId, userId, request, "[AGENT]", false);

        assertThat(result.getComment()).isEqualTo("Güncellenmiş yorum");
        verify(commentRepository).save(comment);
    }

    @Test
    void editComment_whenUserIsNotOwner_shouldThrowUnauthorized() {
        String commentId = "comment-id";

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUserId("real-owner");

        Ticket ticket = new Ticket();
        ticket.setComments(new ArrayList<>(List.of(comment)));

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                commentService.editComment("ticket-id", commentId, "wrong-user",
                        new CommentRequestDto("metin", CommentType.EXTERNAL), "[AGENT]", false))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void editComment_whenCommentNotFound_shouldThrowCommentNotFoundException() {
        Ticket ticket = new Ticket();
        ticket.setComments(new ArrayList<>());   // boş liste

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));

        assertThatThrownBy(() ->
                commentService.editComment("ticket-id", "nonexistent-comment", "user",
                        new CommentRequestDto("metin", CommentType.EXTERNAL), "[AGENT]", false))
                .isInstanceOf(CommentNotFoundException.class);
    }

    @Test
    void editComment_whenAgentRequestsTypeChange_shouldToggleCommentType() {
        String commentId = "comment-id";
        String userId = "agent-123";

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(userId);
        comment.setComment("Yorum");
        comment.setType(CommentType.INTERNAL);   // INTERNAL → EXTERNAL'a dönmeli

        Ticket ticket = new Ticket();
        ticket.setComments(new ArrayList<>(List.of(comment)));

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));
        given(commentRepository.save(comment)).willReturn(comment);
        given(commentMapper.commentResponseDto(comment)).willReturn(new CommentResponseDto());

        commentService.editComment("ticket-id", commentId, userId,
                new CommentRequestDto("Yorum", CommentType.INTERNAL), "[AGENT]", true);

        // changeType=true → tip değişmeli
        assertThat(comment.getType()).isEqualTo(CommentType.EXTERNAL);
    }

    @Test
    void editComment_whenCustomerRequestsTypeChange_shouldNotChangeType() {
        String commentId = "comment-id";
        String userId = "customer-123";

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setUserId(userId);
        comment.setComment("Yorum");
        comment.setType(CommentType.EXTERNAL);

        Ticket ticket = new Ticket();
        ticket.setComments(new ArrayList<>(List.of(comment)));

        given(ticketRepository.findById("ticket-id")).willReturn(Optional.of(ticket));
        given(commentRepository.save(comment)).willReturn(comment);
        given(commentMapper.commentResponseDto(comment)).willReturn(new CommentResponseDto());

        // Müşteri changeType=true istese de değişmemeli
        commentService.editComment("ticket-id", commentId, userId,
                new CommentRequestDto("Yorum", CommentType.EXTERNAL), "[CUSTOMER]", true);

        assertThat(comment.getType()).isEqualTo(CommentType.EXTERNAL);
    }
}
