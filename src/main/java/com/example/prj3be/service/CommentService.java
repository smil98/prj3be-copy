package com.example.prj3be.service;

import com.example.prj3be.domain.Board;
import com.example.prj3be.domain.Comment;
import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.QComment;
import com.example.prj3be.dto.CommentFormDto;
import com.example.prj3be.repository.BoardRepository;
import com.example.prj3be.repository.CommentRepository;
import com.example.prj3be.repository.MemberRepository;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final JPAQueryFactory query;


    public Page<Comment> commentListAll(Long boardId, Pageable pageable) {

        List<Comment> content = query.selectFrom(QComment.comment)
                .where(QComment.comment.board.id.eq(boardId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = query.select(QComment.comment.count())
                .from(QComment.comment)
                .where(QComment.comment.board.id.eq(boardId));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Member findMemberByEmail(String email) {
        Long id = memberRepository.findIdByEmail(email); //email로 memberId 찾음
        Optional<Member> findMember1 = memberRepository.findById(id); //memberId로 member 찾음
        Member member = findMember1.get();
        return member;
    }

    public void write(String boardId, CommentFormDto content, Member dto) {
        System.out.println(content.getContent());
        Optional<Board> byId = boardRepository.findById(Long.valueOf(boardId));
        Board board1 = byId.orElseThrow();

        Comment comment = Comment.builder()
                .board(board1)
                .content(content.getContent())
                .member(dto)
                .build();
        commentRepository.save(comment);
    }

    public Comment update(Long id, Comment updateComment) {
        Optional<Comment> contents = commentRepository.findById(id);
        if (contents.isPresent()) {
            Comment content = contents.get();
            content.setId(updateComment.getId());
            content.setContent(updateComment.getContent());
            return commentRepository.save(content);
        } else {
            throw new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다.");
        }
    }

    @PreAuthorize("hasRole('ADMIN') || #email == authentication.name")
    public void delete(Long id, String email) {
        commentRepository.deleteCommentByMemberId(id);
        commentRepository.deleteCommentByBoardId(id);
        commentRepository.deleteById(id);
    }

    public String findMemberById(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + id));

        return comment.getMember().getEmail();
    }
}
