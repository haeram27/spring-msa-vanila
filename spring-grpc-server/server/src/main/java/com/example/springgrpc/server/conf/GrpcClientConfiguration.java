package com.example.springgrpc.server.conf;

import com.example.springgrpc.client.HelloGrpcClient;
import com.example.springgrpc.client.ListViewSampleGrpcClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfiguration {

    /**
     * 접속 대상이 비어 있으면 기동을 실패시킨다.
     * <p>
     * product 프로파일은 {@code host}를 환경변수로만 받는다({@code ${GRPC_CLIENT_HOST:}}).
     * 이 검사가 없으면 빈 값이 채널 생성까지 내려가 {@code URISyntaxException} 같은
     * 무관해 보이는 예외로 드러나, 정작 무엇이 빠졌는지 알 수 없다.
     */
    private static void requireHost(GrpcClientProperties properties) {
        if (properties.getHost() == null || properties.getHost().isBlank()) {
            throw new IllegalStateException(
                "sample.grpc.client.host is not set - product 프로파일은 GRPC_CLIENT_HOST 환경변수를 요구한다");
        }
    }

    @Bean(destroyMethod = "close")
    public HelloGrpcClient helloGrpcClient(GrpcClientProperties properties) {
        requireHost(properties);
        return HelloGrpcClient.connect(
            properties.getHost(),
            properties.getPort(),
            properties.getTimeoutSeconds()
        );
    }

    @Bean(destroyMethod = "close")
    public ListViewSampleGrpcClient listViewSampleGrpcClient(GrpcClientProperties properties) {
        requireHost(properties);
        return ListViewSampleGrpcClient.connect(
            properties.getHost(),
            properties.getPort(),
            properties.getTimeoutSeconds()
        );
    }
}
