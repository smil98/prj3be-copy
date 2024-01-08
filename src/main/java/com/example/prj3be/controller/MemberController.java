package com.example.prj3be.controller;

import com.example.prj3be.constant.Role;
import com.example.prj3be.domain.Likes;
import com.example.prj3be.domain.Member;
import com.example.prj3be.dto.FindMemberDto;
import com.example.prj3be.dto.MemberEditFormDto;
import com.example.prj3be.dto.MemberFormDto;
import com.example.prj3be.jwt.TokenProvider;
//import com.example.prj3be.dto.SocialMemberDto;
import com.example.prj3be.repository.CommentRepository;
import com.example.prj3be.repository.LikeRepository;
import com.example.prj3be.repository.OrderRepository;
import com.example.prj3be.repository.PaymentRepository;
import com.example.prj3be.service.CartService;
import com.example.prj3be.service.CommentService;
import com.example.prj3be.service.LikeService;
import com.example.prj3be.service.MemberService;
import com.example.prj3be.service.oauth.OauthService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;


@RestController
@Slf4j
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final OauthService oauthService;
    private final CartService cartService;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;


    @PostMapping("add")
    public void method1(@Validated @RequestBody MemberFormDto dto) {
        Member member = new Member();

        member.setEmail(dto.getEmail());
        member.setPassword(passwordEncoder.encode(dto.getPassword()));
        member.setNickName(dto.getNickName());
        member.setGender(dto.getGender());
        member.setAge(dto.getAge());

//        Role role = Role.valueOf(String.valueOf(dto.getRole()));
//        member.setRole(role);
        memberService.signup(member);
    }

    // 회원 정보
    @GetMapping
    public ResponseEntity<FindMemberDto> method2() {
        // access token Jwt Filter에서 SecurityContextHolder에 넣어줌
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!authentication.getName().equals("anonymousUser")) {
            Member findMember = memberService.findMemberByEmail(authentication.getName());
            if(findMember != null) {
                FindMemberDto dto = new FindMemberDto(findMember);
                return ResponseEntity.ok(dto);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindMemberDto> getMemberById(@PathVariable Long id) {
        System.out.println("id = " + id);
        System.out.println("MemberController.getMemberById");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member findMember = memberService.findMemberById(id); // 일단 찾아놓고

        if (findMember != null) { //멤버가 null이 아니면
            if(authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                //admin인지 확인, 맞으면 찾은 멤버 정보 전송
                FindMemberDto dto = new FindMemberDto(findMember);
                return ResponseEntity.ok(dto);
            } else { //admin이 아닐 때
                if (authentication.getName().equals(findMember.getEmail())) { //찾는 멤버의 이메일과 authentication.getName() 비교
                    FindMemberDto dto = new FindMemberDto(findMember); //본인이 맞으면 전송
                    return ResponseEntity.ok(dto);
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                }
            }
        } else {
            //해당되는 멤버가 없음
            return ResponseEntity.notFound().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN') || #dto.email == authentication.name")
    @PutMapping("/edit/{id}")
    public void method3(@PathVariable Long id,@Validated @RequestBody MemberEditFormDto dto) {
        memberService.update(id,dto);
    }

    @GetMapping(value = "check",params = "email")
    public ResponseEntity method4(@RequestParam(name = "email") String email){
        System.out.println("email = " + email);
        if (memberService.getEmail(email)==null){
            return ResponseEntity.notFound().build();
        }else {
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping(value = "check")
    public ResponseEntity method5(@RequestParam(name="nickName") String nickName){
        if (memberService.getNickName(nickName)==null){
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("list")
    public Page<FindMemberDto> method5(Pageable pageable,
                                       @RequestParam(value = "k",defaultValue = "")String keyword,
                                       @RequestParam(value = "c",defaultValue = "all")String category) {
        System.out.println(keyword);
        System.out.println(category);
        Page<Member> memberPage = memberService.findMemberList(pageable,keyword,category);
        return memberPage.map(FindMemberDto::new);
    }

    //TODO: 주문한 정보 리턴인데 수정하길...
    @GetMapping("/{email}/orders")
    public List<String> method7(@PathVariable String email){
        return memberService.findOrderListByEmail(email);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity deleteAccount(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean hasRefreshToken = tokenProvider.hasRefreshTokenByEmail(email);

        // payment에서 해당 멤버 관련 레코드 삭제됐는지
        orderRepository.deleteByMemberId(id);
        paymentRepository.deleteByMemberId(id);

        // 해당 멤버별  like 삭제됐는지
        List<Likes> likes = likeRepository.findByMemberId(id);
        likeRepository.deleteAll(likes);

        // 해당 멤버가 작성한 코멘트가 삭제됐는지
        commentRepository.deleteCommentByMemberId(id);

        // fresh_token 삭제 됐는지
        if(hasRefreshToken) {
            //토큰 만료되지 않았을 경우
            tokenProvider.deleteRefreshTokenByEmail(email);
        }else{
            //토큰 만료 된 경우 => 일단 그냥 id를 통해서 삭제
            tokenProvider.deleteRefreshTokenById(id);
        }

        // social 멤버인지, 맞다면 social Token 삭제
        boolean isSocial = tokenProvider.isSocialMemberByEmail(email);
        if(isSocial) {
            oauthService.deleteSocial(id);
        }

        //이 멤버의 카트가 존재한다면
        if (cartService.findCart(id) != false) {
            //장바구니 아이템 삭제
            cartService.deleteCartItem(id);
            //장바구니 삭제
            cartService.deleteCart(id);
        }

        // 회원 삭제
        memberService.deleteMember(id);

        return ResponseEntity.ok().build();
    }

}
