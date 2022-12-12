package com.example.springamqp.aula1.controller;

import com.example.springamqp.aula1.dto.OrderDTO;
import com.example.springamqp.aula1.model.Order;
import com.example.springamqp.aula1.repository.OrderRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collection;

@RestController
@RequestMapping(value = "/v1/orders")
public class OrderController {

	@Autowired
	private OrderRepository orders;

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@PostMapping
	public Order create(@RequestBody Order order) {
		orders.save(order);
		// Usando o routingKey como variavel
		//String routingKey = "orders.v1.order-created";

		//Message message = new Message(order.getId().toString().getBytes());
		//Caso eu queria enviar o tipo message sem converter -> rabbitTemplate.send(routingKey, message);

		final int priority;
		if(order.getValue().compareTo(new BigDecimal("10000")) >= 0) {
			priority = 5;
		} else {
			priority = 1;
		}

		final MessagePostProcessor postProcessor = message -> {
			MessageProperties messageProperties = message.getMessageProperties();
			messageProperties.setPriority(priority);
			return message;
		};
		OrderDTO orderDTO = new OrderDTO(order.getId(), order.getValue());
		//Usando o convertAndSend, para conversão

		rabbitTemplate.convertAndSend("orders.v1.order-created", "", orderDTO, postProcessor);
		//Usando a forma sem o fanout -> rabbitTemplate.convertAndSend("orders.v1.order-created.send-notification", orderDTO);
		return order;
	}

	@GetMapping
	public Collection<Order> list() {
		return orders.findAll();
	}

	@GetMapping("{id}")
	public Order findById(@PathVariable Long id) {
		return orders.findById(id).orElseThrow();
	}

	@PutMapping("{id}/pay")
	public Order pay(@PathVariable Long id) {
		Order order = orders.findById(id).orElseThrow();
		order.markAsPaid();
		return orders.save(order);
	}
	
}
