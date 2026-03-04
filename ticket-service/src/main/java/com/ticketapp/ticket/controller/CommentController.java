package com.ticketapp.ticket.controller;

import com.ticketapp.ticket.model.Comment;
import com.ticketapp.ticket.model.CommentType;
import com.ticketapp.ticket.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/ticket/{id}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;

    @PostMapping("create-comment")
    public Comment createComment(@RequestBody Comment comment, @AuthenticationPrincipal Jwt jwt){

        String userId = jwt.getSubject();
        comment.setUserId(userId);
        comment.setCreatedDate(LocalDateTime.now());

        var role = jwt.getClaimAsMap("realm_access").get("roles").toString();

        if(role.contains("CUSTOMER")){
            comment.setType(CommentType.EXTERNAL);
        } else {
            //Burda yapmak istediğim yorum gönderen kişi customersa kesinlikle external ata
            // destek ekibindense onların tercihi neyse ona göre atama yap
            if(comment.getType() == CommentType.INTERNAL)
            {
                comment.setType(CommentType.INTERNAL);
            } else {
                comment.setType(CommentType.EXTERNAL);
            }
        }

        Comment savedComment = commentRepository.save(comment);

        return savedComment;
    };


}
