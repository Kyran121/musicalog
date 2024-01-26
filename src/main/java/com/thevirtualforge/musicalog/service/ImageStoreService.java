package com.thevirtualforge.musicalog.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStoreService {
    void storeImage(final String key, final MultipartFile file);
    void deleteImage(final String key);
}
