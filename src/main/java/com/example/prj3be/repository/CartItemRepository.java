package com.example.prj3be.repository;

import com.example.prj3be.domain.Board;
import com.example.prj3be.domain.CartItem;
import com.example.prj3be.dto.CartItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.board.id = :boardId")
    CartItem findByCartIdAndBoardId(Long cartId, Long boardId);
    @Query("select ci FROM CartItem ci WHERE ci.board.id = :boardId")
    Optional<CartItem> findCartItemByBoardId(Long boardId);
    @Query("SELECT new com.example.prj3be.dto.CartItemDto(ci.id, ci.board.title, ci.board.price, ci.count, ci.fileUrl) FROM CartItem ci WHERE ci.cart.id = :cartId")
    List<CartItemDto> findCartDetailDtoList(Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id= :cartId AND ci.id = :cartItemId")
    CartItem findCartItemByCartIdAndCartItemId(Long cartId, Long cartItemId);

    @Query("SELECT ci.board FROM CartItem ci WHERE ci.id = :cartItemId")
    List<Board> findBoardIdByCartItemId(List<Long> cartItemId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.id = :cartItemId")
    void deleteCartItemByCartAndCartItemId(Long cartId, Long cartItemId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteCartItemsByCartId(Long cartId);
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.board.id = :boardId")
    int deleteCartItemByBoardId(Long boardId);
}
