package com.example.springamqp.aula1.dlq;

import com.example.springamqp.aula1.dto.OrderDTO;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DeadLetterQueueListener {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String X_RETRY_HEADER = "x-dlq-retry";

    private static final String DLQ = "orders.v1.order-created.dlx.generate-cashback.dlq";

    private static final String DLQ_PARKING_LOT = "orders.v1.order-created.dlx.generate-cashback.dlq.parking-lot";

    @RabbitListener(queues = DLQ)
    public void processar(OrderDTO orderDTO, @Headers Map<String, Object> headers) {

        Integer retryHeader = (Integer) headers.get(X_RETRY_HEADER);

        if(retryHeader == null) {
            retryHeader = 0;
        }

        System.out.println("Reprocessando venda de id: " + orderDTO.getId());

        if(retryHeader < 3) {
            int tryCount = retryHeader + 1;
            Map<String, Object> updateHeaders = new HashMap<>(headers);

            updateHeaders.put(X_RETRY_HEADER, tryCount);

            //Reprocessamento

            final MessagePostProcessor messagePostProcessor = message -> {
              MessageProperties messageProperties = message.getMessageProperties();
              updateHeaders.forEach(messageProperties::setHeader);
              return message;
            };

            System.out.println("Reenviando venda de id " + orderDTO.getId() + " para a DLQ");
            this.rabbitTemplate.convertAndSend(DLQ, orderDTO, messagePostProcessor);
        } else {
            System.out.println("Reprocessamento falhou, enviando venda de id " + orderDTO.getId() + " para o parking lot");
            this.rabbitTemplate.convertAndSend(DLQ_PARKING_LOT, orderDTO);
        }

    }
}
