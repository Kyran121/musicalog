package com.thevirtualforge.musicalog.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@Builder
public class AlbumFilterDTO {
    private String title;
    private String artistName;
}
