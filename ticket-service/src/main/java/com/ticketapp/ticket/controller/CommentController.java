package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Yorum Yönetimi", description = "Ticket üzerindeki yorum oluşturma, listeleme ve düzenleme işlemleri")
@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "Yorumları listele", description = "Bir ticketa ait tüm yorumları döner.")
    @GetMapping("/{ticketId}/comments")
    public List<CommentResponseDto> getComments(@PathVariable String ticketId, @AuthenticationPrincipal Jwt jwt) {
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();
        return commentService.getCommentsByTicketId(ticketId, role);
    }

    @Operation(summary = "Yorum ekle", description = "Belirtilen ticket'a yeni bir yorum ekler.")
    @PostMapping("/{ticketId}/create-comment")
    public CommentResponseDto createComment(@PathVariable String ticketId,
                                            @Valid @RequestBody CommentRequestDto request,
                                            @AuthenticationPrincipal Jwt jwt) {
        return commentService.createComment(ticketId, request, jwt.getSubject());
    }

    @Operation(summary = "Yorum düzenle", description = "Mevcut yorumu günceller. changeType=true ile yorum tipi de değiştirilebilir.")
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
