#!/bin/env bash
docker compose exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic 'my-topic' --from-beginning --partition 2 
