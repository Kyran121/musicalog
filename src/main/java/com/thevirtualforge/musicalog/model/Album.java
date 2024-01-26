package com.thevirtualforge.musicalog.model;

import com.thevirtualforge.musicalog.model.enums.AlbumType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "albums")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Album {

    public static final String TITLE = "title";
    public static final String ARTIST_NAME = "artistName";

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @Indexed
    private String title;

    @Indexed
    private String artistName;

    private AlbumType type;
    private int stock;
    private String coverImageUrl;
}
