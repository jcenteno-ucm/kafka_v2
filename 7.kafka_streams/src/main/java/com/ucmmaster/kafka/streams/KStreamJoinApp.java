package com.ucmmaster.kafka.streams;

import com.ucmmaster.kafka.data.v1.Device;
import com.ucmmaster.kafka.data.v1.TemperatureAlert;
import com.ucmmaster.kafka.data.v1.TemperatureTelemetry;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class KStreamJoinApp {

    private static final Logger logger = LoggerFactory.getLogger(KStreamJoinApp.class.getName());

    private static Topology createTopology() {

        final String outputTopic = "temperature-telemetry-critical-temperature";

        //Creamos un Serde de tipo Avro ya que el productor produce <String,TemperatureTelemetry>
        final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url", "http://localhost:8081");

        Serde<TemperatureTelemetry> temperatureSerde = new SpecificAvroSerde<>();
        temperatureSerde.configure(serdeConfig, false);

        Serde<Device> deviceSerde = new SpecificAvroSerde<>();
        deviceSerde.configure(serdeConfig, false);

        Serde<TemperatureAlert> alertSerde = new SpecificAvroSerde<>();
        alertSerde.configure(serdeConfig, false);

        //Creamos el KStream mediante el builder
        StreamsBuilder builder = new StreamsBuilder();

        KStream<String, TemperatureTelemetry> telemetry = builder.stream(
                "temperature-telemetry",
                Consumed.with(Serdes.String(), temperatureSerde)
        );

        KTable<String, Device> deviceTable = builder.table(
                "devices",
                Consumed.with(Serdes.String(), deviceSerde)
        );

        // como es un join entre un KStream y un KTable no hace falta usar ventana
        telemetry
                .join(
                        deviceTable,
                        (temp, device) -> {
                            int excess = temp.getTemperature() - device.getTemperatureThreshold();
                            if (excess <= 0) return null;
                            TemperatureAlert alert = new TemperatureAlert();
                            alert.setId(temp.getId());
                            alert.setTemperature(temp.getTemperature());
                            alert.setTemperatureThreshold(device.getTemperatureThreshold());
                            alert.setExcess(excess);
                            alert.setAlertLevel(excess >= 5 ? "HIGH" : "WARNING");
                            alert.setAlertTimestamp(LocalDateTime.now());
                            return alert;
                        },
                        Joined.with(Serdes.String(), temperatureSerde, deviceSerde)
                )
                .filter((k, v) -> v != null)
                .to(outputTopic, Produced.with(Serdes.String(), alertSerde));

        return builder.build();
    }

    public static void main(String[] args) throws IOException {

        // Cargamos la configuración
        Properties props = ConfigLoader.getProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kstream-join-app");

        // Creamos la topologia
        Topology topology = createTopology();

        KafkaStreams streams = new KafkaStreams(topology, props);
        // Iniciar Kafka Streams
        streams.start();
        // Parada controlada en caso de apagado
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));

    }
}