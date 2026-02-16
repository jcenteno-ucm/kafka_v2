# JAVA 

## Objetivo

Asimilar los conceptos mediante el uso del lenguaje java ☕️

## Build

Para construir el proyecto usaremos **maven**. 

Podemos usar nuestro IDE de referencia o la línea de comando.

```bash
mvn clean install
```

## Simple Client

### Producer API

Las clases relativas a la producción son **Producer** y **ProducerApp**

Del mismo modo, para ejecutar la aplicación productora podemos usar nuestro IDE o la linea de comandos

```bash
mvn dependency:copy-dependencies
```

```bash
java -Djava.security.manager=allow -cp "target/classes:target/dependency/*" com.ucmmaster.kafka.simple.ProducerApp    
```

Deberías empezar a ver logs en la pantalla

### Consumer API

Las clases relativas a la producción son **Consumer** y **ConsumerApp**

En este caso abre una nueva terminal para poder tener ejecutando a la vez productor y consumidor

```bash
java -Djava.security.manager=allow -cp "target/classes:target/dependency/*" com.ucmmaster.kafka.simple.ConsumerApp    
``` 

Deberías empezar a ver logs en la pantalla

ConsumerApp crea una aplicación consumidora con 2 consumers.

Fíjate en los logs, que los threads **[Thread-0]** y **[Thread-1]** están consumiendo topics de sus respectivas particiones

Si ejecutas la aplicación consumidora desde el IDE, es posible matar unos de estos threads, y por lo tanto podremos ver el rebalanceo de las particiones de ese consumidor al otro

> ❗️ **NOTA**<br/>Para detener una aplicación de consola debemos pulsar **Ctrl+C**
 
> 📚 **NOTA**<br/>La configuración tanto del productor como del consumidor están externalizadas en el fichero **simple-client.properties**

> 💊 **NOTA**<br/>Lee el código de todas las clases<br/>Analiza las clases de [Producer](https://kafka.apache.org/39/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html) y [Consumer](https://kafka.apache.org/39/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html) API <br/> Trata de hacer pequeñas modificaciones

## Avro Client

En este ejemplo, vamos a hacer lo mismo pero esta vez haciendo uso de un **data contract** expresado con un esquema AVRO.

### Data Contract

El contrato de datos se encuentra definido en com.ucmmaster.kafka.data.v1.TemperatureTelemetry.avsc

La generación de la correspondiente clase TemperatureTelemetry.java se hace a través de un plugin maven.

### Producer API

Las clases relativas a la producción son **Producer** y **ProducerAvroApp** dentro del paquete com.ucmmaster.kafka.avro

Del mismo modo, para ejecutar la aplicación productora podemos usar nuestro IDE o la linea de comandos

```bash
mvn dependency:copy-dependencies
```

```bash
java -Djava.security.manager=allow -cp "target/classes:target/dependency/*" com.ucmmaster.kafka.avro.ProducerAvroApp    
```

Deberías empezar a ver logs en la pantalla

### Consumer API

Las clases relativas a la producción son **Consumer** y **ConsumerAvroApp** dentro del paquete com.ucmmaster.kafka.avro

En este caso abre una nueva terminal para poder tener ejecutando a la vez productor y consumidor

```bash
java -Djava.security.manager=allow -cp "target/classes:target/dependency/*" com.ucmmaster.kafka.avro.ConsumerAvroApp    
``` 

### Control Center

Explora a través de [Control Center](http://localhost:9021/clusters/Nk018hRAQFytWskYqtQduw/management/topics/temperature-telemetry-avro/settings) los mensajes y el esquema registrado

### Schema Registry

Explora los siguientes endpoints del Schema Registry:

http://localhost:8081/schemas

http://localhost:8081/subjects

http://localhost:8081/subjects/temperature-telemetry-value/versions

http://localhost:8081/subjects/temperature-telemetry-value/versions/1

### Console Consumer

Vamos a probar a consumir los mensajes desde la herramienta de consola:

¿Qué pasará?

```bash
kafka-console-consumer --bootstrap-server broker-1:29092 --topic temperature-telemetry --property print.key=true    
``` 

<details>
  <summary><b>Solución</b></summary>

¡El mensaje es ilegible. El motivo es que el consumidor de consola, espera que los bytes correspondientes al valor del mensaje sean caracteres textuales pero ahora son datos serializados en avro, que requiere el deserializador correspondiente!.
</details>

### Evolución del Data Contract

Vamos a evolucionar el contrato de datos añadiendo un nuevo campo `humidty` y se encuentra definido en com.ucmmaster.kafka.data.v2.TemperatureTelemetry.avsc

Vamos a la clase com.ucmmaster.kafka.avro.Producer y cambiamos lo siguiente:

```java
import com.ucmmaster.kafka.data.v1.TemperatureTelemetry;
```

por la clase de la v2:

```java
import com.ucmmaster.kafka.data.v2.TemperatureTelemetry;
```

cambia también el método 

```java
protected TemperatureTelemetry createRandomTemperatureTelemetry() {
    int id = random.ints(1, 10).findFirst().getAsInt();
    int temperature = random.ints(15, 40).findFirst().getAsInt();
    return new TemperatureTelemetry(id,temperature);
}
```

por este otro:

```java
protected TemperatureTelemetry createRandomTemperatureTelemetry() {
    int id = random.ints(1, 10).findFirst().getAsInt();
    int temperature = random.ints(15, 40).findFirst().getAsInt();
    int humidity = random.ints(1, 100).findFirst().getAsInt();
    return new TemperatureTelemetry(id,temperature,humidity);
}
```

Arranca de nuevo la aplicación productora y observa los nuevos mensajes producidos

<details>
  <summary><b>Solución</b></summary>

¡Los mensajes llevan el nuevo campo humidity y hay un nuevo schema en el Schema Registry!
¡El consumidor sigue consumiendo los nuevos mensajes, a pesar de que sigue con la v1 del contrato!

</details>

Comprueba el Schema Registry:

http://localhost:8081/subjects/temperature-telemetry-value/versions

http://localhost:8081/subjects/temperature-telemetry-value/versions/2

http://localhost:8081/config

> ❗️ **NOTA**<br/>Para detener una aplicación de consola debemos pulsar **Ctrl+C**

> 📚 **NOTA**<br/>La configuración tanto del productor como del consumidor están externalizadas en el fichero **avro-client.properties**

> 🔎 Presta especial atención a las clases de los **serdes** en el fichero properties, son clases del librerias de Confluent

> 💊 **NOTA**<br/>Lee el código de todas las clases<br/>Analiza las clases de [Producer](https://kafka.apache.org/39/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html) y [Consumer](https://kafka.apache.org/39/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html) API <br/> Trata de hacer pequeñas modificaciones