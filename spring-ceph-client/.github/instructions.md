---
applyTo: "**"
description: "Project-level overview for the Java Ceph client repository; references detailed Java project rule instructions"
---

# Project Overview

이 저장소는 Java Spring 기반 Ceph client 프로젝트다.

## Project Context
- Ceph RGW(S3 호환) 연동을 중심으로 동작한다.
- AWS SDK for Java v2 및 radosgw-admin4j 연동 구성을 포함한다.
- 테스트는 단위 테스트와 실제 RGW 연동 통합 테스트를 함께 사용한다.

## Referenced Rule File
- Java 개발 규칙은 [java-project-rules.instructions.md](.github/java-project-rules.instructions.md)를 따른다.
