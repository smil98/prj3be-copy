package com.example.prj3be.service;

import com.example.prj3be.config.PaymentConfig;
import com.example.prj3be.domain.Member;
import com.example.prj3be.domain.Order;
import com.example.prj3be.domain.Payment;
import com.example.prj3be.dto.PaymentSuccessDto;
import com.example.prj3be.exception.CustomLogicException;
import com.example.prj3be.exception.ExceptionCode;
import com.example.prj3be.repository.MemberRepository;
import com.example.prj3be.repository.OrderRepository;
import com.example.prj3be.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.HttpHeaders;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Transactional
@RequiredArgsConstructor
@Service
public class PaymentService {
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentConfig paymentConfig;
    private final OrderRepository orderRepository;

    public Payment requestTossPayment(Payment payment, String Email) throws CustomLogicException {
        // 처음 결제 요청시 값들에 대한 검증
        Optional<Member> findMember = memberRepository.findByEmail(Email);
        Member member = findMember.orElseThrow();
        if (payment.getAmount() < 1000) {
            //결제 금액이 1000원 미만이면 오류
            throw new CustomLogicException(ExceptionCode.INVALID_PAYMENT_AMOUNT);
        }
        payment.setMember(member);
        return paymentRepository.save(payment);
    }

    public PaymentSuccessDto tossPaymentSuccess(String paymentKey, String orderId, Long amount) throws JSONException {
        Payment payment = verifyPayment(orderId,amount);
        PaymentSuccessDto result = requestPaymentAccept(paymentKey,orderId,amount);
        payment.setPaymentKey(paymentKey); //추후 결제 취소 / 결제 조회!
        payment.setPaySuccessYN(true);
//        payment.getMember().setPoint(payment.getMember().getPoint() + amount); 나중에 추후 포인트 사용시 이용.
//        memberService.updateMemberCache(payment.getCustomer());
        paymentRepository.save(payment);
        String paymentName = payment.getPaymentName();
        Member member = payment.getMember();
        Order order = Order.builder()
                .price(amount.intValue())
                .payment(payment)
                .orderUid(orderId)
                .member(member)
                .orderName(paymentName)
                .build();
        orderRepository.save(order);
        return result;
    }
    public Payment verifyPayment(String orderId, Long amount) {
        Payment payment = paymentRepository.findByPaymentUid(orderId).orElseThrow(() -> {
            throw new CustomLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
        });
        if (!payment.getAmount().equals(amount)) {
            throw new CustomLogicException(ExceptionCode.PAYMENT_AMOUNT_EXP);
        }
        return payment;
    }
    public PaymentSuccessDto requestPaymentAccept(String paymentKey, String orderId, Long amount) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();
//        JSONObject params = new JSONObject();//키/값 쌍을 문자열이 아닌 오브젝트로 보낼 수 있음
        Map<String, Object> params = new HashMap<>();
        params.put("orderId", orderId);
        params.put("amount", amount);
        params.put("paymentKey",paymentKey);

        PaymentSuccessDto result = null;
        try { //post요청 (url , HTTP객체 ,응답 Dto)
            System.out.println("---");
            result = restTemplate.postForObject(PaymentConfig.URL,
                    new HttpEntity<>(params, headers),
                    PaymentSuccessDto.class);
            System.out.println("11111111111");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomLogicException(ExceptionCode.ALREADY_APPROVED);
        }
        return result;
    }
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        String encodedAuthKey = new String(
                Base64.getEncoder().encode((paymentConfig.getTestSecretKey() + ":").getBytes(StandardCharsets.UTF_8)));
        headers.setBasicAuth(encodedAuthKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    public Map cancelPayment(String email, String paymentKey, String cancelReason) {
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        Member member = byEmail.orElseThrow();
        Payment payment = paymentRepository.findByPaymentKeyAndMember_Email(paymentKey, member.getEmail()).orElseThrow(() -> {
            throw new CustomLogicException(ExceptionCode.PAYMENT_NOT_FOUND);
        });
        return payCancel(paymentKey,cancelReason);
    }

    private Map payCancel(String paymentKey, String cancelReason) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = getHeaders();
        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason",cancelReason);
        return restTemplate.postForObject(PaymentConfig.URL + paymentKey + "/cancel",
                new HttpEntity<>(params, headers),
                Map.class);
    }

    public Payment findPayment(String paymentKey) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey).orElseThrow();
        return payment;
    }
}
