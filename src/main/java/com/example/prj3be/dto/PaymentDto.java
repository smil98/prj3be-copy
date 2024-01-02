package com.example.prj3be.dto;

import com.example.prj3be.constant.PaymentStatus;
import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.Payment;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {
    @NotNull
    private Long amount; //총 금액
    @NotNull
    private String paymentName; //결제 제목
    private String name;
    @NotNull
    private String email;
    private String successUrl;
    private String paymentUid;
    private String failUrl;
    public Payment toEntity() {
        return Payment.builder()
                .amount(amount)
                .paymentName(paymentName)
                .paymentUid(paymentUid)
                .paySuccessYN(false)
                .build();
    }
}
