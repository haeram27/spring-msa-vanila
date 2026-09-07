package com.example.springgrpc.server.repository.enums;

/**
 * 스케줄 반복 주기.
 * <p>
 * {@link Status}와 같은 이유로 {@code *_UNSPECIFIED}를 두지 않는다.
 */
public enum Frequency {
    IMMEDIATE, SPECIFIC_TIME, EVERY_DAY, EVERY_WEEK, EVERY_MONTH_DAY, EVERY_YEAR
}
