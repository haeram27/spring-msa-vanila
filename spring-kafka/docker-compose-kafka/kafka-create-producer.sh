#!/bin/env bash
docker-compose exec kafka kafka-console-producer --bootstrap-server localhost:9092 --topic my_topic
