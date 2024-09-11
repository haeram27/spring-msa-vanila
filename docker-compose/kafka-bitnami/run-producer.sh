#!/bin/env bash
docker compose exec -it kafka kafka-console-producer.sh --bootstrap-server localhost:9092 --topic 'my-topic'
