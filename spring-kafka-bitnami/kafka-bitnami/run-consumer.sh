#!/bin/env bash
docker compose exec kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic 'my-topic' --from-beginning --group 'group1'
