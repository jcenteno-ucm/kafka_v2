package com.ucmmaster.kafka.avro;

public class ProducerAvroApp {

    public static void main(String[] args) {
        String config = "avro-client.properties";
        String topic  = "temperature-telemetry";
        Producer producer = new Producer(config);
        producer.produce(topic);
    }

}
