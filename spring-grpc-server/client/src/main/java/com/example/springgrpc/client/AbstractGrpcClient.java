package com.example.springgrpc.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * gRPC 클라이언트 공통 기반. 채널 수명 관리와 호출 단위 Metadata/데드라인 결합을 담당한다.
 * <p>
 * 하위 클래스는 자기 서비스의 스텁만 들고 있으면 되고, 채널·타임아웃·헤더 변환은 여기서 처리한다.
 */
public abstract class AbstractGrpcClient implements AutoCloseable {

    /** gRPC Metadata 키로 허용되는 문자. 대문자는 허용되지 않는다. */
    private static final String VALID_KEY_PATTERN = "[-_.0-9a-z]+";

    private final ManagedChannel channel;
    private final long timeoutSeconds;

    protected AbstractGrpcClient(ManagedChannel channel, long timeoutSeconds) {
        this.channel = channel;
        this.timeoutSeconds = timeoutSeconds;
    }

    /**
     * 접속 인자를 모두 검증한 뒤 평문 채널을 연다.
     * 하위 클래스의 {@code connect(...)} 팩터리가 생성자에 넘길 채널을 만들 때 쓴다.
     */
    protected static ManagedChannel openChannel(String host, int port, long timeoutSeconds) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host must not be blank");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than 0");
        }
        if (timeoutSeconds <= 0) {
            throw new IllegalArgumentException("timeoutSeconds must be greater than 0");
        }

        return ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build();
    }

    /**
     * 호출 시점에 전달받은 헤더를 gRPC Metadata로 실어 보낸다.
     * Metadata는 스텁이 아니라 호출 단위로 붙여야 하므로, 매 호출마다 인터셉터를 새로 결합한다.
     */
    protected <S extends AbstractStub<S>> S callStub(S stub, Map<String, String> headers) {
        S call = stub.withDeadlineAfter(timeoutSeconds, TimeUnit.SECONDS);

        Metadata metadata = toMetadata(headers);
        if (metadata.keys().isEmpty()) {
            return call;
        }
        return call.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    /**
     * 문자열 헤더 맵을 gRPC Metadata로 변환한다.
     * gRPC 키는 소문자만 허용하고, {@code -bin} 접미사는 바이너리 마샬러를 요구하므로 건너뛴다.
     */
    private static Metadata toMetadata(Map<String, String> headers) {
        Metadata metadata = new Metadata();
        if (headers == null) {
            return metadata;
        }

        headers.forEach((name, value) -> {
            if (name == null || value == null) {
                return;
            }
            String key = name.toLowerCase(Locale.ROOT);
            if (!key.matches(VALID_KEY_PATTERN) || key.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                return;
            }
            metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
        });
        return metadata;
    }

    @Override
    public void close() throws InterruptedException {
        channel.shutdown();
        channel.awaitTermination(5, TimeUnit.SECONDS);
    }
}
