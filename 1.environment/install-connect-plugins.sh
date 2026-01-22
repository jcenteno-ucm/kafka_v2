#/bin/bash

docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:latest

docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest

#copia el driver jdbc mysql dentro del contenedor connect
docker cp ./mysql/mysql-connector-java-5.1.45.jar connect:/usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib/mysql-connector-java-5.1.45.jar

#docker compose exec connect confluent-hub install --no-prompt mongodb/kafka-connect-mongodb:latest
#docker compose exec connect confluent-hub install --no-prompt neo4j/kafka-connect-neo4j:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-elasticsearch:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-hdfs:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-cassandra:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-azure-blob-storage:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-jms:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-file:latest
#docker compose exec connect confluent-hub install --no-prompt confluentinc/kafka-connect-ftp:latest

docker compose restart connect

echo "Esperando 30s a que connect reinicie"

sleep 30
