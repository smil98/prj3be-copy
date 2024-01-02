package com.example.prj3be.domain;

import com.example.prj3be.constant.PaymentStatus;
import com.example.prj3be.dto.PaymentResDto;
import jakarta.persistence.*;
import lombok.*;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table
public class Payment extends BaseTimeEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private Long amount;
    private String paymentUid; // 결제 고유 번호
    private String paymentName; // 결제 제목
    private boolean paySuccessYN; // 성공여부
    @ManyToOne(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    private String name;
    private String paymentKey;
    private String failReason;
    private boolean cancelYN;
    private String cancelReason;

    public Payment toEntity() {
        return Payment.builder()
                .status(status)
                .amount(amount)
                .paymentName(paymentName)
                .paymentUid(UUID.randomUUID().toString())
                .paySuccessYN(false)
                .build();
    }

    public PaymentResDto toPaymentResDto() {
        // SimpleDateFormat 또는 다른 날짜 포맷터를 사용하여 날짜를 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return PaymentResDto.builder()
                .status(this.status != null ? this.status.toString() : null)
                .amount(this.amount)
                .paymentName(this.paymentName)
                .paymentUid(this.paymentUid)
                .customerEmail(this.member != null ? this.member.getEmail() : null)
                .customerName(this.name)
                // URL은 이 메서드에서 설정하지 않고 컨트롤러에서 설정
                .failReason(this.failReason)
                .cancelYN(this.cancelYN)
                .cancelReason(this.cancelReason)
                .createdAt(this.getRegTime() != null ? formatter.format(this.getRegTime()) : null)
                .build();
    }
}
