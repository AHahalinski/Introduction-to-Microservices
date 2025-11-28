package com.epam.microservices.resourceservice.service;

import com.epam.microservices.resourceservice.exception.InvalidContentTypeException;
import org.springframework.stereotype.Service;

/**
 * Service for validating HTTP Content-Type headers.
 * Ensures that uploaded resources have the correct media type.
 */
@Service
public class ContentTypeValidationService {

    private static final String AUDIO_MPEG = "audio/mpeg";

    /**
     * Validates that the Content-Type header indicates an MP3 file.
     *
     * @param contentType the Content-Type header value
     * @throws InvalidContentTypeException if Content-Type is not audio/mpeg
     */
    public void validateAudioMpegContentType(String contentType) {
        if (contentType == null || !contentType.startsWith(AUDIO_MPEG)) {
            String invalidType = contentType != null ? contentType : "unknown";
            throw new InvalidContentTypeException(
                    String.format("Invalid file format: %s. Only MP3 files are allowed", invalidType)
            );
        }
    }
}

