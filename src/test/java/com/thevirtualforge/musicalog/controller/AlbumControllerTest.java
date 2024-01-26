package com.thevirtualforge.musicalog.controller;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;
import com.thevirtualforge.musicalog.model.enums.AlbumType;
import com.thevirtualforge.musicalog.service.AlbumService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlbumController.class)
@ExtendWith(SpringExtension.class)
public class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlbumService albumService;

    @Qualifier("webApplicationContext")
    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void getAlbums_returnsAllAlbumsGivenNoFilters() throws Exception {
        doReturn(List.of(
            AlbumDTO.builder()
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD.name())
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build(),
            AlbumDTO.builder()
                .title("Scorpion")
                .artistName("Adonis")
                .type(AlbumType.VINYL.name())
                .stock(1)
                .coverImageUrl("s3://image-store/2.jpeg")
                .build()))
            .when(albumService).getAllAlbums();

        mockMvc.perform(get("/api/albums")
                .contentType(MediaType.APPLICATION_JSON_VALUE))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))

            .andExpect(jsonPath("$[0].id").doesNotExist())
            .andExpect(jsonPath("$[0].title").value("For All The Dogs"))
            .andExpect(jsonPath("$[0].artistName").value("Drake"))
            .andExpect(jsonPath("$[0].type").value("CD"))
            .andExpect(jsonPath("$[0].stock").value("1"))
            .andExpect(jsonPath("$[0].coverImageUrl").value("s3://image-store/1.jpeg"))

            .andExpect(jsonPath("$[1].id").doesNotExist())
            .andExpect(jsonPath("$[1].title").value("Scorpion"))
            .andExpect(jsonPath("$[1].artistName").value("Adonis"))
            .andExpect(jsonPath("$[1].type").value("VINYL"))
            .andExpect(jsonPath("$[1].stock").value("1"))
            .andExpect(jsonPath("$[1].coverImageUrl").value("s3://image-store/2.jpeg"));
    }

    @Test
    void getAlbums_returnsAlbumsMatchingFilterGivenFilter() throws Exception {
        doReturn(List.of(
            AlbumDTO.builder()
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD.name())
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build()))
            .when(albumService).getMatchingAlbums(AlbumFilterDTO.builder()
                .artistName("drake")
                .build());

        mockMvc.perform(get("/api/albums")
                .param("artistName", "drake")
                .contentType(MediaType.APPLICATION_JSON_VALUE))

            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))

            .andExpect(jsonPath("$[0].id").doesNotExist())
            .andExpect(jsonPath("$[0].title").value("For All The Dogs"))
            .andExpect(jsonPath("$[0].artistName").value("Drake"))
            .andExpect(jsonPath("$[0].type").value("CD"))
            .andExpect(jsonPath("$[0].stock").value("1"))
            .andExpect(jsonPath("$[0].coverImageUrl").value("s3://image-store/1.jpeg"));
    }

    @Test
    void createAlbum() throws Exception {
        AlbumDTO createdAlbum = AlbumDTO.builder().id("01").build();
        MockMultipartFile multipartCoverImage = getMultipartCoverImage();

        doReturn(createdAlbum)
            .when(albumService).createAlbum(AlbumPayloadDTO.builder()
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD.name())
                .stock(1)
                .coverImage(multipartCoverImage)
                .build());

        mockMvc.perform(multipart("/api/albums")
                .file(multipartCoverImage)
                .param("title", "For All The Dogs")
                .param("artistName", "Drake")
                .param("type", "CD")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isCreated())
            .andExpect(header().string("location", "/api/albums/01"));
    }

    @Test
    void createAlbum_titleRequired() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage())
                .param("artistName", "Drake")
                .param("type", "CD")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("title must not be blank"));
    }

    @Test
    void createAlbum_artistNameRequired() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage())
                .param("title", "For All The Dogs")
                .param("type", "CD")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("artistName must not be blank"));
    }

    @Test
    void createAlbum_typeRequired() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage())
                .param("title", "For All The Dogs")
                .param("artistName", "Drake")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("type must not be blank"));
    }

    @Test
    void createAlbum_typeValueOfEnum() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage())
                .param("title", "For All The Dogs")
                .param("type", "Invalid")
                .param("artistName", "Drake")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("type must be one of: VINYL, CD"));
    }

    @Test
    void createAlbum_coverImageRequired() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .param("title", "For All The Dogs")
                .param("type", "CD")
                .param("artistName", "Drake")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("coverImage must not be null"));
    }

    @Test
    void createAlbum_coverImageMustBeAnImage() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getHelloWorldTextFile())
                .param("title", "For All The Dogs")
                .param("type", "CD")
                .param("artistName", "Drake")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("coverImage must be an image, size 250x250"));
    }

    @Test
    void createAlbum_coverImageMustBeSize250x250() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage400x400())
                .param("title", "For All The Dogs")
                .param("type", "CD")
                .param("artistName", "Drake")
                .param("stock", "1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("coverImage must be an image, size 250x250"));
    }

    @Test
    void createAlbum_stockRequired() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage())
                .param("title", "For All The Dogs")
                .param("type", "CD")
                .param("artistName", "Drake")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("stock must not be null"));
    }

    @Test
    void createAlbum_stockMustGreaterOrEqualToZero() throws Exception {
        mockMvc.perform(multipart("/api/albums")
                .file(getMultipartCoverImage())
                .param("title", "For All The Dogs")
                .param("type", "CD")
                .param("artistName", "Drake")
                .param("stock", "-1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("stock must be greater than or equal to 0"));
    }

    @Test
    void updateAlbum() throws Exception {
        AlbumDTO createdAlbum = AlbumDTO.builder().id("01").build();
        MockMultipartFile multipartCoverImage = getMultipartCoverImage();

        doReturn(createdAlbum)
            .when(albumService).createAlbum(AlbumPayloadDTO.builder()
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD.name())
                .stock(1)
                .coverImage(multipartCoverImage)
                .build());

        mockMvc.perform(multipart("/api/albums/01")
                .file(multipartCoverImage)
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
                .param("stock", "2")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isOk());

        verify(albumService, times(1)).updateAlbum("01", AlbumPayloadDTO.builder()
            .coverImage(multipartCoverImage)
            .stock(2)
            .build());
    }

    @Test
    void updateAlbum_stockMustGreaterOrEqualToZero() throws Exception {
        mockMvc.perform(patch("/api/albums/01")
                .param("stock", "-1")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("stock must be greater than or equal to 0"));
    }

    @Test
    void updateAlbum_coverImageMustBeAnImage() throws Exception {
        mockMvc.perform(multipart("/api/albums/01")
                .file(getHelloWorldTextFile())
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("coverImage must be an image, size 250x250"));
    }

    @Test
    void updateAlbum_coverImageMustBeSize250x250() throws Exception {
        mockMvc.perform(multipart("/api/albums/01")
                .file(getMultipartCoverImage400x400())
                .with(request -> {
                    request.setMethod("PATCH");
                    return request;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("coverImage must be an image, size 250x250"));
    }

    @Test
    void updateAlbum_typeValueOfEnum() throws Exception {
        mockMvc.perform(patch("/api/albums/01")
                .param("type", "Invalid")
                .contentType(MediaType.MULTIPART_FORM_DATA))

            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0]").value("type must be one of: VINYL, CD"));
    }

    @Test
    void deleteAlbum() throws Exception {
        mockMvc.perform(delete("/api/albums/01"))
            .andExpect(status().isOk());

        verify(albumService, times(1)).deleteAlbum("01");
    }

    @Test
    void getAlbum() throws Exception {
        final String key = "01";

        doReturn(AlbumDTO.builder()
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(1)
            .coverImageUrl("s3://image-store/1.jpeg")
            .build())
            .when(albumService).getAlbum(key);

        mockMvc.perform(get("/api/albums/" + key))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").doesNotExist())
            .andExpect(jsonPath("$.title").value("For All The Dogs"))
            .andExpect(jsonPath("$.artistName").value("Drake"))
            .andExpect(jsonPath("$.type").value("CD"))
            .andExpect(jsonPath("$.stock").value("1"))
            .andExpect(jsonPath("$.coverImageUrl").value("s3://image-store/1.jpeg"));
    }

    private MockMultipartFile getMultipartCoverImage() throws IOException {
        File coverImageFile = resourceLoader.getResource("classpath:cover-image-250x250.png").getFile();
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());
        return new MockMultipartFile("coverImage", "cover-image-250x250.png", "image/png", coverImageFileContent);
    }

    private MockMultipartFile getMultipartCoverImage400x400() throws IOException {
        File coverImageFile = resourceLoader.getResource("classpath:cover-image-400x400.png").getFile();
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());
        return new MockMultipartFile("coverImage", "cover-image-400x400.png", "image/png", coverImageFileContent);
    }

    private MockMultipartFile getHelloWorldTextFile() throws IOException {
        File helloWorldFile = ResourceUtils.getFile("classpath:hello-world.txt");
        byte[] helloWorldFileContent = Files.readAllBytes(helloWorldFile.toPath());
        return new MockMultipartFile("coverImage", "hello-world.txt", "text/plain", helloWorldFileContent);
    }
}
