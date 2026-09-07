package com.example.cephclient.mock.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.cephclient.s3.controller.S3ClientController;
import com.example.cephclient.s3.facade.S3ClientFacade;

@ExtendWith(MockitoExtension.class)
class S3ClientControllerMockTests {

    @Mock
    private S3ClientFacade s3ClientFacade;

    @InjectMocks
    private S3ClientController controller;

    @Test
    void createBucket_ValidBucket_ReturnsSuccessResponse() {
        when(s3ClientFacade.createBucket("unit-test-bucket")).thenReturn(true);

        S3ClientController.BucketActionResponse response = controller.createBucket(
            new S3ClientController.BucketActionRequest("unit-test-bucket")
        );

        assertThat(response.bucket()).isEqualTo("unit-test-bucket");
        assertThat(response.success()).isTrue();
        verify(s3ClientFacade).createBucket("unit-test-bucket");
    }

    @Test
    void createBucket_BlankBucket_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> controller.createBucket(new S3ClientController.BucketActionRequest(" ")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("bucket must not be blank");
    }

    @Test
    void listBucketNames_ReturnsNamesFromFacade() {
        when(s3ClientFacade.listBucketNames()).thenReturn(List.of("a", "b"));

        S3ClientController.BucketNamesResponse response = controller.listBucketNames();

        assertThat(response.bucketNames()).containsExactly("a", "b");
        verify(s3ClientFacade).listBucketNames();
    }

    @Test
    void deleteBucket_ValidBucket_ReturnsSuccessResponse() {
        when(s3ClientFacade.deleteBucket("unit-test-bucket")).thenReturn(true);

        S3ClientController.BucketActionResponse response = controller.deleteBucket(
            new S3ClientController.BucketActionRequest("unit-test-bucket")
        );

        assertThat(response.bucket()).isEqualTo("unit-test-bucket");
        assertThat(response.success()).isTrue();
        verify(s3ClientFacade).deleteBucket("unit-test-bucket");
    }

    @Test
    void deleteObject_ValidRequest_ReturnsSuccessResponse() {
        when(s3ClientFacade.deleteObject("unit-test-bucket", "images/a.png")).thenReturn(true);

        S3ClientController.DeleteObjectActionResponse response = controller.deleteObject(
            new S3ClientController.DeleteObjectRequest("unit-test-bucket", "images/a.png")
        );

        assertThat(response.bucket()).isEqualTo("unit-test-bucket");
        assertThat(response.key()).isEqualTo("images/a.png");
        assertThat(response.success()).isTrue();
        verify(s3ClientFacade).deleteObject("unit-test-bucket", "images/a.png");
    }

    @Test
    void deleteObject_BlankKey_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> controller.deleteObject(new S3ClientController.DeleteObjectRequest("unit-test-bucket", " ")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("key must not be blank");
    }

    @Test
    void listParts_ValidRequest_ReturnsPartListResponse() {
        when(s3ClientFacade.listParts("unit-test-bucket", "large-file.zip", "upload-id-1"))
            .thenReturn(
                List.of(
                    new S3ClientFacade.MultipartPartInfo(1, "etag-1", 1024L, null),
                    new S3ClientFacade.MultipartPartInfo(2, "etag-2", 2048L, null)
                )
            );

        S3ClientController.ListPartsResponse response = controller.listParts(
            new S3ClientController.ListPartsRequest("unit-test-bucket", "large-file.zip", "upload-id-1")
        );

        assertThat(response.bucket()).isEqualTo("unit-test-bucket");
        assertThat(response.key()).isEqualTo("large-file.zip");
        assertThat(response.uploadId()).isEqualTo("upload-id-1");
        assertThat(response.parts())
            .extracting(S3ClientFacade.MultipartPartInfo::partNumber, S3ClientFacade.MultipartPartInfo::eTag)
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple(1, "etag-1"),
                org.assertj.core.groups.Tuple.tuple(2, "etag-2")
            );
        verify(s3ClientFacade).listParts("unit-test-bucket", "large-file.zip", "upload-id-1");
    }

    @Test
    void listParts_BlankUploadId_ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new S3ClientController.ListPartsRequest("unit-test-bucket", "large-file.zip", " "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("uploadId must not be blank");
    }
}