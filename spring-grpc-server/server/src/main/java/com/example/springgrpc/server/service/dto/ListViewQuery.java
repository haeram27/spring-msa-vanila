package com.example.springgrpc.server.service.dto;

/**
 * 목록 조회 조건. proto {@code ListViewRequest}의 도메인 대응물이다.
 *
 * @param filter     적용할 필터. {@code null}이면 전체 조회
 * @param pagination 페이징. {@code null}이면 기본값 적용
 * @param sort       정렬. {@code null}이면 {@code id} 오름차순
 */
public record ListViewQuery(ListViewFilter filter, Pagination pagination, Sort sort) {

    /** 조회 조건이 없는 전체 목록 질의. */
    public static ListViewQuery all() {
        return new ListViewQuery(null, null, null);
    }

    /**
     * @param pageSize   0 이하이면 서비스 기본값을 쓴다
     * @param pageNumber 1부터 시작. 0 이하이면 첫 페이지로 본다
     */
    public record Pagination(int pageSize, int pageNumber) {
    }

    public record Sort(Key key, Order order) {
        public enum Key {
            ID, NAME, STATUS, CATEGORY, FREQUENCY
        }

        public enum Order {
            ASC, DESC
        }
    }
}
