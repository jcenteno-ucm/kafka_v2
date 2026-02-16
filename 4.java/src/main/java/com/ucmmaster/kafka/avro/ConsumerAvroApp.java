package com.ucmmaster.kafka.avro;

public class ConsumerAvroApp {

  public static void createConsumer(String name, String config, String topic) {
    Consumer consumer = new Consumer(name,config);
    consumer.consume(topic);
  }

  public static void main(String[] args) {
    String config = "avro-client.properties";
    String topic  = "temperature-telemetry";

    Thread consumerThread1 = new Thread(() -> createConsumer("consumer-0",config,topic));
    Thread consumerThread2 = new Thread(() -> createConsumer("consumer-1",config,topic));

    consumerThread1.start();
    consumerThread2.start();
  }

}
