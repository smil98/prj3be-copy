package com.example.prj3be.controller;

import com.example.prj3be.domain.Comment;
import com.example.prj3be.domain.Member;
import com.example.prj3be.dto.CommentFormDto;
import com.example.prj3be.service.CommentService;
import com.example.prj3be.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("list")
    public Page<Comment> commentList(@RequestParam("id") Long boardId, Pageable pageable) {
        Page<Comment> commentList = commentService.commentListAll(boardId, pageable);
        return commentList;
    }


    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SOCIAL')")
    @PostMapping("add/{boardId}")
    public void save(@PathVariable("boardId") String boardId, @RequestBody CommentFormDto content) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member memberByEmail = commentService.findMemberByEmail(authentication.getName());
        System.out.println("memberByEmail = " + memberByEmail.getEmail());
        System.out.println("authentication = " + authentication.getName());
        commentService.write(boardId, content, memberByEmail);
    }

    @PreAuthorize("#updateComment.member.email == authentication.name")
    @PutMapping("update/{id}")
    public Comment update(@PathVariable Long id, @RequestBody Comment updateComment) {
        Comment editComment = commentService.update(id, updateComment);
        return editComment;
    }

    @DeleteMapping("delete/{id}")
    public void delete(@PathVariable Long id) {
        String email = commentService.findMemberById(id);
        commentService.delete(id, email);
    }
}
