package com.example.prj3be.repository;

import com.example.prj3be.domain.Likes;
import com.example.prj3be.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LikeRepository extends JpaRepository<Likes, Long>, QuerydslPredicateExecutor<Likes> {

    boolean existsByBoardIdAndMemberId(Long boardId, Long id);

    void deleteByBoardIdAndMemberId(Long id, Long id1);

    int countByBoardId(Long id);

    List<Likes> findByBoardId(Long id);

    void deleteByBoardId(Long id);

    List<Likes> findByMemberId(Long id);

//    @Repository
//    public interface LikesRepository extends JpaRepository<Likes, Long> {
//
//        int countByBoardId(Long boardId);
//
//        void deleteByBoardIdAndMemberId(Long boardId, Long memberId);
//List<Likes> findByMemberId(Long id);
//        Likes findByBoardIdAndMemberId(Long boardId, Long memberId);
//    }


}
