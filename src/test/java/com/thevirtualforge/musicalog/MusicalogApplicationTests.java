package com.thevirtualforge.musicalog;

import com.thevirtualforge.musicalog.configuration.TestMongoContainerConfiguration;
import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.model.Album;
import com.thevirtualforge.musicalog.model.enums.AlbumType;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MultiValueMap;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
@ContextConfiguration(classes = TestMongoContainerConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MusicalogApplicationTests {

    private static final String BUCKET_NAME = "image-bucket";

    static final DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:0.11.3");

    @Container
    static final LocalStackContainer localstack = new LocalStackContainer(localstackImage)
        .withServices(S3);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private WebTestClient webTestClient;

    @Qualifier("webApplicationContext")
    @Autowired
    private ResourceLoader resourceLoader;

    private S3Client s3Client;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.cloud.aws.s3.endpoint", () -> localstack.getEndpointOverride(S3).toString());
        registry.add("spring.cloud.aws.s3.region", localstack::getRegion);
    }

    @BeforeEach
    void setup() {
        s3Client = makeS3Client();
        createImageBucketIfNotExists();
        truncateImageBucket();
    }

    private void createImageBucketIfNotExists() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(BUCKET_NAME).build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
        }
    }

    private void truncateImageBucket() {
        while (true) {
            List<ObjectIdentifier> objectIdentifiers = listObjectIdentifiers();
            if (objectIdentifiers.isEmpty()) {
                break;
            }
            deleteObjects(objectIdentifiers);
        }
    }

    private List<ObjectIdentifier> listObjectIdentifiers() {
        ListObjectsV2Response listResponse = s3Client.listObjectsV2(ListObjectsV2Request.builder()
            .bucket(BUCKET_NAME)
            .build());

        return listResponse.contents().stream()
            .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
            .collect(Collectors.toList());
    }

    private void deleteObjects(List<ObjectIdentifier> objectIdentifiers) {
        DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
            .bucket(BUCKET_NAME)
            .delete(builder -> builder.objects(objectIdentifiers))
            .build();
        s3Client.deleteObjects(deleteRequest);
    }

    private static S3Client makeS3Client() {
        return S3Client
            .builder()
            .endpointOverride(localstack.getEndpoint())
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                )
            )
            .region(Region.of(localstack.getRegion()))
            .build();
    }

    @AfterEach
    void cleanup() {
        mongoTemplate.remove(new Query(), "albums");
    }

    @Test
    void givenNoAlbums_whenGetAllAlbumsEndpointCalled_thenNoAlbumsReturned() {
        webTestClient.get().uri("/api/albums")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(AlbumDTO.class)
            .hasSize(0);
    }

    @Test
    void givenAlbums_whenGetAllAlbumsEndpointCalled_thenAllAlbumsReturned() {
        Album album1 = Album.builder()
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .coverImageUrl("s3://image-store/1.jpeg")
            .build();

        Album album2 = Album.builder()
            .title("Scorpion")
            .artistName("Adonis")
            .type(AlbumType.VINYL)
            .stock(1)
            .coverImageUrl("s3://image-store/2.jpeg")
            .build();

        mongoTemplate.insertAll(List.of(album1, album2));

        webTestClient.get().uri("/api/albums")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(AlbumDTO.class)
            .hasSize(2)
            .consumeWith(result -> {
                List<AlbumDTO> albums = result.getResponseBody();

                assertThat(albums)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(List.of(
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
                            .build()));
            });
    }

    @Test
    void givenNoAlbums_whenGetAlbumsEndpointWithArtistNameFilter_thenNoAlbumsReturned() {
        webTestClient.get().uri("/api/albums?artistName=drake")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(AlbumDTO.class)
            .hasSize(0);
    }

    @Test
    void givenAlbums_whenGetAlbumsEndpointWithArtistNameFilter_thenAlbumsWithArtistNameReturned() {
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

        webTestClient.get().uri("/api/albums?artistName=drake")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(AlbumDTO.class)
            .hasSize(1)
            .consumeWith(result -> {
                List<AlbumDTO> albums = result.getResponseBody();

                assertThat(albums)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(List.of(
                        AlbumDTO.builder()
                            .title("For All The Dogs")
                            .artistName("Drake")
                            .type(AlbumType.CD.name())
                            .stock(1)
                            .coverImageUrl("s3://image-store/1.jpeg")
                            .build()));
            });
    }

    @Test
    void givenAlbums_whenGetAlbumsEndpointWithTitleFilter_thenAlbumsWithTitleReturned() {
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

        webTestClient.get().uri("/api/albums?title=scorpion")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(AlbumDTO.class)
            .hasSize(1)
            .consumeWith(result -> {
                List<AlbumDTO> albums = result.getResponseBody();

                assertThat(albums)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("id")
                    .containsExactlyInAnyOrderElementsOf(List.of(
                        AlbumDTO.builder()
                            .title("Scorpion")
                            .artistName("Adonis")
                            .type(AlbumType.VINYL.name())
                            .stock(1)
                            .coverImageUrl("s3://image-store/2.jpeg")
                            .build()));
            });
    }

    @Test
    void whenCreateAlbumEndpointCalledWithAlbumFormData_thenNewAlbumCreated() throws IOException {
        Resource coverImage = resourceLoader.getResource("classpath:cover-image-250x250.png");
        byte[] coverImageFileContents = Files.readAllBytes(coverImage.getFile().toPath());

        MultipartBodyBuilder albumPayload = new MultipartBodyBuilder();
        albumPayload.part("title", "For All The Dogs");
        albumPayload.part("artistName", "Drake");
        albumPayload.part("type", "CD");
        albumPayload.part("stock", 1);
        albumPayload.part("coverImage", coverImage).contentType(MediaType.IMAGE_PNG);
        MultiValueMap<String, HttpEntity<?>> albumPayloadParts = albumPayload.build();

        WebTestClient.ResponseSpec createRequest = webTestClient.post().uri("/api/albums")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(albumPayloadParts)
            .exchange()
            .expectStatus().isCreated();

        List<Album> albums = mongoTemplate.findAll(Album.class);
        assertThat(albums).hasSize(1);

        Album album = albums.get(0);
        assertThat(album)
            .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                .withIgnoredFields("id")
                .build())
            .isEqualTo(Album.builder()
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD)
                .stock(1)
                .coverImageUrl("s3://" + BUCKET_NAME + "/" + album.getId())
                .build());

        createRequest.expectHeader().location("/api/albums/" + album.getId());

        GetObjectRequest coverImageRequest = GetObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(album.getId())
            .build();
        ResponseInputStream<GetObjectResponse> coverImageObject = s3Client.getObject(coverImageRequest);
        assertThat(coverImageObject.readAllBytes()).isEqualTo(coverImageFileContents);
    }

    @Test
    void whenEditAlbumEndpointCalledWithPartialFormData_thenAlbumEdited() throws IOException {
        final String key = "01";

        mongoTemplate.insert(Album.builder()
            .id(key)
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .coverImageUrl("s3://" + BUCKET_NAME + "/" + key)
            .build());

        File coverImageFile = resourceLoader.getResource("classpath:cover-image-250x250.png").getFile();
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(key)
            .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(coverImageFileContent));

        Resource newCoverImage = resourceLoader.getResource("classpath:cover-image-two-250x250.jpeg");
        byte[] newCoverImageFileContents = Files.readAllBytes(newCoverImage.getFile().toPath());

        MultipartBodyBuilder albumPayload = new MultipartBodyBuilder();
        albumPayload.part("stock", 2);
        albumPayload.part("coverImage", newCoverImage).contentType(MediaType.IMAGE_JPEG);
        MultiValueMap<String, HttpEntity<?>> albumPayloadParts = albumPayload.build();

        webTestClient.patch().uri("/api/albums/01")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(albumPayloadParts)
            .exchange()
            .expectStatus().isOk();

        List<Album> albums = mongoTemplate.findAll(Album.class);
        assertThat(albums).hasSize(1);

        Album album = albums.get(0);
        assertThat(album)
            .isEqualTo(Album.builder()
                .id("01")
                .title("For All The Dogs")
                .artistName("Drake")
                .type(AlbumType.CD)
                .stock(2)
                .coverImageUrl("s3://" + BUCKET_NAME + "/" + album.getId())
                .build());

        GetObjectRequest newCoverImageRequest = GetObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(key)
            .build();
        ResponseInputStream<GetObjectResponse> newCoverImageObject = s3Client.getObject(newCoverImageRequest);
        assertThat(newCoverImageObject.readAllBytes()).isEqualTo(newCoverImageFileContents);
    }

    @Test
    void whenEditAlbumEndpointCalledWithNonExistentAlbum_thenNotFound() {
        MultipartBodyBuilder albumPayload = new MultipartBodyBuilder();
        albumPayload.part("stock", 2);
        MultiValueMap<String, HttpEntity<?>> albumPayloadParts = albumPayload.build();

        webTestClient.patch().uri("/api/albums/01")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .bodyValue(albumPayloadParts)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void whenDeleteAlbumEndpointCalled_thenAlbumDeleted() throws IOException {
        final String key = "01";

        mongoTemplate.insert(Album.builder()
            .id(key)
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .coverImageUrl("s3://" + BUCKET_NAME + "/" + key)
            .build());

        File coverImageFile = resourceLoader.getResource("classpath:cover-image-250x250.png").getFile();
        byte[] coverImageFileContent = Files.readAllBytes(coverImageFile.toPath());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(BUCKET_NAME)
            .key(key)
            .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(coverImageFileContent));

        webTestClient.delete().uri("/api/albums/01")
            .exchange()
            .expectStatus().isOk();

        assertThat(mongoTemplate.findAll(Album.class)).isEmpty();
        assertThatExceptionOfType(NoSuchKeyException.class)
            .isThrownBy(() -> s3Client.headObject(HeadObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(key)
                .build()));
    }

    @Test
    void whenGetAlbumEndpointCalled_thenAlbumIsReturned() {
        final String key = "01";

        mongoTemplate.insert(Album.builder()
            .id(key)
            .title("For All The Dogs")
            .artistName("Drake")
            .type(AlbumType.CD)
            .stock(1)
            .coverImageUrl("s3://" + BUCKET_NAME + "/" + key)
            .build());

        webTestClient.get().uri("/api/albums/01")
            .exchange()
            .expectStatus().isOk()
            .expectBody(AlbumDTO.class)
            .consumeWith(result -> {
                AlbumDTO album = result.getResponseBody();
                assertThat(album)
                    .usingRecursiveComparison(RecursiveComparisonConfiguration.builder()
                        .withIgnoredFields("id")
                        .build())
                    .isEqualTo(AlbumDTO.builder()
                        .id(key)
                        .title("For All The Dogs")
                        .artistName("Drake")
                        .type(AlbumType.CD.name())
                        .stock(1)
                        .coverImageUrl("s3://" + BUCKET_NAME + "/" + key)
                        .build());
            });
    }

    @Test
    void whenGetAlbumEndpointCalledWithNonExistentAlbum_thenNotFound() {
        webTestClient.get().uri("/api/albums/01")
            .exchange()
            .expectStatus().isNotFound();
    }
}
