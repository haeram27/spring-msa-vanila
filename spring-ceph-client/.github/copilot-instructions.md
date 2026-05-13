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
- AI 채팅/터미널 실행 운영 규칙은 [ai-chat.instructions.md](.github/ai-chat.instructions.md)를 따른다.

## AI Chat Policy Summary
- 터미널 명령은 기본적으로 백그라운드(비동기) 실행 후 즉시 사용자에게 주도권을 반환한다.
- 동기 실행은 기본 타임아웃 10초를 적용한다.
- 10초 초과 시 백그라운드로 전환하고 현재 로그 요약을 먼저 보고한다.
- 완료 신호(green/idle)를 받으면 절대로 추가 대기하지 않고 즉시 결과를 확인해 전달한다.
