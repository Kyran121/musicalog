package com.thevirtualforge.musicalog.dto;

import com.thevirtualforge.musicalog.model.enums.AlbumType;
import com.thevirtualforge.musicalog.validation.ValidImage;
import com.thevirtualforge.musicalog.validation.ValueOfEnum;
import com.thevirtualforge.musicalog.validation.group.Create;
import com.thevirtualforge.musicalog.validation.group.Edit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class AlbumPayloadDTO {

    @NotBlank(groups = Create.class)
    private String title;

    @NotBlank(groups = Create.class)
    private String artistName;

    @NotBlank(groups = Create.class)
    @ValueOfEnum(groups = {Create.class, Edit.class}, enumClass = AlbumType.class)
    private String type;

    @NotNull(groups = Create.class)
    @DecimalMin(groups = {Create.class, Edit.class}, value = "0")
    private Integer stock;

    @NotNull(groups = Create.class)
    @ValidImage(groups = {Create.class, Edit.class}, width = 250, height = 250)
    private MultipartFile coverImage;
}
