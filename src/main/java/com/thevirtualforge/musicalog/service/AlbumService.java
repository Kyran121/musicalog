package com.thevirtualforge.musicalog.service;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;

import java.util.List;

public interface AlbumService {
    List<AlbumDTO> getAllAlbums();
    AlbumDTO getAlbum(final String key);
    List<AlbumDTO> getMatchingAlbums(final AlbumFilterDTO filter);
    AlbumDTO createAlbum(final AlbumPayloadDTO payload);
    void updateAlbum(final String id, final AlbumPayloadDTO payload);
    void deleteAlbum(final String id);
}
