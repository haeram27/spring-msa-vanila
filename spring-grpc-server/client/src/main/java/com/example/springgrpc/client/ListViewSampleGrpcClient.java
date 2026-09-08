package com.example.springgrpc.client;

import com.example.springgrpc.api.ListViewSampleRequest;
import com.example.springgrpc.api.ListViewResponse;
import com.example.springgrpc.api.ListViewSampleGrpc;

import io.grpc.ManagedChannel;

import java.util.Map;

/**
 * {@code com.example.grpc.v1.ListViewSample} 서비스 호출용 클라이언트.
 * <p>
 * {@link HelloGrpcClient}가 {@code String} 하나를 받아 요청 메시지를 대신 조립하는 것과 달리,
 * 여기서는 {@link ListViewSampleRequest}를 그대로 받는다. 필터가 재귀 트리라 원시 타입으로 감싸면
 * proto 빌더를 그대로 베낀 API가 하나 더 생길 뿐이기 때문이다.
 */
public final class ListViewSampleGrpcClient extends AbstractGrpcClient {

    private final ListViewSampleGrpc.ListViewSampleBlockingStub stub;

    private ListViewSampleGrpcClient(ManagedChannel channel, long timeoutSeconds) {
        super(channel, timeoutSeconds);
        this.stub = ListViewSampleGrpc.newBlockingStub(channel);
    }

    public static ListViewSampleGrpcClient connect(String host, int port) {
        return connect(host, port, 5);
    }

    public static ListViewSampleGrpcClient connect(String host, int port, long timeoutSeconds) {
        return new ListViewSampleGrpcClient(openChannel(host, port, timeoutSeconds), timeoutSeconds);
    }

    public ListViewResponse listAll(ListViewSampleRequest request) {
        return listAll(request, Map.of());
    }

    public ListViewResponse listAll(ListViewSampleRequest request, Map<String, String> headers) {
        if (request == null) {
            throw new IllegalArgumentException("request must not be null");
        }

        return callStub(stub, headers).listAll(request);
    }
}
