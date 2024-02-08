#!/bin/env bash
docker-compose exec kafka kafka-topics --bootstrap-server localhost:9092 --topic my_topic --create --partitions 3 --replication-factor 1
