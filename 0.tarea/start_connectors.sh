#/bin/bash

echo "Lanzando conectores"

curl -d @"./connectors/source-datagen-_transactions.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors | jq

curl -d @"./connectors/sink-mysql-_transactions.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors | jq

#curl -d @"./connectors/source-datagen-sensor-telemetry.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors | jq

#curl -d @"./connectors/source-mysql-sales_transactions.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors | jq

#curl -d @"./connectors/sink-mongodb-sensor_alerts.json" -H "Content-Type: application/json" -X POST http://localhost:8083/connectors | jq

echo "OK"

