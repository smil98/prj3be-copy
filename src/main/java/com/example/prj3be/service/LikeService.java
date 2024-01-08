package com.example.prj3be.service;

import com.example.prj3be.domain.Board;
import com.example.prj3be.domain.Likes;
import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.QLikes;
import com.example.prj3be.repository.BoardRepository;
import com.example.prj3be.repository.LikeRepository;
import com.example.prj3be.repository.MemberRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    public boolean isLiked(Long boardId, String email) {
        if (!email.equals("anonymousUser")) {
            Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Member not found with email: " + email));

            return likeRepository.existsByBoardIdAndMemberId(boardId, member.getId());
        }
        return false;
    }

    public int getLikeCount(Long boardId) {
        return likeRepository.countByBoardId(boardId);
    }

    public Map<String, Object> updateLike(Long id, String email) {
        Member member = null;
        if (email != null) {
            member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("Member not found with email: " + email));
        }

        System.out.println("member = " + member);

        boolean isLiked = likeRepository.existsByBoardIdAndMemberId(id, member.getId());

        // isLiked가 true면(=기존에 like가 있으면) 삭제 ->isLiked는 여전히 true=>리턴 할 때 반전값을 부여)
        if (isLiked) {
            likeRepository.deleteByBoardIdAndMemberId(id, member.getId());
        } else {
            Likes like = new Likes(boardRepository.findById(id).get(), member);
            likeRepository.save(like);
        }

        int countLike = likeRepository.countByBoardId(id);

        // Board 엔티티를 가져와서 좋아요 수 업데이트
        Optional<Board> optionalBoard = boardRepository.findById(id);
        if (optionalBoard.isPresent()) {
            Board board = optionalBoard.get();
            List<Likes> likes = likeRepository.findByBoardId(board.getId());
            board.setLikes_board(likes);
            int updatedLikeCount = board.getLikes_board().size(); // 좋아요 수 업데이트
            boardRepository.save(board);
            countLike = updatedLikeCount;
        }

        return Map.of("isLiked", !isLiked, "countLike", countLike);
    }


    public Page<Likes> getAllLikes(Pageable pageable, String keyword, String category, Long id) {
        QLikes likes = QLikes.likes;
        BooleanBuilder builder = new BooleanBuilder();

        if (category != null && keyword != null) {
            if ("title".equals(category)) {
                builder.and(likes.board.title.containsIgnoreCase(keyword));
            } else if ("artist".equals(category)) {
                builder.and(likes.board.artist.containsIgnoreCase(keyword));
            }
        }

        builder.and(likes.member.id.eq(id)); //해당 아이디를 가진 멤버가 누른 likes 조건 추가

        Predicate predicate = builder.hasValue() ? builder.getValue() : null;

        if (predicate != null) {
            return likeRepository.findAll(predicate, pageable);
        } else {
            return likeRepository.findAll(pageable);
        }
    }

    public boolean checkForUser(Long id, String email) {
        //url로 받은 아이디와 context에 저장된 email로 아이디를 받아와서 비교
        Long idByContext = memberRepository.findIdByEmail(email);
        if (id == idByContext) {
            return true;
        } else {
            return false;
        }
    }
}
