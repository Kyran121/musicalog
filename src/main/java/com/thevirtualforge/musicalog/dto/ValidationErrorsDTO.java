package com.thevirtualforge.musicalog.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ValidationErrorsDTO {
    private final List<String> errors;
}