package com.example.prj3be.domain;

import com.example.prj3be.exception.OutOfStockException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Entity
@Getter @Setter
@Table(name="cart_item")
public class CartItem {
    @Id
    @GeneratedValue
    @Column(name="cart_item_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name="cart_id") //한 사람당 한 카트
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "board_id")
    private Board board;

    private int count;
    //자주 쓰이는 것들은 따로 저장
    private String fileUrl;
    private Long stockQuantity;

    public static CartItem createCartItem(Cart cart, Board board, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setBoard(board);
        cartItem.setCount(count);
        cartItem.setFileUrl(board.getBoardFiles().get(0).getFileUrl());
        cartItem.setStockQuantity(board.getStockQuantity());
        return cartItem;
    }

    public void addCount(Long stockQuantity) {
        if((this.count + 1) <= stockQuantity) {
            this.count += 1;
        } else {
            throw new OutOfStockException("재고 수량 초과");
        }
    }

    public void subtractCount(int count) {
        this.count -= 1;
    }
}


