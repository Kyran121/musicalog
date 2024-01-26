package com.thevirtualforge.musicalog.repository;

import com.thevirtualforge.musicalog.configuration.TestMongoContainerConfiguration;
import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.model.Album;
import com.thevirtualforge.musicalog.model.enums.AlbumType;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
@ContextConfiguration(classes = TestMongoContainerConfiguration.class)
public class AlbumRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private AlbumRepository albumRepository;

    @BeforeEach
    void setup() {
        mongoTemplate.insertAll(List.of(
            Album.builder()
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD)
                .stock(1)
                .coverImageUrl("s3://image-store/1.jpeg")
                .build(),
            Album.builder()
                .title("Scorpion")
                .artistName("Adonis")
                .type(AlbumType.VINYL)
                .stock(1)
                .coverImageUrl("s3://image-store/2.jpeg")
                .build()));
    }

    @AfterEach
    void cleanup() {
        mongoTemplate.remove(new Query(), Album.class);
    }

    @Test
    void findAll() {
        assertThat(albumRepository.findAll())
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id")
                .build())
            .containsExactlyElementsOf(List.of(
                Album.builder()
                    .title("For All The Dogs")
                    .artistName("Drake")
                    .type(AlbumType.CD)
                    .stock(1)
                    .coverImageUrl("s3://image-store/1.jpeg")
                    .build(),
                Album.builder()
                    .title("Scorpion")
                    .artistName("Adonis")
                    .type(AlbumType.VINYL)
                    .stock(1)
                    .coverImageUrl("s3://image-store/2.jpeg")
                    .build()));
    }

    @Test
    void findBy_fullArtistNameMatch() {
        assertThat(albumRepository.findBy(AlbumFilterDTO.builder().artistName("drake").build()))
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id")
                .build())
            .containsExactlyElementsOf(List.of(
                Album.builder()
                    .title("For All The Dogs")
                    .artistName("Drake")
                    .type(AlbumType.CD)
                    .stock(1)
                    .coverImageUrl("s3://image-store/1.jpeg")
                    .build()));
    }

    @Test
    void findBy_partialSearchNotSupportedForArtistName() {
        assertThat(albumRepository.findBy(AlbumFilterDTO.builder().artistName("rak").build()))
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id")
                .build()).isEmpty();
    }

    @Test
    void findBy_fullTitleMatch() {
        assertThat(albumRepository.findBy(AlbumFilterDTO.builder().title("scorpion").build()))
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id")
                .build())
            .containsExactlyElementsOf(List.of(
                Album.builder()
                    .title("Scorpion")
                    .artistName("Adonis")
                    .type(AlbumType.VINYL)
                    .stock(1)
                    .coverImageUrl("s3://image-store/2.jpeg")
                    .build()));
    }

    @Test
    void findBy_partialTitleMatch() {
        assertThat(albumRepository.findBy(AlbumFilterDTO.builder().title("all the").build()))
            .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id")
                .build())
            .containsExactlyElementsOf(List.of(
                Album.builder()
                    .title("For All The Dogs")
                    .artistName("Drake")
                    .type(AlbumType.CD)
                    .stock(1)
                    .coverImageUrl("s3://image-store/1.jpeg")
                    .build()));
    }
}
