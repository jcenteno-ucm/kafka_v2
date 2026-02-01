import random
import logging
from time import time
from confluent_kafka import SerializingProducer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroSerializer
from confluent_kafka.serialization import StringSerializer

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

schema_registry_config = {'url': 'http://localhost:8081'}
schema_registry_client = SchemaRegistryClient(schema_registry_config)

schema_str = None
schema_path = '../avro/com.ucmmaster.kafka.data.v3.TemperatureTelemetry.avsc'
with open(schema_path, "r") as f:
    schema_str = f.read()

value_serializer = AvroSerializer(
    schema_registry_client=schema_registry_client,
    schema_str=schema_str,
    conf={
        'auto.register.schemas': True
    }
)

producer = SerializingProducer({
    'bootstrap.servers': 'localhost:9092',
    'partitioner': 'murmur2_random',
    'key.serializer': StringSerializer('utf_8'),
    'value.serializer': value_serializer,
})

data = {
    "id": random.randint(1, 10),
    "temperature": random.randint(1, 40),
    "humidity": random.randint(10, 80),
    "read_at": int(time())
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