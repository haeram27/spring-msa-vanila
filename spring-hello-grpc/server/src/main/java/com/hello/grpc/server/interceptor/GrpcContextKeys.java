package com.hello.grpc.server.interceptor;

import io.grpc.Context;

public final class GrpcContextKeys {
    // Context에 바인딩할 키 정의
    public static final Context.Key<String> USER_ID_KEY = Context.key("current-user-id");
    
    private GrpcContextKeys() {}
}
