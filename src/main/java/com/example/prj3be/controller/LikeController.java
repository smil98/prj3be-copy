package com.example.prj3be.controller;

import com.example.prj3be.domain.Likes;
import com.example.prj3be.domain.Member;
import com.example.prj3be.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService service;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SOCIAL')")
    @GetMapping("update/{boardId}")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long boardId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        System.out.println("id = " + boardId);

        return ResponseEntity.ok(service.updateLike(boardId, email));
    }

    @GetMapping("board/{boardId}")
    public ResponseEntity<Map<String, Object>>get(@PathVariable Long boardId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isLiked = service.isLiked(boardId, email);
        int likeCount = service.getLikeCount(boardId);
        return ResponseEntity.ok(Map.of("isLiked", isLiked, "countLike", likeCount));
    }
}
