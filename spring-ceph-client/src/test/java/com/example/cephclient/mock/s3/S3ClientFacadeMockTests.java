package com.example.cephclient.mock.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cephclient.s3.facade.S3ClientFacade;

import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListPartsRequest;
import software.amazon.awssdk.services.s3.model.ListPartsResponse;
import software.amazon.awssdk.services.s3.model.Part;

@ExtendWith(MockitoExtension.class)
class S3ClientFacadeMockTests {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private S3ClientFacade facade;

    @Test
    void createBucket_ShouldReturnTrue_WhenSdkResponseIsSuccessful() {
        String bucketName = "unit-test-bucket";

        CreateBucketResponse createBucketResponse = mock(CreateBucketResponse.class);
        SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);
        when(createBucketResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);

        when(s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()))
            .thenReturn(createBucketResponse);

        boolean result = facade.createBucket(bucketName);

        ArgumentCaptor<CreateBucketRequest> captor = ArgumentCaptor.forClass(CreateBucketRequest.class);
        verify(s3Client, times(1)).createBucket(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(bucketName);
        assertThat(result).isTrue();
    }

    @Test
    void createBucket_ShouldReturnFalse_WhenResponseIsNull() {
        String bucketName = "unit-test-bucket";

        when(s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()))
            .thenReturn(null);

        boolean result = facade.createBucket(bucketName);

        assertThat(result).isFalse();
    }

    @Test
    void createBucket_ShouldReturnFalse_WhenExceptionOccurs() {
        String bucketName = "unit-test-bucket";

        when(s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build()))
            .thenThrow(new RuntimeException("boom"));

        boolean result = facade.createBucket(bucketName);

        assertThat(result).isFalse();
    }

    @Test
    void listBucketNames_ShouldReturnBucketNameList() {
        when(s3Client.listBuckets(ListBucketsRequest.builder().build()))
            .thenReturn(
                ListBucketsResponse.builder()
                    .buckets(
                        Bucket.builder().name("bucket-a").build(),
                        Bucket.builder().name("bucket-b").build()
                    )
                    .build()
            );

        List<String> result = facade.listBucketNames();

        assertThat(result).containsExactly("bucket-a", "bucket-b");
        verify(s3Client, times(1)).listBuckets(ListBucketsRequest.builder().build());
    }

    @Test
    void listParts_ShouldReturnPartMetadataList() {
        String bucketName = "unit-test-bucket";
        String key = "large-file.zip";
        String uploadId = "upload-id-1";

        when(s3Client.listParts(
            ListPartsRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .build()
        )).thenReturn(
            ListPartsResponse.builder()
                .parts(
                    Part.builder().partNumber(1).eTag("etag-1").size(1024L).build(),
                    Part.builder().partNumber(2).eTag("etag-2").size(2048L).build()
                )
                .build()
        );

        List<S3ClientFacade.MultipartPartInfo> result = facade.listParts(bucketName, key, uploadId);

        assertThat(result)
            .extracting(S3ClientFacade.MultipartPartInfo::partNumber, S3ClientFacade.MultipartPartInfo::eTag, S3ClientFacade.MultipartPartInfo::size)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, "etag-1", 1024L),
                org.assertj.core.groups.Tuple.tuple(2, "etag-2", 2048L)
            );
        verify(s3Client, times(1)).listParts(
            ListPartsRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .build()
        );
    }

    @Test
    void listParts_ShouldReturnEmptyList_WhenResponseIsNull() {
        String bucketName = "unit-test-bucket";
        String key = "large-file.zip";
        String uploadId = "upload-id-1";

        when(s3Client.listParts(
            ListPartsRequest.builder()
                .bucket(bucketName)
                .key(key)
                .uploadId(uploadId)
                .build()
        )).thenReturn(null);

        List<S3ClientFacade.MultipartPartInfo> result = facade.listParts(bucketName, key, uploadId);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteBucket_ShouldReturnTrue_WhenSdkResponseIsSuccessful() {
        String bucketName = "unit-test-bucket";

        DeleteBucketResponse deleteBucketResponse = mock(DeleteBucketResponse.class);
        SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);
        when(deleteBucketResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);

        when(s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build()))
            .thenReturn(deleteBucketResponse);

        boolean result = facade.deleteBucket(bucketName);

        ArgumentCaptor<DeleteBucketRequest> captor = ArgumentCaptor.forClass(DeleteBucketRequest.class);
        verify(s3Client, times(1)).deleteBucket(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(bucketName);
        assertThat(result).isTrue();
    }

    @Test
    void deleteBucket_ShouldReturnFalse_WhenExceptionOccurs() {
        String bucketName = "unit-test-bucket";

        when(s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build()))
            .thenThrow(new RuntimeException("boom"));

        boolean result = facade.deleteBucket(bucketName);

        assertThat(result).isFalse();
    }

    @Test
    void deleteObject_ShouldReturnTrue_WhenSdkResponseIsSuccessful() {
        String bucketName = "unit-test-bucket";
        String key = "test-object";

        DeleteObjectResponse deleteObjectResponse = mock(DeleteObjectResponse.class);
        SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
        when(sdkHttpResponse.isSuccessful()).thenReturn(true);
        when(deleteObjectResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);

        when(s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build()))
            .thenReturn(deleteObjectResponse);

        boolean result = facade.deleteObject(bucketName, key);

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client, times(1)).deleteObject(captor.capture());
        assertThat(captor.getValue().bucket()).isEqualTo(bucketName);
        assertThat(captor.getValue().key()).isEqualTo(key);
        assertThat(result).isTrue();
    }

    @Test
    void deleteObject_ShouldReturnFalse_WhenExceptionOccurs() {
        String bucketName = "unit-test-bucket";
        String key = "test-object";

        when(s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build()))
            .thenThrow(new RuntimeException("boom"));

        boolean result = facade.deleteObject(bucketName, key);

        assertThat(result).isFalse();
    }
}
