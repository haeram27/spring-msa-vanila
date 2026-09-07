package com.example.springgrpc.server.grpc.interceptor;

import java.util.Collections;
import java.util.Map;

import io.grpc.Context;

public final class GrpcRequestHeaderContext {
    private static final Context.Key<Map<String, String>> REQUEST_HEADERS = Context.key("request-headers");

    private GrpcRequestHeaderContext() {
    }

    public static Context withHeaders(Context base, Map<String, String> headers) {
        return base.withValue(REQUEST_HEADERS, Map.copyOf(headers));
    }

    public static Map<String, String> currentHeaders() {
        Map<String, String> headers = REQUEST_HEADERS.get();
        if (headers == null) {
            return Collections.emptyMap();
        }
        return headers;
    }
}
