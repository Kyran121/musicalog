package com.thevirtualforge.musicalog.validation.constraint;

import com.thevirtualforge.musicalog.validation.ValidImage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ValidImageValidator implements ConstraintValidator<ValidImage, MultipartFile> {

    private ValidImage annotation;

    @Override
    public void initialize(ValidImage constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        annotation = constraintAnnotation;
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) {
            return true;
        }
        if (file.isEmpty()) {
            return false;
        }
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null || !isAcceptableImageContentType(file.getContentType())
                || image.getWidth() != annotation.width() || image.getHeight() != annotation.height()) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private boolean isAcceptableImageContentType(String theImageContentType) {
        for (String imageContentType : annotation.contentTypes()) {
            if (Objects.equals(imageContentType, theImageContentType)) {
                return true;
            }
        }
        return false;
    }
}