package com.example.springamqp.aula1.listener;

import com.example.springamqp.aula1.dto.OrderDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    //Listener que vai receber as messages
@RabbitListener(queues = "orders.v1.order-created.send-notification")
    public void onOrderCreated(OrderDTO obj) {
    System.out.println("ID recebido " + obj.getId());
    System.out.println("Valor recebido " + obj.getValue());
    }
}
