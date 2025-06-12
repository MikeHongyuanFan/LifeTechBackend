package com.finance.admin.certificate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:lifetech-certificates}")
    private String bucketName;

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    public String uploadFile(String fileName, byte[] fileContent, String contentType, Map<String, String> metadata) {
        if (!s3Enabled) {
            log.info("S3 is disabled, simulating file upload for: {}", fileName);
            return "local://" + fileName;
        }

        try {
            ensureBucketExists();
            
            String key = generateFileKey(fileName);
            
            Map<String, String> s3Metadata = new HashMap<>(metadata);
            s3Metadata.put("upload-timestamp", LocalDateTime.now().toString());
            
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .metadata(s3Metadata)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileContent));
            
            log.info("Successfully uploaded file to S3: {}", key);
            return key;
            
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", fileName, e);
            throw new RuntimeException("Failed to upload file to cloud storage", e);
        }
    }

    public byte[] downloadFile(String fileKey) {
        if (!s3Enabled) {
            log.info("S3 is disabled, simulating file download for: {}", fileKey);
            return ("Mock file content for: " + fileKey).getBytes();
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            try (InputStream inputStream = s3Client.getObject(getObjectRequest)) {
                return inputStream.readAllBytes();
            }
            
        } catch (NoSuchKeyException e) {
            log.error("File not found in S3: {}", fileKey);
            throw new RuntimeException("File not found: " + fileKey);
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", fileKey, e);
            throw new RuntimeException("Failed to download file from cloud storage", e);
        }
    }

    public void deleteFile(String fileKey) {
        if (!s3Enabled) {
            log.info("S3 is disabled, simulating file deletion for: {}", fileKey);
            return;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file from S3: {}", fileKey);
            
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", fileKey, e);
            throw new RuntimeException("Failed to delete file from cloud storage", e);
        }
    }

    public String getFileUrl(String fileKey) {
        if (!s3Enabled) {
            return "http://localhost:8080/api/certificates/download/" + fileKey;
        }

        try {
            GetUrlRequest getUrlRequest = GetUrlRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            return s3Client.utilities().getUrl(getUrlRequest).toString();
            
        } catch (Exception e) {
            log.error("Failed to generate URL for file: {}", fileKey, e);
            throw new RuntimeException("Failed to generate file URL", e);
        }
    }

    public boolean fileExists(String fileKey) {
        if (!s3Enabled) {
            return true; // Simulate file exists for testing
        }

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check file existence: {}", fileKey, e);
            return false;
        }
    }

    private void ensureBucketExists() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.headBucket(headBucketRequest);
            
        } catch (NoSuchBucketException e) {
            log.info("Bucket does not exist, creating: {}", bucketName);
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            s3Client.createBucket(createBucketRequest);
            log.info("Successfully created bucket: {}", bucketName);
        }
    }

    private String generateFileKey(String fileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("certificates/%s/%s", timestamp, fileName);
    }
} 