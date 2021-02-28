package br.com.alura.ecommerce;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class KafkaService<T> implements Closeable {

    private final ConsumerFunction parse;
    private final KafkaConsumer<String, T> consumer;
    private final Class<T> clazz;

    public KafkaService(String groupId, String topic, ConsumerFunction parse, Class<T> clazz, Map<String, String> properties) {
        this(groupId, parse, clazz, properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public KafkaService(String groupId, Pattern topic, ConsumerFunction parse, Class<T> clazz, Map<String, String> properties) {
        this(groupId, parse, clazz, properties);
        consumer.subscribe(topic);
    }

    private KafkaService(String groupId, ConsumerFunction parse, Class<T> clazz, Map<String, String> properties) {
        this.parse = parse;
        this.consumer = new KafkaConsumer<>(properties(clazz, groupId, properties));
        this.clazz = clazz;
    }

    public void run() {
        while (true) {
            final var records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                System.out.println("Encontrei registros " + records.count());
                for (var record : records) {
                    parse.consume(record);
                }

            }

        }
    }


    private Properties properties(Class<T> clazz, String groupId, Map<String, String> newProperties) {
        var properties = new Properties();

        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GsonDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(GsonDeserializer.TYPE_CONFIG, clazz.getName());
        properties.putAll(newProperties);

        return properties;
    }

    @Override
    public void close() {
        consumer.close();
    }
}
