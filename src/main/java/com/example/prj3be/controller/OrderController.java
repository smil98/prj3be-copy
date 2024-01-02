package com.example.prj3be.controller;

import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.Order;
import com.example.prj3be.domain.Payment;
import com.example.prj3be.dto.FindMemberDto;
import com.example.prj3be.dto.OrderFormDto;
import com.example.prj3be.service.OrderService;
import com.example.prj3be.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;
    @GetMapping()
    public ResponseEntity method1() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!authentication.getName().equals("anonymousUser")) {
            Member findMember = orderService.findMemberByEmail(authentication.getName());
            FindMemberDto dto = new FindMemberDto();
            dto.setEmail(findMember.getEmail());
            dto.setNickName(findMember.getNickName());
            dto.setAddress(findMember.getAddress());
            dto.setEmail(findMember.getEmail());
            dto.setGender(findMember.getGender());
            dto.setRole(findMember.getRole());
            System.out.println(dto);
            return ResponseEntity.ok(dto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


}
