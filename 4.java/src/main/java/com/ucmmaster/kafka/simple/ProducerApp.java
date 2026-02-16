package com.ucmmaster.kafka.simple;

public class ProducerApp {

    public static void main(String[] args) {
        String config = "simple-client.properties";
        String topic  = "temperature-telemetry-simple";
        Producer producer = new Producer(config);
        producer.produce(topic);
    }

}
