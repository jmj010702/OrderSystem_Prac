package com.beyound.ordersystem.ordering.repository;

import com.beyound.ordersystem.ordering.entity.OrderingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingDetailRepository extends JpaRepository<OrderingDetail, Long> {

}
