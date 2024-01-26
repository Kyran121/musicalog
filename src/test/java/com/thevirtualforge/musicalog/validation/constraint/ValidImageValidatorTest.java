package com.thevirtualforge.musicalog.validation.constraint;

import com.thevirtualforge.musicalog.validation.ValidImage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ValidImageValidatorTest {

    @Mock
    private ValidImage validImage;

    private ValidImageValidator validator;

    @BeforeEach
    void setup() {
        lenient().doReturn(250)
            .when(validImage).width();
        lenient().doReturn(250)
            .when(validImage).height();
        lenient().doReturn(new String [] {"image/png"})
            .when(validImage).contentTypes();

        validator = new ValidImageValidator();
        validator.initialize(validImage);
    }

    @Test
    void isValid() throws IOException {
        assertThat(validator.isValid(null, null)).isTrue();

        MockMultipartFile emptyFile = getEmptyImageFile();
        assertThat(validator.isValid(emptyFile, null)).isFalse();

        MockMultipartFile txtFile = getHelloWorldTextFile();
        assertThat(validator.isValid(txtFile, null)).isFalse();

        MockMultipartFile pngFile400x400 = getPngMultipartCoverImage400x400();
        assertThat(validator.isValid(pngFile400x400, null)).isFalse();

        MockMultipartFile pngFile250x250 = getPngMultipartCoverImage250x250();
        assertThat(validator.isValid(pngFile250x250, null)).isTrue();
    }

    private MockMultipartFile getEmptyImageFile() {
        return new MockMultipartFile("coverImage", "cover-image.png", "image/png", new byte[] {});
    }

    private MockMultipartFile getHelloWorldTextFile() throws IOException {
        File helloWorldFile = ResourceUtils.getFile("classpath:hello-world.txt");
        byte[] helloWorldFileContent = Files.readAllBytes(helloWorldFile.toPath());
        return new MockMultipartFile("coverImage", "hello-world.txt", "text/plain", helloWorldFileContent);
    }

    private MockMultipartFile getPngMultipartCoverImage400x400() throws IOException {
        File coverImageFile = ResourceUtils.getFile("classpath:cover-image-400x400.png");
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());
        return new MockMultipartFile("coverImage", "cover-image-400x400.png", "image/png", coverImageFileContent);
    }

    private MockMultipartFile getPngMultipartCoverImage250x250() throws IOException {
        File coverImageFile = ResourceUtils.getFile("classpath:cover-image-250x250.png");
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());
        return new MockMultipartFile("coverImage", "cover-image-250x250.png", "image/png", coverImageFileContent);
    }
}