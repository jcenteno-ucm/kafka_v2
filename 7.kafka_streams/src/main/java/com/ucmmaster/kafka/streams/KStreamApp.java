package com.ucmmaster.kafka.streams;

import com.ucmmaster.kafka.data.v2.TemperatureTelemetry;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class KStreamApp {

    public static void main(String[] args) throws IOException {

        // Cargamos la configuración
        Properties props = new Properties();
        String config = "streams.properties";
        try (InputStream fis = KStreamApp.class.getClassLoader().getResourceAsStream(config)) {
            props.load(fis);
        }
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-app");

        final String inputTopic = "temperature-telemetry-avro";
        final String outputTopic = "temperature-telemetry-high-temperature";

        //Creamos un Serde de tipo Avro ya que el productor produce <String,TemperatureTelemetry>
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://localhost:8081");
        Serde<TemperatureTelemetry> temperatureTelemetrySerde = new SpecificAvroSerde();
        temperatureTelemetrySerde.configure(serdeConfig, false);

        //Creamos el KStream mediante el builder
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, TemperatureTelemetry> firstStream = builder.stream(inputTopic, Consumed.with(Serdes.String(), temperatureTelemetrySerde));

        //Filtramos los eventos con temperatura >= 30 grados
        firstStream
                .peek((key, value) -> System.out.println("Incoming record - key " + key + " value " + value))
                .filter((key, value) -> value.getTemperature() >= 30)
                .peek((key, value) -> System.out.println("Outgoing record - key " + key + " value " + value))
                .to(outputTopic, Produced.with(Serdes.String(), temperatureTelemetrySerde));

        // Iniciar Kafka Streams
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Parada controlada en caso de apagado
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}