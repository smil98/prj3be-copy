package com.example.prj3be.service;

import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.Order;
import com.example.prj3be.repository.MemberRepository;
import com.example.prj3be.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    public Member findMemberByEmail(String email) {
        Long id = memberRepository.findIdByEmail(email);
        Optional<Member> findMember1 = memberRepository.findById(id);
        Member member = findMember1.get();
        return member;
    }

    public void make(Order order) {
        orderRepository.save(order);
    }
}
