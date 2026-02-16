package com.ucmmaster.kafka.simple;

public class ConsumerApp {

  public static void createConsumer(String name, String config, String topic) {
    Consumer consumer = new Consumer(name,config);
    consumer.consume(topic);
  }

  public static void main(String[] args) {
    String config = "simple-client.properties";
    String topic  = "temperature-telemetry-simple";

    Thread consumerThread1 = new Thread(() -> createConsumer("consumer-0",config,topic));
    Thread consumerThread2 = new Thread(() -> createConsumer("consumer-1",config,topic));
    //Thread consumerThread3 = new Thread(() -> createConsumer("consumer-2",config,topic));

    consumerThread1.start();
    consumerThread2.start();
    //consumerThread3.start();
  }

}
