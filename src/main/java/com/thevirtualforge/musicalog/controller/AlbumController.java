package com.thevirtualforge.musicalog.controller;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;
import com.thevirtualforge.musicalog.service.AlbumService;
import com.thevirtualforge.musicalog.validation.group.Create;
import com.thevirtualforge.musicalog.validation.group.Edit;
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

    @GetMapping(path = "/{id}")
    public ResponseEntity<AlbumDTO> getAlbum(@PathVariable String id) {
        return ResponseEntity.ok(albumService.getAlbum(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createAlbum(@Validated(Create.class) @ModelAttribute AlbumPayloadDTO payload)
        throws URISyntaxException {

        AlbumDTO createdAlbum = albumService.createAlbum(payload);

        URI createdAlbumLocation = new URI("/api/albums/" + createdAlbum.getId());
        return ResponseEntity.created(createdAlbumLocation).build();
    }

    @PatchMapping(
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        path = "/{id}")
    public ResponseEntity<?> updateAlbum(@PathVariable String id, @Validated(Edit.class) @ModelAttribute AlbumPayloadDTO payload) {
        albumService.updateAlbum(id, payload);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<?> deleteAlbum(@PathVariable String id) {
        albumService.deleteAlbum(id);
        return ResponseEntity.ok().build();
    }
}