package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.dto.CommentRequestDto;
import com.ticketapp.ticket.dto.CommentResponseDto;
import com.ticketapp.ticket.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // Kullanıcı yorum oluşturma methodu
    @PostMapping("/{id}/comments")
    public CommentResponseDto createComment(@PathVariable String id, @RequestBody CommentRequestDto request, @AuthenticationPrincipal Jwt jwt) {

        return commentService.createComment(id,request, jwt.getSubject());
    }

}
