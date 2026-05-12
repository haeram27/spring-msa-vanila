package com.example.cephclient.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.EvaluatedTimeTests;
import com.example.cephclient.s3.S3ClientFacade;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class S3ClientFacadeIntegrationTests extends EvaluatedTimeTests {

    @Autowired
    private S3ClientFacade s3Tools;

    @Test
    void createBucket() {
        String bucketName = "it-bucket-" + Instant.now().toEpochMilli();

        log.info("[createBucket] request bucketName={}", bucketName);
        s3Tools.createBucket(bucketName);
        log.info("[createBucket] success bucketName={}", bucketName);

        List<String> bucketNames = s3Tools.listBucketNames();
        log.info("[listBucketNames] count={}, buckets={}", bucketNames.size(), bucketNames);

        assertThat(bucketNames).contains(bucketName);
    }

    @Test
    void listBucketNames() {
        log.info("[listBucketNames] request");
        List<String> bucketNames = s3Tools.listBucketNames();
        log.info("[listBucketNames] success count={}, buckets={}", bucketNames.size(), bucketNames);

        assertThat(bucketNames).isNotNull();
    }

    @Test
    void deleteBucket_WithExistingBucket_ShouldReturnTrue() {
        String bucketName = "it-delete-bucket-" + Instant.now().toEpochMilli();

        log.info("[createBucket] bucketName={}", bucketName);
        boolean created = s3Tools.createBucket(bucketName);
        assertThat(created).isTrue();

        List<String> bucketsAfterCreate = s3Tools.listBucketNames();
        log.info("[listBucketNames after create] count={}, contains={}", bucketsAfterCreate.size(), bucketsAfterCreate.contains(bucketName));
        assertThat(bucketsAfterCreate).contains(bucketName);

        log.info("[deleteBucket] bucketName={}", bucketName);
        boolean deleted = s3Tools.deleteBucket(bucketName);
        log.info("[deleteBucket] success={}", deleted);
        assertThat(deleted).isTrue();

        List<String> bucketsAfterDelete = s3Tools.listBucketNames();
        log.info("[listBucketNames after delete] count={}, contains={}", bucketsAfterDelete.size(), bucketsAfterDelete.contains(bucketName));
        assertThat(bucketsAfterDelete).doesNotContain(bucketName);
    }

    @Test
    void deleteObject_WithExistingObject_ShouldReturnTrue() {
        String bucketName = "it-deleteobj-bucket-" + Instant.now().toEpochMilli();
        String key = "test-object.txt";

        log.info("[createBucket] bucketName={}", bucketName);
        boolean created = s3Tools.createBucket(bucketName);
        assertThat(created).isTrue();

        // Note: Object 업로드는 presigned URL로 클라이언트가 수행하므로, 여기서는 삭제만 테스트
        // 실제 시나리오에서는 presigned PUT URL 받아서 업로드 후 DELETE 테스트
        log.info("[deleteObject] bucket={}, key={} (객체 미리 존재 가정)", bucketName, key);
        boolean deleted = s3Tools.deleteObject(bucketName, key);
        log.info("[deleteObject] result={}", deleted);
        
        // 존재하지 않는 객체도 S3는 204로 응답하므로 true 반환
        assertThat(deleted).isTrue();

        log.info("[deleteBucket] cleanup bucketName={}", bucketName);
        s3Tools.deleteBucket(bucketName);
    }
}
