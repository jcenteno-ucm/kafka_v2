package com.ucmmaster.kafka.streams;

import com.ucmmaster.kafka.data.v2.TemperatureTelemetry;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class KStreamAggApp {

    private static final Logger logger = LoggerFactory.getLogger(KStreamAggApp.class.getName());

    public static void main(String[] args) throws IOException {
        // Cargamos la configuración
        Properties props = ConfigLoader.getProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-agg-app");

        final String inputTopic = "temperature-telemetry-avro";
        final String outputTopic = "temperature-telemetry-max";

        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://localhost:8081");
        //Creamos un Serde de tipo Avro ya que el productor produce <String,TemperatureTelemetry>
        Serde<TemperatureTelemetry> temperatureTelemetrySerde = new SpecificAvroSerde<>();
        temperatureTelemetrySerde.configure(serdeConfig, false);

        Serde<GenericRecord>  genericSerde = new GenericAvroSerde();
        genericSerde.configure(serdeConfig, false);

        //Creamos el KStream mediante el builder
        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(inputTopic, Consumed.with(Serdes.String(), temperatureTelemetrySerde))
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(1)))
                .aggregate(
                        () -> 0, // Valor inicial
                        (k, v, total) -> Math.max(v.getTemperature(),total), // Agregar ingresos
                         Materialized.with(Serdes.String(), Serdes.Integer())
                )
                .toStream()
                .map((wk, value) -> KeyValue.pair(wk.key(),String.valueOf(value)))
                .peek((key, value) -> System.out.println("Outgoing record - key " + key + " value " + value))
                .to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        try(KafkaStreams streams = new KafkaStreams(builder.build(), props)){
            // Iniciar Kafka Streams
            streams.start();
            // Parada controlada en caso de apagado
            Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        }catch (IllegalStateException ex){
            logger.error(ex.getMessage());
        }
    }
}
