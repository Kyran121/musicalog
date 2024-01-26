package com.thevirtualforge.musicalog.mapper;

import com.thevirtualforge.musicalog.dto.AlbumDTO;
import com.thevirtualforge.musicalog.dto.AlbumPayloadDTO;
import com.thevirtualforge.musicalog.model.Album;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlbumMapper {
    List<AlbumDTO> albumsToAlbumDTOs(List<Album> albums);

    AlbumDTO albumToAlbumDTO(Album album);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coverImageUrl", ignore = true)
    Album albumPayloadDTOToAlbum(AlbumPayloadDTO albumPayload);
}
