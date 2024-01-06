package com.example.prj3be.service;

import com.example.prj3be.domain.*;
import com.example.prj3be.dto.CartItemDto;
import com.example.prj3be.exception.OutOfStockException;
import com.example.prj3be.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final BoardFileRepository boardFileRepository;
    private final BoardRepository boardRepository;
    private final CartItemRepository cartItemRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;

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
    public void addItemsToCart(Cart cart, Long boardId) {
        System.out.println("CartService.addItemsToCart");
        Board board = boardRepository.findById(boardId).orElseThrow(NullPointerException::new);
        CartItem savedCartItem = cartItemRepository.findByCartIdAndBoardId(cart.getId(), boardId);

        //만약 장바구니에 해당 아이템이 이미 존재할 경우 수량 증대
        if(savedCartItem != null) {
            System.out.println("장바구니에 해당 아이템이 존재함");
            savedCartItem.addCount(board.getStockQuantity());
            cartItemRepository.save(savedCartItem);
        } else {
            System.out.println("장바구니에 해당 아이템이 존재하지 않음");
            //만약 장바구니에 해당 아이템이 존재하지 않을 경우 1개 추가
            CartItem cartItem = CartItem.createCartItem(cart, board, 1);
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
        System.out.println("CartService.addCountToCartItem");
        Cart cart = cartRepository.findByMemberId(memberId);
        CartItem item = cartItemRepository.findCartItemByCartIdAndCartItemId(cart.getId(), cartItemId);
        item.addCount(item.getStockQuantity());
        cartItemRepository.save(item);
    }

    public void subtractCountFromCartItem(Long memberId, Long cartItemId) {
        System.out.println("CartService.subtractCountFromCartItem");
        Cart cart = cartRepository.findByMemberId(memberId);
        CartItem item = cartItemRepository.findCartItemByCartIdAndCartItemId(cart.getId(), cartItemId);
        try{
            item.subtractCount(item.getCount());
            cartItemRepository.save(item);
        } catch (OutOfStockException e) {
            e.printStackTrace();
        }
    }

    // 삭제
    // 특정 카트 안에 있는 특정 아이템 삭제
    @Transactional
    public void deleteCartItemByCartAndCartItem(Long memberId, Long cartItemId) {
        System.out.println("CartService.deleteCartItemByCartAndCartItem");
        Cart cart = cartRepository.findByMemberId(memberId);
        cartItemRepository.deleteCartItemByCartAndCartItemId(cart.getId(), cartItemId);
    }

    // 특정 멤버의 카트 아이템 삭제
    @Transactional
    public void deleteCartItem(Long id) {
        System.out.println("CartService.deleteCartItem");
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

    public Long getBoardInfoByBoardId(Long boardId) {
        System.out.println("CartService.getBoardInfoByBoardId");
        Optional<Board> byId = boardRepository.findById(boardId);
        if(byId.isPresent()) {
            System.out.println("byId = " + byId.get().getStockQuantity());
            return byId.get().getStockQuantity();
        } else {
            throw new EntityNotFoundException("Board not found with ID: " + boardId);
        }
    }

    @Transactional
    public void deleteLikedByMemberAndBoardId(Long memberId, Long boardId) {
        System.out.println("CartService.deleteLikedByMemberAndBoardId");
        likeRepository.deleteByBoardIdAndMemberId(boardId, memberId);
    }

    //찜한 목록에서 선택된 상품 카트로 저장
    public void addSelectedToCart(Cart cart, List<Long> selectedLikes) {
        System.out.println("CartService.addSelectedToCart");
        List<Likes> likesList = likeRepository.findAllById(selectedLikes);

        List<Long> boardIds = likesList.stream()
                .map(Likes::getBoard)
                .map(Board::getId)
                .collect(Collectors.toList());

        for(Long boardId : boardIds) {
            addItemsToCart(cart, boardId);
        }
    }

    //찜한 목록에서 카트로 이동한 상품들 좋아요 내역 삭제
    @Transactional
    public void deleteSelectedLikes(List<Long> selectedLikes) {
        likeRepository.deleteAllById(selectedLikes);
    }
}
