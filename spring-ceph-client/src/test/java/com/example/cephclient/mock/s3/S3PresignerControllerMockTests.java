package com.example.cephclient.mock.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cephclient.s3.controller.S3PresignerController;
import com.example.cephclient.s3.facade.S3PresignerFacade;

import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@ExtendWith(MockitoExtension.class)
class S3PresignerControllerMockTests {

    @Mock
    private S3PresignerFacade s3PresignerFacade;

    @InjectMocks
    private S3PresignerController controller;

    @Test
    void presignPutObjectBulk_ValidRequests_ReturnsPresignResults() {
        PresignedPutObjectRequest presigned1 = mock(PresignedPutObjectRequest.class);
        PresignedPutObjectRequest presigned2 = mock(PresignedPutObjectRequest.class);
        mockPresignedPut(presigned1, "https://ceph.local/put-1");
        mockPresignedPut(presigned2, "https://ceph.local/put-2");

        List<S3PresignerFacade.PutObjectUrlRequest> requests = List.of(
            new S3PresignerFacade.PutObjectUrlRequest("bucket", "key-1", "text/plain", 100L, 300),
            new S3PresignerFacade.PutObjectUrlRequest("bucket", "key-2", "text/plain", 200L, 300)
        );

        when(s3PresignerFacade.presignPutObjectBulk(requests)).thenReturn(List.of(presigned1, presigned2));

        List<S3PresignerFacade.PresignResult> results = controller.presignPutObjectBulk(requests);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).url()).isEqualTo("https://ceph.local/put-1");
        assertThat(results.get(1).url()).isEqualTo("https://ceph.local/put-2");
        assertThat(results.get(0).method()).isEqualTo("PUT");
        assertThat(results.get(1).method()).isEqualTo("PUT");
        verify(s3PresignerFacade).presignPutObjectBulk(requests);
    }

    @Test
    void presignPutObjectBulk_EmptyRequests_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> controller.presignPutObjectBulk(List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("requests must not be empty");
    }

    @Test
    void presignUploadPartBulk_ValidRequest_ReturnsResult() {
        S3PresignerFacade.PresignUploadPartBulkRequest request =
            new S3PresignerFacade.PresignUploadPartBulkRequest("bucket", "key", "upload-id", 2, 300);

        List<S3PresignerFacade.PresignResult> expected = List.of(
            new S3PresignerFacade.PresignResult("https://ceph.local/multipart-part-1", "PUT", java.util.Map.of(), Instant.now().plusSeconds(60)),
            new S3PresignerFacade.PresignResult("https://ceph.local/multipart-part-2", "PUT", java.util.Map.of(), Instant.now().plusSeconds(60))
        );

        when(s3PresignerFacade.presignUploadPartBulk(request)).thenReturn(expected);

        List<S3PresignerFacade.PresignResult> result = controller.presignUploadPartBulk(request);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).method()).isEqualTo("PUT");
        verify(s3PresignerFacade).presignUploadPartBulk(request);
    }

    private void mockPresignedPut(PresignedPutObjectRequest request, String url) {
        SdkHttpRequest httpRequest = SdkHttpRequest.builder()
            .method(SdkHttpMethod.PUT)
            .uri(URI.create(url))
            .putHeader("x-test", "v")
            .build();

        try {
            when(request.url()).thenReturn(URI.create(url).toURL());
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid url: " + url, e);
        }
        when(request.httpRequest()).thenReturn(httpRequest);
        when(request.expiration()).thenReturn(Instant.now().plusSeconds(60));
    }
}
