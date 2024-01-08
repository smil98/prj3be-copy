package com.example.prj3be.service;

import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.Order;
import com.example.prj3be.domain.QMember;
import com.example.prj3be.dto.MemberEditFormDto;
import com.example.prj3be.repository.MemberRepository;
import com.example.prj3be.repository.OrderRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final OrderRepository orderRepository;

    public void signup(Member member) {
        memberRepository.save(member);
    }

    public Member findMemberById(Long id) {
        Optional<Member> findMember1 = memberRepository.findById(id);
        Member member = findMember1.get();

        return member;
    }

    public void update(Long id, MemberEditFormDto dto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + id));
        member.setNickName(dto.getNickName());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setAddress(dto.getAddress());
        member.setAge(dto.getAge());
        member.setGender(dto.getGender());
        member.setNickName(dto.getNickName());
    }

    public String getEmail(String email) {
        return memberRepository.findEmailByEmail(email)
                .orElse(null);
    }

    public String getNickName(String nickName) {
        return memberRepository.findNickNameByNickName(nickName)
                .orElse(null);
    }

    public Page<Member> findMemberList(Pageable pageable,String keyword,String category) {
        QMember member = QMember.member;
        BooleanBuilder builder = new BooleanBuilder();

        if (category != null && keyword != null) {
            if ("nickName".equals(category)) {
                builder.and(member.nickName.containsIgnoreCase(keyword));
            } else if ("email".equals(category)) {
                builder.and(member.email.containsIgnoreCase(keyword));
            }
        }

        Predicate predicate = builder.hasValue() ? builder.getValue() : null;

        if (predicate != null) {
            return memberRepository.findAll(predicate, pageable);
        } else {
            return memberRepository.findAll(pageable);
        }
    }

    public Member findMemberByEmail(String email) {
        Long id = memberRepository.findIdByEmail(email);
        Optional<Member> findMember1 = memberRepository.findById(id);
        Member member;
        if(findMember1.isPresent()) {
            member = findMember1.get();
        } else {
            return null;
        }

        return member;
    }

    public List<Order> findOrderListById(Long id) {
        return orderRepository.findOrdersByMemberId(id);
    }

    public void deleteMember(Long id) {
        memberRepository.deleteById(id);
    }

}
