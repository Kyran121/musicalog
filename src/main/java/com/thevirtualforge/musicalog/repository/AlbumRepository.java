package com.thevirtualforge.musicalog.repository;

import com.thevirtualforge.musicalog.model.Album;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends MongoRepository<Album, String>, AlbumFindByFilterRepository {
    List<Album> findAll();
}