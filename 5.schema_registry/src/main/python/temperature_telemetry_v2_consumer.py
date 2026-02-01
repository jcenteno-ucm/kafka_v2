import logging
from confluent_kafka import DeserializingConsumer
from confluent_kafka.schema_registry import SchemaRegistryClient
from confluent_kafka.schema_registry.avro import AvroDeserializer
from confluent_kafka.serialization import StringDeserializer
from datetime import datetime, timedelta

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

schema_registry_config = {'url': 'http://localhost:8081'}
schema_registry_client = SchemaRegistryClient(schema_registry_config)

schema_str = None
schema_path = '../avro/com.ucmmaster.kafka.data.v2.TemperatureTelemetry.avsc'
with open(schema_path, "r") as f:
    schema_str = f.read()

value_deserializer = AvroDeserializer(
    schema_registry_client=schema_registry_client,
    schema_str=schema_str
)

conf = {
        'bootstrap.servers': 'localhost:9092',
        'group.id': 'temperature-telemetry-group-v2',
        'auto.offset.reset': 'earliest',
        'enable.auto.commit': True,
        'key.deserializer': StringDeserializer('utf_8'),
        'value.deserializer': value_deserializer
 }

consumer = DeserializingConsumer(conf)
topic = 'temperature-telemetry'
consumer.subscribe([topic])
print(f"Consumiendo mensajes del topic '{topic}' con esquema v2...\n")

try:
    while True:
        msg = consumer.poll(timeout=1.0)

        if msg is None:
            continue

        if msg.error():
            if msg.error().code() == KafkaError._PARTITION_EOF:
                print("Llegamos al final de la partición")
            else:
                print(f"Error: {msg.error()}")
                break
        else:
            key = msg.key()
            value = msg.value()

            print(f"→ Mensaje recibido:")
            print(f"   id:         {value['id']}")
            print(f"   temperatura: {value['temperature']}")
            print(f"   humidity: {value['humidity']}")
            print("-" * 60)

except KeyboardInterrupt:
    print("\nDetenido por el usuario")
finally:
    consumer.close()
