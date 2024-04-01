#!/bin/env bash
docker compose exec -it kafka kafka-console-producer --bootstrap-server localhost:9092 --topic 'my-topic'
