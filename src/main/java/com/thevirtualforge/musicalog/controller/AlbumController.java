package com.thevirtualforge.musicalog.controller;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;
import com.thevirtualforge.musicalog.dto.ValidationErrorsDTO;
import com.thevirtualforge.musicalog.service.AlbumService;
import com.thevirtualforge.musicalog.validation.group.Create;
import com.thevirtualforge.musicalog.validation.group.Edit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @Operation(summary = "Gets albums")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = AlbumDTO.class)))
        )
    })
    @GetMapping
    public ResponseEntity<List<AlbumDTO>> getAlbums(@ModelAttribute AlbumFilterDTO filter) {
        List<AlbumDTO> albums;
        if (filter.getArtistName() != null || filter.getTitle() != null) {
            albums = albumService.getMatchingAlbums(filter);
        } else {
            albums = albumService.getAllAlbums();
        }
        return ResponseEntity.ok(albums);
    }

    @Operation(summary = "Gets album with id")
    @ApiResponses(value = {
        @ApiResponse(
            description = "Found the album",
            responseCode = "200",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AlbumDTO.class)
            )
        ),
        @ApiResponse(
            description = "Album not found",
            responseCode = "404"
        )
    })
    @GetMapping(path = "/{id}")
    public ResponseEntity<AlbumDTO> getAlbum(@PathVariable String id) {
        return ResponseEntity.ok(albumService.getAlbum(id));
    }

    @Operation(summary = "Creates album")
    @ApiResponses(value = {
        @ApiResponse(
            description = "Created album",
            responseCode = "202"
        ),
        @ApiResponse(
            description = "Validation error",
            responseCode = "400",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidationErrorsDTO.class)
            )
        )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAlbum(@Validated(Create.class) @ModelAttribute AlbumPayloadDTO payload)
        throws URISyntaxException {

        AlbumDTO createdAlbum = albumService.createAlbum(payload);

        URI createdAlbumLocation = new URI("/api/albums/" + createdAlbum.getId());
        return ResponseEntity.created(createdAlbumLocation).build();
    }

    @Operation(summary = "Updates album with id")
    @ApiResponses(value = {
        @ApiResponse(
            description = "Edited album",
            responseCode = "200"
        ),
        @ApiResponse(
            description = "Album not found",
            responseCode = "404"
        ),
        @ApiResponse(
            description = "Validation error",
            responseCode = "400",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ValidationErrorsDTO.class)
            )
        )
    })
    @PatchMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        path = "/{id}")
    public ResponseEntity<?> updateAlbum(@PathVariable String id, @Validated(Edit.class) @ModelAttribute AlbumPayloadDTO payload) {
        albumService.updateAlbum(id, payload);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deletes album with id")
    @ApiResponses(value = {
        @ApiResponse(
            description = "Deleted album",
            responseCode = "200"
        )
    })
    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable String id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.ok().build();
    }
}