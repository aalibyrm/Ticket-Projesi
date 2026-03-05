package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.service.CommentService;
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

    // Kullanıcı yorum oluşturma methodu
    @PostMapping("/{ticketId}/comments")
    public CommentResponseDto createComment(@PathVariable String ticketId, @RequestBody CommentRequestDto request,@AuthenticationPrincipal Jwt jwt) {

        return commentService.createComment(ticketId,request, jwt.getSubject());
    }

    //Role göre tüm yorumları listeleme
    @GetMapping("/{ticketId}/comments")
    public List<CommentResponseDto> getCommentsByTicketId(@PathVariable String ticketId,@AuthenticationPrincipal Jwt jwt){
        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

        return commentService.getCommentsByTicketId(ticketId,role);
    }

}
