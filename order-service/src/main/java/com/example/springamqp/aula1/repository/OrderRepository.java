package com.example.springamqp.aula1.repository;

import com.example.springamqp.aula1.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
