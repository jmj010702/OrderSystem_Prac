package com.beyound.ordersystem.ordering.repository;

import com.beyound.ordersystem.member.entity.Member;
import com.beyound.ordersystem.ordering.entity.Ordering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Ordering, Long> {
    List<Ordering> findAllByMember(Member member);
    Page<Ordering> findAll(Pageable pageable);
}
