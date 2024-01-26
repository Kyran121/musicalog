package com.thevirtualforge.musicalog.repository;

import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.model.Album;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface AlbumFindByFilterRepository {
    List<Album> findBy(AlbumFilterDTO filter);
}
