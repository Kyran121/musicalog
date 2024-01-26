package com.thevirtualforge.musicalog.service.impl;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;
import com.thevirtualforge.musicalog.exception.AlbumNotFoundException;
import com.thevirtualforge.musicalog.mapper.AlbumMapper;
import com.thevirtualforge.musicalog.model.Album;
import com.thevirtualforge.musicalog.model.enums.AlbumType;
import com.thevirtualforge.musicalog.repository.AlbumRepository;
import com.thevirtualforge.musicalog.service.AlbumService;
import com.thevirtualforge.musicalog.service.ImageStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceImplTest {

    private static final String BUCKET_NAME = "image-bucket";

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private ImageStoreService imageStoreService;

    @Mock
    private AlbumMapper albumMapper;

    @Captor
    private ArgumentCaptor<Album> albumCaptor;

    private AlbumService albumService;

    @BeforeEach
    void setup() {
        albumService = new AlbumServiceImpl(albumRepository, albumMapper, imageStoreService, BUCKET_NAME);
    }

    @Test
    void getAllAlbums() {
        List<AlbumDTO> albumDTOs = List.of(
            AlbumDTO.builder()
                .id("01")
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD.name())
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build(),
            AlbumDTO.builder()
                .id("02")
                .title("Scorpion")
                .artistName("Adonis")
                .type(AlbumType.VINYL.name())
                .stock(1)
                .coverImageUrl("s3://image-store/2.jpeg")
                .build());

        List<Album> albums = List.of(
            Album.builder()
                .id("01")
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD)
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build(),
            Album.builder()
                .id("02")
                .title("Scorpion")
                .artistName("Adonis")
                .type(AlbumType.VINYL)
                .stock(1)
                .coverImageUrl("s3://image-store/2.jpeg")
                .build());

        doReturn(albums)
            .when(albumRepository).findAll();

        doReturn(albumDTOs)
            .when(albumMapper).albumsToAlbumDTOs(albums);

        assertThat(albumService.getAllAlbums())
            .containsExactlyElementsOf(albumDTOs);
    }

    @Test
    void getMatchingAlbums() {
        AlbumFilterDTO albumFilter = AlbumFilterDTO.builder()
            .artistName("drake")
            .build();

        List<AlbumDTO> albumDTOs = List.of(
            AlbumDTO.builder()
                .id("01")
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD.name())
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build());

        List<Album> albums = List.of(
            Album.builder()
                .id("01")
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD)
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build());

        doReturn(albums)
            .when(albumRepository).findBy(albumFilter);

        doReturn(albumDTOs)
            .when(albumMapper).albumsToAlbumDTOs(albums);

        assertThat(albumService.getMatchingAlbums(albumFilter))
            .containsExactlyElementsOf(albumDTOs);
    }

    @Test
    void createAlbum() throws IOException {
        final String coverImageUrl = "s3://image-bucket/01.png";

        AlbumPayloadDTO payload = AlbumPayloadDTO.builder()
            .coverImage(getMultipartCoverImage())
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(1)
            .build();

        Album albumFromPayload = Album.builder()
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .build();

        doReturn(albumFromPayload)
            .when(albumMapper).albumPayloadDTOToAlbum(payload);

        Album insertedAlbum = albumFromPayload.toBuilder().id("01").build();

        doReturn(insertedAlbum)
            .when(albumRepository).insert(albumFromPayload);

        Album savedAlbum = insertedAlbum.toBuilder()
            .coverImageUrl(coverImageUrl)
            .build();

        AlbumDTO savedAlbumDTO = AlbumDTO.builder()
            .id("01")
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(1)
            .coverImageUrl(coverImageUrl)
            .build();

        doReturn(savedAlbumDTO)
            .when(albumMapper).albumToAlbumDTO(savedAlbum);

        assertThat(albumService.createAlbum(payload))
            .isEqualTo(savedAlbumDTO);

        verify(imageStoreService, times(1))
            .storeImage(insertedAlbum.getId(), payload.getCoverImage());
    }

    @Test
    void createAlbum_deleteImageIfSaveFails() throws IOException {
        final String coverImageUrl = "s3://image-bucket/01.png";

        AlbumPayloadDTO payload = AlbumPayloadDTO.builder()
            .coverImage(getMultipartCoverImage())
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(1)
            .build();

        Album albumFromPayload = Album.builder()
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .build();

        doReturn(albumFromPayload)
            .when(albumMapper).albumPayloadDTOToAlbum(payload);

        Album insertedAlbum = albumFromPayload.toBuilder().id("01").build();

        doReturn(insertedAlbum)
            .when(albumRepository).insert(albumFromPayload);

        Album savedAlbum = insertedAlbum.toBuilder()
            .coverImageUrl(coverImageUrl)
            .build();

        doThrow(RuntimeException.class)
            .when(albumRepository).save(savedAlbum);

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> albumService.createAlbum(payload));

        verify(imageStoreService, times(1))
            .storeImage(insertedAlbum.getId(), payload.getCoverImage());

        verify(imageStoreService, times(1))
            .deleteImage(insertedAlbum.getId());
    }

    @Test
    void updateAlbum() throws IOException {
        final String key = "01";

        Album existingAlbum = Album.builder()
            .id(key)
            .coverImageUrl("s3://image-bucket/01")
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .build();

        doReturn(Optional.of(existingAlbum))
            .when(albumRepository).findById(key);

        AlbumPayloadDTO payload = AlbumPayloadDTO.builder()
            .coverImage(getMultipartCoverImageTwo())
            .title("Scorpion")
            .artistName("Adonis")
            .type(AlbumType.VINYL.name())
            .stock(2)
            .build();
        albumService.updateAlbum(key, payload);

        verify(albumRepository).save(albumCaptor.capture());

        Album savedAlbum = albumCaptor.getValue();
        assertThat(savedAlbum)
            .usingRecursiveComparison()
            .isEqualTo(existingAlbum.toBuilder()
                .id("01")
                .coverImageUrl("s3://image-bucket/01")
                .title("Scorpion")
                .artistName("Adonis")
                .type(AlbumType.VINYL)
                .stock(2)
                .build());

        verify(imageStoreService, times(1))
            .storeImage(key, payload.getCoverImage());
    }

    @Test
    void updateAlbum_albumNotFound() {
        final String key = "01";

        doReturn(Optional.empty())
            .when(albumRepository).findById(key);

        assertThatExceptionOfType(AlbumNotFoundException.class)
            .isThrownBy(() -> albumService.updateAlbum(key, AlbumPayloadDTO.builder().build()));
    }

    @Test
    void deleteAlbum() {
        final String key = "01";

        albumService.deleteAlbum(key);

        verify(albumRepository, times(1)).deleteById(key);
        verify(imageStoreService, times(1)).deleteImage(key);
    }

    @Test
    void getAlbum() {
        final String key = "01";

        Album album = Album.builder()
            .id("01")
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .coverImageUrl("s3://image-store/01.jpeg")
            .build();

        AlbumDTO albumDTO = AlbumDTO.builder()
            .id("01")
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(1)
            .coverImageUrl("s3://image-store/01.jpeg")
            .build();

        doReturn(Optional.of(album))
            .when(albumRepository).findById(key);

        doReturn(albumDTO)
            .when(albumMapper).albumToAlbumDTO(album);

        assertThat(albumService.getAlbum(key))
            .isEqualTo(albumDTO);
    }

    @Test
    void getAlbum_albumNotFound() {
        assertThatExceptionOfType(AlbumNotFoundException.class)
            .isThrownBy(() -> albumService.getAlbum("01"));
    }

    private MockMultipartFile getMultipartCoverImage() throws IOException {
        File coverImageFile = ResourceUtils.getFile("classpath:cover-image-250x250.png");
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());
        return new MockMultipartFile("coverImage", "cover-image-250x250.png", "image/png", coverImageFileContent);
    }

    private MockMultipartFile getMultipartCoverImageTwo() throws IOException {
        File coverImageFile = ResourceUtils.getFile("classpath:cover-image-two-250x250.jpeg");
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());
        return new MockMultipartFile("coverImage", "cover-image-two-250x250.png", "image/jpeg", coverImageFileContent);
    }
}
