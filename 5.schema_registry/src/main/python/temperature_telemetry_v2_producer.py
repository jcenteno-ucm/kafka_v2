import random
import logging
from confluent_kafka import SerializingProducer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import StringSerializer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

schema_registry_config = {'url': 'http://localhost:8081'}
schema_registry_client = SchemaRegistryClient(schema_registry_config)

value_serializer = AvroSerializer(
    schema_registry_client=schema_registry_client,
    conf={
        'auto.register.schemas': False,
        'use.latest.version': True
    }
)

producer = SerializingProducer({
    'bootstrap.servers': 'localhost:9092,localhost:9093,localhost:9094',
    'partitioner': 'murmur2_random',
    'key.serializer': StringSerializer('utf_8'),
    'value.serializer': value_serializer,
})

data = {
    "id": random.randint(1, 10),
    "temperature": random.randint(1, 40),
    "humidity": random.randint(10, 80)
}

def delivery_report(err, msg):
    if err is not None:
        logger.error(f"Message delivery failed: {err}")
    else:
        logger.info(f"Message delivered to {msg.topic()} [{msg.partition()}] @ offset {msg.offset()}")

producer.produce(
    topic='temperature-telemetry',
    key=str(data['id']),
    value=data,
    on_delivery=delivery_report
)

producer.flush(timeout=10.0)
logger.info("Flush completed.")