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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumMapper albumMapper;
    private final ImageStoreService imageStoreService;
    private final String bucketName;

    public AlbumServiceImpl(
        AlbumRepository albumRepository,
        AlbumMapper albumMapper,
        ImageStoreService imageStoreService,
        @Value("${image-store.bucket-name}") String bucketName) {

        this.albumRepository = albumRepository;
        this.albumMapper = albumMapper;
        this.imageStoreService = imageStoreService;
        this.bucketName = bucketName;
    }

    @Override
    public List<AlbumDTO> getAllAlbums() {
        List<Album> allAlbums = albumRepository.findAll();
        return albumMapper.albumsToAlbumDTOs(allAlbums);
    }

    @Override
    public AlbumDTO getAlbum(String key) {
        Optional<Album> album = albumRepository.findById(key);
        return album.map(albumMapper::albumToAlbumDTO)
            .orElseThrow(() -> new AlbumNotFoundException("album " + key + " not found"));
    }

    @Override
    public List<AlbumDTO> getMatchingAlbums(final AlbumFilterDTO filter) {
        List<Album> matchingAlbums = albumRepository.findBy(filter);
        return albumMapper.albumsToAlbumDTOs(matchingAlbums);
    }

    @Override
    @Transactional
    public AlbumDTO createAlbum(final AlbumPayloadDTO payload) {
        Album insertedAlbum = insertAlbumFromPayload(payload);
        return albumMapper.albumToAlbumDTO(insertedAlbum);
    }

    private Album insertAlbumFromPayload(AlbumPayloadDTO payload) {
        Album album = albumMapper.albumPayloadDTOToAlbum(payload);
        Album insertedAlbum = albumRepository.insert(album);

        imageStoreService.storeImage(insertedAlbum.getId(), payload.getCoverImage());
        insertedAlbum.setCoverImageUrl("s3://" + bucketName + "/" + insertedAlbum.getId());

        try {
            albumRepository.save(insertedAlbum);
        } catch (Exception e) {
            imageStoreService.deleteImage(insertedAlbum.getId());
            throw e;
        }

        return insertedAlbum;
    }

    @Override
    public void updateAlbum(final String id, final AlbumPayloadDTO payload) {
        Optional<Album> optionalAlbum = albumRepository.findById(id);
        if (optionalAlbum.isEmpty()) {
            throw new AlbumNotFoundException("album " + id + " not found");
        }

        Album album = optionalAlbum.get();
        if (payload.getTitle() != null && !payload.getTitle().isBlank()) {
            album.setTitle(payload.getTitle());
        }
        if (payload.getArtistName() != null && !payload.getArtistName().isBlank()) {
            album.setArtistName(payload.getArtistName());
        }
        if (payload.getType() != null && !payload.getType().isBlank()) {
            album.setType(AlbumType.valueOf(payload.getType()));
        }
        if (payload.getStock() != null) {
            album.setStock(payload.getStock());
        }
        if (payload.getCoverImage() != null) {
            imageStoreService.storeImage(id, payload.getCoverImage());
        }
        albumRepository.save(album);
    }

    @Override
    public void deleteAlbum(String id) {
        albumRepository.deleteById(id);
        imageStoreService.deleteImage(id);
    }
}
