package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/{ticketId}/comments")
    public List<CommentResponseDto> getComments(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();
        return commentService.getCommentsByTicketId(ticketId, role);
    }

    // Kullanıcı yorum oluşturma methodu
    @PostMapping("/{ticketId}/create-comment")
    public CommentResponseDto createComment(@PathVariable String ticketId,
                                            @Valid @RequestBody CommentRequestDto request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return commentService.createComment(ticketId, request, jwt.getSubject());
    }

    @PatchMapping("/{ticketId}/comments/{commentId}")
    public CommentResponseDto editComment(@PathVariable String ticketId,
                                          @PathVariable String commentId,
                                          @AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody CommentRequestDto request,
                                          @RequestParam boolean changeType) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();
        return commentService.editComment(ticketId, commentId, jwt.getSubject(), request, role, changeType);
    }
}
