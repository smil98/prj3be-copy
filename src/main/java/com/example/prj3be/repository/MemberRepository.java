package com.example.prj3be.repository;

import com.example.prj3be.constant.Role;
import com.example.prj3be.domain.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Map;
import java.util.Optional;


public interface MemberRepository extends JpaRepository<Member,Long>,QuerydslPredicateExecutor<Member>{
    @Query("SELECT m.email FROM Member m WHERE m.email = :email")
    Optional<String> findEmailByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findByEmail(String email);

    @EntityGraph(attributePaths = "role")
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findOneWithAuthoritiesByEmail(String email);

    @Query("SELECT m.id FROM Member m WHERE m.email = :email")
    Long findIdByEmail(String email);

    @Query("SELECT m.role FROM Member m WHERE m.email = :email")
    Role checkSocialMemberByEmail(String email);

    @Query("SELECT m.nickName FROM Member m WHERE m.nickName = :nickName")
    Optional<String> findNickNameByNickName(String nickName);
}
