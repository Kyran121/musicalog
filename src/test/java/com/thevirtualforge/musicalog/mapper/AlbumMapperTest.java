package com.thevirtualforge.musicalog.mapper;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;
import com.thevirtualforge.musicalog.model.Album;
import com.thevirtualforge.musicalog.model.enums.AlbumType;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class AlbumMapperTest {

    private final AlbumMapper albumMapper = new AlbumMapperImpl();

    @Test
    void albumsToAlbumDTOs() {
        Album album = getAlbum();
        AlbumDTO albumDTO = getAlbumDTO();

        assertThat(albumMapper.albumsToAlbumDTOs(null)).isNull();
        assertThat(albumMapper.albumsToAlbumDTOs(Collections.singletonList(null)))
            .isEqualTo(Collections.singletonList(null));
        assertThat(albumMapper.albumsToAlbumDTOs(Collections.singletonList(album)))
            .containsExactly(albumDTO);

        Album albumWithoutType = getAlbum().toBuilder().type(null).build();
        AlbumDTO albumDTOWithoutType = getAlbumDTO().toBuilder().type(null).build();

        assertThat(albumMapper.albumsToAlbumDTOs(Collections.singletonList(albumWithoutType)))
            .containsExactly(albumDTOWithoutType);
    }

    @Test
    void albumToAlbumDTO() {
        Album album = getAlbum();
        AlbumDTO albumDTO = getAlbumDTO();

        assertThat(albumMapper.albumToAlbumDTO(null)).isNull();
        assertThat(albumMapper.albumToAlbumDTO(album)).isEqualTo(albumDTO);

        Album albumWithoutType = getAlbum().toBuilder().type(null).build();
        AlbumDTO albumDTOWithoutType = getAlbumDTO().toBuilder().type(null).build();

        assertThat(albumMapper.albumToAlbumDTO(albumWithoutType)).isEqualTo(albumDTOWithoutType);
    }

    @Test
    void albumPayloadDTOToAlbum() {
        AlbumPayloadDTO albumPayloadDTO = getAlbumPayloadDTO();
        Album album = getAlbum();

        RecursiveComparisonConfiguration cmpConfig = RecursiveComparisonConfiguration.builder()
            .withIgnoredFields("id", "coverImageUrl")
            .build();

        assertThat(albumMapper.albumPayloadDTOToAlbum(null)).isNull();
        assertThat(albumMapper.albumPayloadDTOToAlbum(albumPayloadDTO))
            .usingRecursiveComparison(cmpConfig)
            .isEqualTo(album);
        assertThat(albumMapper.albumPayloadDTOToAlbum(albumPayloadDTO.toBuilder().type("cd").build()))
            .usingRecursiveComparison(cmpConfig)
            .isEqualTo(album);

        AlbumPayloadDTO albumPayloadDTOWithoutStock = getAlbumPayloadDTO().toBuilder().stock(null).build();
        Album albumWithoutStock = getAlbum().toBuilder().stock(0).build();

        assertThat(albumMapper.albumPayloadDTOToAlbum(albumPayloadDTOWithoutStock))
            .usingRecursiveComparison(cmpConfig)
            .isEqualTo(albumWithoutStock);
    }

    private AlbumPayloadDTO getAlbumPayloadDTO() {
        return AlbumPayloadDTO.builder()
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(2)
            .build();
    }

    private Album getAlbum() {
        return Album.builder()
            .id("01")
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(2)
            .coverImageUrl("s3://image-bucket/01")
            .build();
    }

    private AlbumDTO getAlbumDTO() {
        return AlbumDTO.builder()
            .id("01")
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD.name())
            .stock(2)
            .coverImageUrl("s3://image-bucket/01")
            .build();
    }
}
