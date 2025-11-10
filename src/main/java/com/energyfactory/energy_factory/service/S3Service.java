package com.energyfactory.energy_factory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Profile("prod")  // 프로덕션 환경에서만 로드
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 이미지 파일 업로드
     */
    public String uploadImage(MultipartFile file, String directory) throws IOException {
        // 파일 검증
        validateFile(file);

        // 파일명 생성 (UUID + 원본 파일 확장자)
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String fileName = UUID.randomUUID().toString() + "." + extension;

        // S3 경로 생성 (예: products/uuid.jpg)
        String s3Key = directory + "/" + fileName;

        try {
            // S3에 파일 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // 업로드된 파일의 URL 반환
            String fileUrl = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s",
                    bucketName, s3Key);

            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;

        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 이미지 파일 삭제
     */
    public void deleteImage(String fileUrl) {
        try {
            // URL에서 S3 key 추출
            String s3Key = extractS3KeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted successfully: {}", fileUrl);

        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
            throw new RuntimeException("파일 삭제에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * 파일 검증
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다");
        }

        // 파일 확장자 확인
        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            throw new IllegalArgumentException("파일 확장자를 찾을 수 없습니다");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * URL에서 S3 key 추출
     */
    private String extractS3KeyFromUrl(String fileUrl) {
        // URL 형식: https://bucket-name.s3.region.amazonaws.com/directory/filename.jpg
        String s3Prefix = String.format("https://%s.s3.ap-northeast-2.amazonaws.com/", bucketName);
        if (fileUrl.startsWith(s3Prefix)) {
            return fileUrl.substring(s3Prefix.length());
        }
        throw new IllegalArgumentException("잘못된 S3 URL 형식입니다");
    }
}
