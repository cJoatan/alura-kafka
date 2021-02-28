package br.com.alura.ecommerce;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class NewOrderMain {

    public static void main(String[] args) {

        try(final var orderDispatcher = new KafkaDispatcher<Order>()) {
            try(final var emailDispatcher = new KafkaDispatcher<String>()) {

                for (var i = 0; i < 10; i++) {
                    final var key = UUID.randomUUID().toString();

                    final var userId = UUID.randomUUID().toString();
                    final var orderId = UUID.randomUUID().toString();
                    final double amount = Math.random() * 5000 + 1;

                    final var order = new Order(userId, orderId, new BigDecimal(amount));

                    orderDispatcher.send("ECOMMERCE_NEW_ORDER", key, order);

                    final var email = "Welcome. We are processing your order";
                    emailDispatcher.send("ECOMMERCE_SEND_EMAIL", key, email);

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }


}
