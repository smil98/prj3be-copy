package com.example.prj3be.controller;

import com.example.prj3be.config.PaymentConfig;
import com.example.prj3be.dto.PaymentDto;
import com.example.prj3be.dto.PaymentRequestDto;
import com.example.prj3be.dto.PaymentResDto;
import com.example.prj3be.exception.CustomLogicException;
import com.example.prj3be.service.PaymentService;
import io.micrometer.core.annotation.Counted;
import jakarta.validation.Valid;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@Validated
@RequestMapping("/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentConfig paymentConfig;

    public PaymentController(PaymentService paymentService, PaymentConfig paymentConfig) {
        this.paymentService = paymentService;
        this.paymentConfig = paymentConfig;
    }
    @Counted("my.order")
    @PostMapping("/toss")
    public ResponseEntity requestTossPayment(@AuthenticationPrincipal User principal, @RequestBody @Valid PaymentDto paymentReqDto) throws CustomLogicException {
        PaymentResDto paymentResDto = paymentService.requestTossPayment(paymentReqDto.toEntity(), principal.getUsername()).toPaymentResDto();
        paymentResDto.setSuccessUrl(paymentReqDto.getSuccessUrl() == null ? paymentConfig.getSuccessUrl() : paymentReqDto.getSuccessUrl());
        paymentResDto.setFailUrl(paymentReqDto.getFailUrl() == null ? paymentConfig.getFailUrl() : paymentReqDto.getFailUrl());
        return ResponseEntity.ok(paymentResDto);
    }
    @PostMapping("/toss/success")
    public ResponseEntity tossPaymentSuccess(@RequestBody PaymentRequestDto dto) throws JSONException {
        String paymentKey = dto.getPaymentKey();
        String orderId = dto.getPaymentUid();
        Long amount = dto.getAmount();
        return ResponseEntity.ok().body(paymentService.tossPaymentSuccess(paymentKey,orderId,amount));
    }
    //결제 실패 로직

    // 결제 취소 로직
    @PostMapping("/toss/cancel")
    public ResponseEntity tossPaymentCancel(@AuthenticationPrincipal User principal, @RequestParam String paymentKey, @RequestParam String cancelReason) {
        return ResponseEntity.ok().body(paymentService.cancelPayment(principal.getUsername(),paymentKey,cancelReason));
    }

}
