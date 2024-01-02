package com.example.prj3be.service;

import com.example.prj3be.domain.Board;
import com.example.prj3be.domain.Cart;
import com.example.prj3be.domain.CartItem;
import com.example.prj3be.domain.Member;
import com.example.prj3be.dto.CartItemDto;
import com.example.prj3be.exception.OutOfStockException;
import com.example.prj3be.repository.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final BoardFileRepository boardFileRepository;
    private final BoardRepository boardRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;

    // 카트 생성
    public Cart createCart(Long memberId) {
        System.out.println("CartService.createCart");
        Member member = memberRepository.findById(memberId).orElseThrow(null);
        System.out.println("member = " + member.getId());
        Cart cart = cartRepository.findByMemberId(memberId);
        System.out.println("!= 체크하기 전에 cart = " + cart);

        // 해당 회원의 카트가 없으면 만듦, 저장
        if(cart == null) {
            cart = Cart.createCart(member);
            cartRepository.save(cart);
        }

        return cart;
    }

    // 카트에 아이템 추가
    public void addItemsToCart(Cart cart, Long boardId, Long stockQuantity) {
        System.out.println("CartService.addItemsToCart");
        System.out.println("boardId = " + boardId);
        Board board = boardRepository.findById(boardId).orElseThrow(NullPointerException::new);

        String fileUrl = boardFileRepository.findFileUrlsByBoardId(boardId).get(0);
        System.out.println("fileUrl = " + fileUrl);
        System.out.println("cart.getId() = " + cart.getId());

        CartItem savedCartItem = cartItemRepository.findByCartIdAndBoardId(cart.getId(), boardId);
        System.out.println("savedCartItem = " + savedCartItem);

        //만약 장바구니에 해당 아이템이 이미 존재할 경우 수량 증대
        if(savedCartItem != null) {
            System.out.println("장바구니에 해당 아이템이 존재함");
            savedCartItem.addCount(stockQuantity);
            cartItemRepository.save(savedCartItem);
            System.out.println("savedCartItem = " + savedCartItem.getCount());
        } else {
            System.out.println("장바구니에 해당 아이템이 존재하지 않음");
            //만약 장바구니에 해당 아이템이 존재하지 않을 경우 1개 추가
            CartItem cartItem = CartItem.createCartItem(cart, board, 1, fileUrl, stockQuantity);
            System.out.println("cartItem = " + cartItem.getId() + "번 제품");
            System.out.println("cartItem.getCart() = " + cartItem.getCart());
            System.out.println("cartItem.getCount() = " + cartItem.getCount());
            cartItemRepository.save(cartItem);
        }
    }

    // 카트 불러오기
    public List<CartItemDto> getCartList(Long id) {
        System.out.println("CartService.getCartList");

        List<CartItemDto> cartItemList = new ArrayList<>();
        Cart cart = cartRepository.findByMemberId(id);
        if(cart == null) {
            return cartItemList;
        }

        System.out.println(cartItemRepository.findCartDetailDtoList(cart.getId()));

        cartItemList = cartItemRepository.findCartDetailDtoList(cart.getId());

        return cartItemList;
    }

    // 수량 증감
    public void addCountToCartItem(Long memberId, Long cartItemId) {
        Cart cart = cartRepository.findByMemberId(memberId);
        System.out.println("cart.getId() = " + cart.getId());
        CartItem item = cartItemRepository.findCartItemByCartIdAndCartItemId(cart.getId(), cartItemId);
        System.out.println("item.getStockQuantity() = " + item.getStockQuantity());
        System.out.println("item.getCount() = " + item.getCount());
        item.addCount(item.getStockQuantity());
        System.out.println("item.getCount() = " + item.getCount());
        cartItemRepository.save(item);
    }

    public void subtractCountFromCartItem(Long memberId, Long cartItemId) {
        Cart cart = cartRepository.findByMemberId(memberId);
        System.out.println("cart.getId() = " + cart.getId());
        CartItem item = cartItemRepository.findCartItemByCartIdAndCartItemId(cart.getId(), cartItemId);
        try{
            System.out.println("빼기 전 item.getCount() = " + item.getCount());
            item.subtractCount(item.getCount());
            System.out.println("뺀 후 item.getCount() = " + item.getCount());
            cartItemRepository.save(item);
        } catch (OutOfStockException e) {

        }
    }

    // 삭제
    // 특정 카트 안에 있는 특정 아이템 삭제
    @Transactional
    public void deleteCartItemByCartAndCartItem(Long memberId, Long cartItemId) {
        System.out.println("CartService.deleteCartItemByCartAndCartItem");
        Cart cart = cartRepository.findByMemberId(memberId);
        System.out.println("cart.getId() = " + cart.getId());
        System.out.println("cartItemId = " + cartItemId);
        cartItemRepository.deleteCartItemByCartAndCartItemId(cart.getId(), cartItemId);
    }

    // 특정 멤버의 카트 아이템 삭제
    @Transactional
    public void deleteCartItem(Long id) {
        Cart cart = cartRepository.findByMemberId(id);
        cartItemRepository.deleteCartItemsByCartId(cart.getId());
    }

    // 특정 멤버의 카트 삭제
    @Transactional
    public void deleteCart(Long id) {
        cartRepository.deleteByMemberId(id);
    }

    public boolean findCart(Long id) {
        Cart byMemberId = cartRepository.findByMemberId(id);
        if (byMemberId == null){
            return false;
        }else {
            return true;
        }
    }
}
