package com.thevirtualforge.musicalog.repository.impl;

import com.thevirtualforge.musicalog.dto.AlbumFilterDTO;
import com.thevirtualforge.musicalog.model.Album;
import com.thevirtualforge.musicalog.repository.AlbumFindByFilterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class AlbumFindByFilterRepositoryImpl implements AlbumFindByFilterRepository {

    private static final String CASE_INSENSITIVE = "i";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<Album> findBy(AlbumFilterDTO filter) {
        Query query = new Query();

        if (filter.getArtistName() != null) {
            query.addCriteria(Criteria.where(Album.ARTIST_NAME)
                .regex("^" + Pattern.quote(filter.getArtistName()) + "$", CASE_INSENSITIVE));
        }

        if (filter.getTitle() != null) {
            query.addCriteria(Criteria.where(Album.TITLE)
                .regex(Pattern.quote(filter.getTitle()), CASE_INSENSITIVE));
        }

        return mongoTemplate.find(query, Album.class);
    }
}
