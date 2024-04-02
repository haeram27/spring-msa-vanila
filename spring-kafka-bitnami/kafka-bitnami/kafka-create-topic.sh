#!/bin/env bash
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --topic 'my-topic' --create --partitions 3 --replication-factor 1
