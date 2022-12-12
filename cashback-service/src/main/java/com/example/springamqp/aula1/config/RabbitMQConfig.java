package com.example.springamqp.aula1.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    //Queue do cashback
    @Bean
    public Queue queueCashBack() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "orders.v1.order-created.dlx");
        args.put("x-max-priority", 10);
        //args.put("x-dead-letter-routing-key", "orders.v1.order-created.dlx.generate-cashback.dlq");
        return new Queue("orders.v1.order-created.generate-cashback", true, false, false, args);
    }

    @Bean
    public Binding binding() {
       Queue queue = queueCashBack();
       FanoutExchange exchange = new FanoutExchange("orders.v1.order-created");
        return BindingBuilder.bind(queue).to(exchange);
    }

    //Dead letter queue do cashback
    @Bean
    public Queue queueCashBackDLQ() {
        return new Queue("orders.v1.order-created.dlx.generate-cashback.dlq");
    }

    //Dead letter queue Parking Lot
    @Bean
    public Queue queueCashBackDLQParkingLot() {
        return new Queue("orders.v1.order-created.dlx.generate-cashback.dlq.parking-lot");
    }

    @Bean
    public Binding bindingDLQ() {
        Queue queue = queueCashBackDLQ();
        FanoutExchange exchange = new FanoutExchange("orders.v1.order-created.dlx");
        return BindingBuilder.bind(queue).to(exchange);
    }
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    //Bean para converter em json
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    //Bean para enviar um objeto mais complexo, ex: dto e converter em json
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> applicationReadyEventApplicationListener(RabbitAdmin rabbitAdmin) {
        return event -> rabbitAdmin.initialize();
    }
}
