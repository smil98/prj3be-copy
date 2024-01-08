package com.example.prj3be.repository;

import com.example.prj3be.domain.Order;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM Order o WHERE o.member.id = :memberId")
    int deleteByMemberId(Long memberId);

    @Query("SELECT o FROM Order o WHERE o.member.id = :id")
    List<Order> findOrdersByMemberId(@Param("id") Long id);

}
