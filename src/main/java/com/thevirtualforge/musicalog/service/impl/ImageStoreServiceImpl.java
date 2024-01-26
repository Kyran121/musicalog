package com.thevirtualforge.musicalog.service.impl;

import com.thevirtualforge.musicalog.service.ImageStoreService;
import io.awspring.cloud.s3.S3Template;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStoreServiceImpl implements ImageStoreService {

    private final S3Template s3Template;
    private final String bucketName;

    public ImageStoreServiceImpl(
        @Value("${image-store.bucket-name}") String bucketName,
        S3Template s3Template) {

        this.s3Template = s3Template;
        this.bucketName = bucketName;
    }

    @Override
    @SneakyThrows
    public void storeImage(final String key, final MultipartFile file) {
        s3Template.upload(bucketName, key, file.getInputStream());
    }

    @Override
    public void deleteImage(final String key) {
        s3Template.deleteObject(bucketName, key);
    }
}
