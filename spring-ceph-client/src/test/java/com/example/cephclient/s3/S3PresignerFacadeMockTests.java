package com.example.cephclient.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cephclient.s3.S3PresignerFacade;

import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.AbortMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.CompleteMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.CreateMultipartUploadPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadBucketPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.HeadObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedAbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedCompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedCreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedHeadBucketRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedHeadObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedUploadPartRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.UploadPartPresignRequest;

@ExtendWith(MockitoExtension.class)
class S3PresignerFacadeMockTests {

    @Mock
    private S3Presigner s3Presigner;

    @InjectMocks
    private S3PresignerFacade facade;

    @Test
    void presignPutObject_ShouldReturnPresignResult() {
        PresignedPutObjectRequest presigned = mock(PresignedPutObjectRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/put", SdkHttpMethod.PUT);
        when(s3Presigner.presignPutObject(org.mockito.ArgumentMatchers.any(PutObjectPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignPutObject(
            new S3PresignerFacade.PutObjectUrlRequest("b", "k", "text/plain", 100L, 120)
        );

        assertThat(result.method()).isEqualTo("PUT");
        assertThat(result.url()).isEqualTo("https://ceph.local/put");
    }

    @Test
    void presignGetObject_ShouldReturnPresignResult() {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/get", SdkHttpMethod.GET);
        when(s3Presigner.presignGetObject(org.mockito.ArgumentMatchers.any(GetObjectPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignGetObject(
            new S3PresignerFacade.GetObjectUrlRequest("b", "k", "application/octet-stream", 120)
        );

        assertThat(result.method()).isEqualTo("GET");
        assertThat(result.url()).isEqualTo("https://ceph.local/get");
    }

    @Test
    void presignDeleteObject_ShouldReturnPresignResult() {
        PresignedDeleteObjectRequest presigned = mock(PresignedDeleteObjectRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/delete", SdkHttpMethod.DELETE);
        when(s3Presigner.presignDeleteObject(org.mockito.ArgumentMatchers.any(DeleteObjectPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignDeleteObject(
            new S3PresignerFacade.DeleteObjectUrlRequest("b", "k", 60)
        );

        assertThat(result.method()).isEqualTo("DELETE");
    }

    @Test
    void presignHeadObject_ShouldReturnPresignResult() {
        PresignedHeadObjectRequest presigned = mock(PresignedHeadObjectRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/head-object", SdkHttpMethod.HEAD);
        when(s3Presigner.presignHeadObject(org.mockito.ArgumentMatchers.any(HeadObjectPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignHeadObject(
            new S3PresignerFacade.HeadObjectUrlRequest("b", "k", 120)
        );

        assertThat(result.method()).isEqualTo("HEAD");
        assertThat(result.url()).isEqualTo("https://ceph.local/head-object");
    }

    @Test
    void presignHeadBucket_ShouldReturnPresignResult() {
        PresignedHeadBucketRequest presigned = mock(PresignedHeadBucketRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/head-bucket", SdkHttpMethod.HEAD);
        when(s3Presigner.presignHeadBucket(org.mockito.ArgumentMatchers.any(HeadBucketPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignHeadBucket(
            new S3PresignerFacade.HeadBucketUrlRequest("b", 120)
        );

        assertThat(result.method()).isEqualTo("HEAD");
        assertThat(result.url()).isEqualTo("https://ceph.local/head-bucket");
    }

    @Test
    void presignCreateMultipartUpload_ShouldReturnPresignResult() {
        PresignedCreateMultipartUploadRequest presigned = mock(PresignedCreateMultipartUploadRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/multipart-start", SdkHttpMethod.POST);
        when(s3Presigner.presignCreateMultipartUpload(org.mockito.ArgumentMatchers.any(CreateMultipartUploadPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignCreateMultipartUpload(
            new S3PresignerFacade.CreateMultipartUploadUrlRequest("b", "k", "video/mp4", 300)
        );

        assertThat(result.method()).isEqualTo("POST");
    }

    @Test
    void presignUploadPart_ShouldReturnPresignResult() {
        PresignedUploadPartRequest presigned = mock(PresignedUploadPartRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/multipart-part", SdkHttpMethod.PUT);
        when(s3Presigner.presignUploadPart(org.mockito.ArgumentMatchers.any(UploadPartPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignUploadPart(
            new S3PresignerFacade.UploadPartUrlRequest("b", "k", "upload-id", 1, 300)
        );

        assertThat(result.method()).isEqualTo("PUT");
    }

    @Test
    void presignCompleteMultipartUpload_ShouldReturnPresignResult() {
        PresignedCompleteMultipartUploadRequest presigned = mock(PresignedCompleteMultipartUploadRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/multipart-complete", SdkHttpMethod.POST);
        when(s3Presigner.presignCompleteMultipartUpload(org.mockito.ArgumentMatchers.any(CompleteMultipartUploadPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignCompleteMultipartUpload(
            new S3PresignerFacade.CompleteMultipartUploadUrlRequest(
                "b",
                "k",
                "upload-id",
                List.of(new S3PresignerFacade.PartEtag(1, "etag-1")),
                300
            )
        );

        assertThat(result.method()).isEqualTo("POST");
    }

    @Test
    void presignAbortMultipartUpload_ShouldReturnPresignResult() {
        PresignedAbortMultipartUploadRequest presigned = mock(PresignedAbortMultipartUploadRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/multipart-abort", SdkHttpMethod.DELETE);
        when(s3Presigner.presignAbortMultipartUpload(org.mockito.ArgumentMatchers.any(AbortMultipartUploadPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignAbortMultipartUpload(
            new S3PresignerFacade.AbortMultipartUploadUrlRequest("b", "k", "upload-id", 300)
        );

        assertThat(result.method()).isEqualTo("DELETE");
    }

    @Test
    void presignRangeGetObject_ShouldReturnPresignResult() {
        PresignedGetObjectRequest presigned = mock(PresignedGetObjectRequest.class);
        mockPresignedCommon(presigned, "https://ceph.local/range", SdkHttpMethod.GET);
        when(s3Presigner.presignGetObject(org.mockito.ArgumentMatchers.any(GetObjectPresignRequest.class))).thenReturn(presigned);

        S3PresignerFacade.PresignResult result = facade.presignRangeGetObject(
            new S3PresignerFacade.RangeGetObjectUrlRequest("b", "k", 0L, 1023L, 120)
        );

        assertThat(result.method()).isEqualTo("GET");
    }

    private void mockPresignedCommon(Object presignedRequest, String url, SdkHttpMethod method) {
        SdkHttpRequest httpRequest = SdkHttpRequest.builder()
            .method(method)
            .uri(URI.create(url))
            .putHeader("x-test", "v")
            .build();

        if (presignedRequest instanceof PresignedPutObjectRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedGetObjectRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedDeleteObjectRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedCreateMultipartUploadRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedUploadPartRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedCompleteMultipartUploadRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedAbortMultipartUploadRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedHeadObjectRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        } else if (presignedRequest instanceof PresignedHeadBucketRequest req) {
            when(req.url()).thenReturn(toUrl(url));
            when(req.httpRequest()).thenReturn(httpRequest);
            when(req.expiration()).thenReturn(Instant.now().plusSeconds(60));
        }
    }

    private URL toUrl(String rawUrl) {
        try {
            return URI.create(rawUrl).toURL();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid url: " + rawUrl, e);
        }
    }
}
