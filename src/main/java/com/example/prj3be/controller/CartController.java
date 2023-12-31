package com.example.prj3be.controller;

import com.example.prj3be.domain.Cart;
import com.example.prj3be.domain.Member;
import com.example.prj3be.dto.CartItemDto;
import com.example.prj3be.exception.OutOfStockException;
import com.example.prj3be.jwt.TokenProvider;
import com.example.prj3be.repository.MemberRepository;
import com.example.prj3be.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @GetMapping("/fetch")
    public List<CartItemDto> fetchCart() {
        System.out.println("CartController.fetchCart");
        //accessToken으로부터 로그인 아이디 추출
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        //로그인 아이디로부터 멤버 아이디 추출
        Long memberId = memberRepository.findIdByEmail(email);

        return cartService.getCartList(memberId);
    }

    @PostMapping("/add")
    public ResponseEntity createCartAndAddItem(Long boardId) {
        //id = board.id (상품명)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long memberId = memberRepository.findIdByEmail(email);

        try {
            Cart cart = cartService.createCart(memberId);
            cartService.addItemsToCart(cart, boardId);
            return ResponseEntity.ok().build();
        } catch (OutOfStockException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/move")
    public ResponseEntity moveSelectedCartItems(@RequestBody Map<String, List<Long>> request,
                                                @RequestHeader("Authorization") String accessToken) {
        System.out.println("CartController.moveSelectedCartItems");
        List<Long> cartItemIds = request.get("selectedItems");

        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        if(tokenProvider.validateToken(accessToken)) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Optional<Member> member = memberRepository.findByEmail(email);
            if(member.isPresent()) {
                try {
                    cartService.addToLikeByCartItemId(member.get(), cartItemIds);
                    cartService.deleteCartItemsByCartItemId(cartItemIds);
                    return ResponseEntity.ok().build();
                } catch (Exception e) {
                    System.out.println("Error occured while adding to likes or deleting = " + e);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
            } else {
                System.out.println(" Member가 존재하지 않음 ");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete/cartItems")
    public ResponseEntity deleteSelectedCartItems(@RequestParam List<String> selectedItems,
                                                  @RequestHeader("Authorization") String accessToken) {
        System.out.println("CartController.deleteSelectedCartItems");
        List<Long> cartItemIds = selectedItems.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        if(tokenProvider.validateToken(accessToken)) {
            try {
                cartService.deleteCartItemsByCartItemId(cartItemIds);
                return ResponseEntity.ok().build();
            } catch (Exception e) {
                System.out.println("Error occured while deleting = " + e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }


    @PostMapping("/add/liked")
    public ResponseEntity createCartAndAddLiked(@RequestBody Map<String, Long> request) {
        System.out.println("CartController.createCartAndAddLiked");
        Long boardId = request.get("boardId");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long memberId = memberRepository.findIdByEmail(email);

        try {
            Cart cart = cartService.createCart(memberId);
            Long stockQuantity = cartService.getBoardInfoByBoardId(boardId);
            if(stockQuantity != null) {
                cartService.addItemsToCart(cart, boardId);
                cartService.deleteLikedByMemberAndBoardId(memberId, boardId);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.ok().build();
        } catch (OutOfStockException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/add/selected")
    public ResponseEntity createCartAndAddSelected(@RequestBody Map<String, List<Long>> request,
                                                   @RequestHeader("Authorization") String accessToken) {
        System.out.println("CartController.createCartAndAddSelected");
        List<Long> selectedLikes = request.get("selectedLikes");

        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        if(tokenProvider.validateToken(accessToken)) {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Long memberId = memberRepository.findIdByEmail(email);
            try {
                Cart cart = cartService.createCart(memberId);
                cartService.addSelectedToCart(cart, selectedLikes);
                cartService.deleteSelectedLikes(selectedLikes);
                return ResponseEntity.ok().build();
            } catch (OutOfStockException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete/selected")
    public ResponseEntity deleteSelectedLikes(@RequestParam("selectedLikes") List<Long> selectedLikes,
                                              @RequestHeader("Authorization") String accessToken) {
        System.out.println("CartController.deleteSelectedLikes");

        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        if(tokenProvider.validateToken(accessToken)) {
            try {
                cartService.deleteSelectedLikes(selectedLikes);
            return ResponseEntity.ok().build();
            } catch (Exception e) {
                System.out.println("삭제 중 문제 발생: " + e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete/{cartItemId}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long cartItemId) {
        System.out.println("CartController.deleteCartItem");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("email = " + email);
        Long memberId = memberRepository.findIdByEmail(email);
        System.out.println("memberId = " + memberId);

        try {
            cartService.deleteCartItemByCartAndCartItem(memberId, cartItemId);
            return ResponseEntity.ok(cartItemId + "번 아이템 삭제 완료");
        } catch (Exception e) {
            System.out.println("카트 아이템 삭제 중 오류 발생: " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카트 아이템 삭제 중 오류 발생");
        }
    }

    @GetMapping("/addCount/{cartItemId}")
    public void addCount(@PathVariable Long cartItemId) {
        System.out.println("CartController.addCount");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("email = " + email);
        //로그인 아이디로부터 멤버 아이디 추출
        Long memberId = memberRepository.findIdByEmail(email);
        System.out.println("memberId = " + memberId);

        cartService.addCountToCartItem(memberId, cartItemId);
    }

    @GetMapping("/subtractCount/{cartItemId}")
    public void subtractCount(@PathVariable Long cartItemId) {
        System.out.println("CartController.subtractCount");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        //로그인 아이디로부터 멤버 아이디 추출
        Long memberId = memberRepository.findIdByEmail(email);

        cartService.subtractCountFromCartItem(memberId, cartItemId);
    }
}