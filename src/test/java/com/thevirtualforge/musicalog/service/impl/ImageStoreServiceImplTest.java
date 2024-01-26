package com.thevirtualforge.musicalog.service.impl;

import com.thevirtualforge.musicalog.service.ImageStoreService;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageStoreServiceImplTest {

    private static final String BUCKET_NAME = "image-bucket";
    private static final String KEY = "01.png";

    @Mock
    private S3Template s3Template;

    private ImageStoreService imageStoreService;

    @BeforeEach
    void setup() {
        imageStoreService = new ImageStoreServiceImpl(BUCKET_NAME, s3Template);
    }

    @Test
    void storeImage() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        doReturn(mock(InputStream.class)).when(file).getInputStream();

        imageStoreService.storeImage(KEY, file);
        verify(s3Template).upload(BUCKET_NAME, KEY, file.getInputStream());
    }

    @Test
    void deleteImage() {
        imageStoreService.deleteImage(KEY);
        verify(s3Template).deleteObject(BUCKET_NAME, KEY);
    }
}