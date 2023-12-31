package com.example.prj3be.controller;

import com.example.prj3be.domain.Likes;
import com.example.prj3be.dto.LikesDto;
import com.example.prj3be.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService service;

    @PreAuthorize("hasAnyRole('ADMIN', 'USER', 'SOCIAL')")
    @GetMapping("/update/{boardId}")
    public ResponseEntity<Map<String, Object>> like(@PathVariable Long boardId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(service.updateLike(boardId, email));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<Map<String, Object>> get(@PathVariable Long boardId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean isLiked = service.isLiked(boardId, email);
        int likeCount = service.getLikeCount(boardId);
        return ResponseEntity.ok(Map.of("isLiked", isLiked, "countLike", likeCount));
    }

    @GetMapping("/list/{id}")
    public Page<LikesDto> fetchLiked(Pageable pageable,
                                     @PathVariable Long id,
                                     @RequestParam(value = "k", defaultValue = "") String keyword,
                                     @RequestParam(value = "c", defaultValue = "all") String category,
    @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("받은 파라미터: keyword=" + keyword + ", category=" + category + ", memberID=" + id);

        // 관리자라면 어떤 id이든 바로 승인
        if(authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
            Page<Likes> likesPage = service.getAllLikes(pageable, keyword, category, id);
            return likesPage.map(LikesDto::new);
        } else {
            //관리자가 아니라면 본인인지 확인 후 진행
            boolean authenticated = service.checkForUser(id, email);
            if(authenticated) {
                Page<Likes> likesPage = service.getAllLikes(pageable, keyword, category, id);
                return likesPage.map(LikesDto::new);
            } else {
                return null;
            }
        }
    }
}
