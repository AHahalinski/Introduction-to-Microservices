package com.epam.microservices.resourceservice.service;

import com.epam.microservices.resourceservice.dto.SongMetadataDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Service for extracting metadata from MP3 files using Apache Tika.
 * Handles extraction of title, artist, album, duration, and year.
 */
@Service
@Slf4j
public class MetadataExtractorService {

    private static final String METADATA_KEY_TITLE = "dc:title";
    private static final String METADATA_KEY_ARTIST = "xmpDM:artist";
    private static final String METADATA_KEY_ALBUM = "xmpDM:album";
    private static final String METADATA_KEY_DURATION = "xmpDM:duration";
    private static final String METADATA_KEY_RELEASE_DATE = "xmpDM:releaseDate";
    
    private static final String DEFAULT_VALUE = "Unknown";
    private static final String DEFAULT_YEAR = "1900";
    private static final String DEFAULT_DURATION = "00:00";
    private static final int YEAR_LENGTH = 4;

    /**
     * Extracts metadata from MP3 binary data.
     *
     * @param resourceId the resource ID to associate with the metadata
     * @param audioData the MP3 binary data
     * @return extracted metadata as DTO
     * @throws RuntimeException if metadata extraction fails
     */
    public SongMetadataDto extractMetadata(Long resourceId, byte[] audioData) {
        log.debug("Starting metadata extraction for resource ID: {}", resourceId);
        
        try {
            Metadata metadata = parseAudioMetadata(audioData);
            SongMetadataDto songMetadata = buildSongMetadata(resourceId, metadata);
            
            log.debug("Successfully extracted metadata for resource ID: {} - Name: {}, Artist: {}, Album: {}, Duration: {}, Year: {}", 
                    resourceId, songMetadata.getName(), songMetadata.getArtist(), 
                    songMetadata.getAlbum(), songMetadata.getDuration(), songMetadata.getYear());
            
            return songMetadata;

        } catch (IOException | SAXException | TikaException e) {
            log.error("Failed to extract metadata from MP3 file for resource ID: {}", resourceId, e);
            throw new RuntimeException("Failed to extract metadata from MP3 file", e);
        }
    }

    /**
     * Parses audio data using Apache Tika MP3 parser.
     */
    private Metadata parseAudioMetadata(byte[] audioData) throws IOException, SAXException, TikaException {
        Parser parser = new Mp3Parser();
        Metadata metadata = new Metadata();
        ContentHandler handler = new DefaultHandler();
        ParseContext parseContext = new ParseContext();

        parser.parse(new ByteArrayInputStream(audioData), handler, metadata, parseContext);
        return metadata;
    }

    /**
     * Builds SongMetadataDto from parsed Tika metadata.
     */
    private SongMetadataDto buildSongMetadata(Long resourceId, Metadata metadata) {
        String name = getMetadataValue(metadata, METADATA_KEY_TITLE, DEFAULT_VALUE);
        String artist = getMetadataValue(metadata, METADATA_KEY_ARTIST, DEFAULT_VALUE);
        String album = getMetadataValue(metadata, METADATA_KEY_ALBUM, DEFAULT_VALUE);
        String year = extractYear(metadata);
        String duration = extractDuration(metadata);

        return new SongMetadataDto(resourceId, name, artist, album, duration, year);
    }

    /**
     * Gets metadata value with fallback to default.
     */
    private String getMetadataValue(Metadata metadata, String key, String defaultValue) {
        String value = metadata.get(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    /**
     * Extracts year from release date metadata.
     * Falls back to default year if not present or invalid.
     */
    private String extractYear(Metadata metadata) {
        String releaseDate = metadata.get(METADATA_KEY_RELEASE_DATE);
        
        if (releaseDate == null || releaseDate.isEmpty()) {
            log.debug("No release date found, using default year: {}", DEFAULT_YEAR);
            return DEFAULT_YEAR;
        }
        
        if (releaseDate.length() >= YEAR_LENGTH) {
            return releaseDate.substring(0, YEAR_LENGTH);
        }
        
        log.debug("Release date too short: {}, using default year: {}", releaseDate, DEFAULT_YEAR);
        return DEFAULT_YEAR;
    }

    /**
     * Extracts and formats duration from metadata.
     * Converts from seconds to mm:ss format with leading zeros.
     */
    private String extractDuration(Metadata metadata) {
        String durationStr = metadata.get(METADATA_KEY_DURATION);
        
        if (durationStr == null || durationStr.isEmpty()) {
            log.debug("No duration found, using default: {}", DEFAULT_DURATION);
            return DEFAULT_DURATION;
        }

        try {
            double durationSeconds = Double.parseDouble(durationStr);
            return formatDuration((int) durationSeconds);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse duration: {}, using default: {}", durationStr, DEFAULT_DURATION);
            return DEFAULT_DURATION;
        }
    }

    /**
     * Formats duration from total seconds to mm:ss format.
     */
    private String formatDuration(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

