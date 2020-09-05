package com.learning.order.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.learning.order.model.Order;

@Repository
public interface OrderRepository extends CrudRepository<Order, Integer> {

	List<Order> findByCustomerId(Integer customerId);
	
}
