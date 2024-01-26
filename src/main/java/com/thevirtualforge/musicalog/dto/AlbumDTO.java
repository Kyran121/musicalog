package com.thevirtualforge.musicalog.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Builder
public class AlbumDTO {
    private String id;
    private String title;
    private String artistName;
    private String type;
    private int stock;
    private String coverImageUrl;
}