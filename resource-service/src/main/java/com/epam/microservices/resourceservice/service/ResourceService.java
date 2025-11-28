package com.epam.microservices.resourceservice.service;

import com.epam.microservices.resourceservice.dto.SongMetadataDto;
import com.epam.microservices.resourceservice.entity.Resource;
import com.epam.microservices.resourceservice.exception.InvalidMp3Exception;
import com.epam.microservices.resourceservice.exception.ResourceNotFoundException;
import com.epam.microservices.resourceservice.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing MP3 resources.
 * Handles upload, retrieval, and deletion of audio resources with metadata synchronization.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceService {

    private static final int MAX_CSV_LENGTH = 200;
    private static final int MIN_MP3_SIZE = 3;
    private static final byte MP3_SYNC_BYTE = (byte) 0xFF;
    private static final byte MP3_FRAME_MASK = (byte) 0xE0;
    private static final byte ID3_TAG_I = 'I';
    private static final byte ID3_TAG_D = 'D';
    private static final byte ID3_TAG_3 = '3';

    private final ResourceRepository resourceRepository;
    private final MetadataExtractorService metadataExtractorService;
    private final SongServiceClient songServiceClient;

    /**
     * Uploads a new MP3 resource, extracts metadata, and syncs with Song Service.
     *
     * @param audioData the MP3 binary data
     * @return the ID of the created resource
     * @throws InvalidMp3Exception if the audio data is not a valid MP3
     */
    @Transactional
    public Long uploadResource(byte[] audioData) {
        validateMp3(audioData);

        Resource resource = createResource(audioData);
        log.info("Resource saved with ID: {}", resource.getId());

        extractAndSaveMetadata(resource.getId(), audioData);

        return resource.getId();
    }

    /**
     * Creates and persists a resource entity.
     */
    private Resource createResource(byte[] audioData) {
        Resource resource = new Resource();
        resource.setData(audioData);
        return resourceRepository.save(resource);
    }

    /**
     * Extracts metadata and attempts to save it to Song Service.
     * Logs errors but doesn't fail the resource creation.
     */
    private void extractAndSaveMetadata(Long resourceId, byte[] audioData) {
        try {
            SongMetadataDto metadata = metadataExtractorService.extractMetadata(resourceId, audioData);
            songServiceClient.saveSongMetadata(metadata);
        } catch (Exception e) {
            log.error("Failed to extract or save metadata for resource ID: {}. " +
                    "Resource was created but metadata is missing.", resourceId, e);
        }
    }

    /**
     * Retrieves the binary MP3 data for a given resource ID.
     *
     * @param id the resource ID
     * @return the MP3 binary data
     * @throws IllegalArgumentException if the ID is invalid
     * @throws ResourceNotFoundException if the resource doesn't exist
     */
    @Transactional(readOnly = true)
    public byte[] getResource(Long id) {
        validateId(id);
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Resource with ID=%d not found", id)
                ));
        return resource.getData();
    }

    /**
     * Deletes resources by their IDs and triggers cascading deletion in Song Service.
     * Invalid IDs and non-existent resources are silently ignored.
     *
     * @param ids comma-separated list of resource IDs
     * @return list of successfully deleted resource IDs
     * @throws IllegalArgumentException if the CSV string is invalid or too long
     */
    @Transactional
    public List<Long> deleteResources(String ids) {
        validateCsvLength(ids);

        List<Long> idsToDelete = parseIds(ids);
        List<Long> deletedIds = deleteExistingResources(idsToDelete);

        if (!deletedIds.isEmpty()) {
            syncMetadataDeletion(deletedIds);
        }

        log.info("Deleted {} resources out of {} requested", deletedIds.size(), idsToDelete.size());
        return deletedIds;
    }

    /**
     * Parses comma-separated IDs, validating format.
     */
    private List<Long> parseIds(String ids) {
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(this::parseAndValidateId)
                .toList();
    }

    /**
     * Parses a string to Long and validates it's a positive integer.
     * Throws exception if format is invalid.
     */
    private Long parseAndValidateId(String idStr) {
        try {
            Long id = Long.parseLong(idStr);
            if (id <= 0) {
                throw new IllegalArgumentException(
                        String.format("Invalid ID format: '%s'. Only positive integers are allowed", idStr)
                );
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    String.format("Invalid ID format: '%s'. Only positive integers are allowed", idStr)
            );
        }
    }

    /**
     * Deletes resources that exist in the repository.
     */
    private List<Long> deleteExistingResources(List<Long> ids) {
        List<Long> deletedIds = new ArrayList<>();
        for (Long id : ids) {
            if (resourceRepository.existsById(id)) {
                resourceRepository.deleteById(id);
                deletedIds.add(id);
                log.debug("Deleted resource with ID: {}", id);
            } else {
                log.debug("Resource with ID: {} does not exist, skipping", id);
            }
        }
        return deletedIds;
    }

    /**
     * Requests metadata deletion from Song Service.
     */
    private void syncMetadataDeletion(List<Long> deletedIds) {
        String deletedIdsStr = deletedIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        songServiceClient.deleteSongMetadata(deletedIdsStr);
    }

    /**
     * Validates that the audio data is a valid MP3 file.
     * Checks for MP3 frame sync or ID3 tag presence.
     *
     * @throws InvalidMp3Exception if validation fails
     */
    private void validateMp3(byte[] audioData) {
        if (audioData == null || audioData.length < MIN_MP3_SIZE) {
            throw new InvalidMp3Exception("Invalid MP3 file: data is null or too small");
        }

        boolean hasValidMp3Sync = audioData[0] == MP3_SYNC_BYTE && 
                                  (audioData[1] & MP3_FRAME_MASK) == MP3_FRAME_MASK;
        boolean hasId3Tag = audioData[0] == ID3_TAG_I && 
                           audioData[1] == ID3_TAG_D && 
                           audioData[2] == ID3_TAG_3;

        if (!hasValidMp3Sync && !hasId3Tag) {
            throw new InvalidMp3Exception("Invalid MP3 file: missing MP3 frame sync or ID3 tag");
        }
    }

    /**
     * Validates that the resource ID is positive and not null.
     *
     * @throws IllegalArgumentException if validation fails
     */
    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid value '%s' for ID. Must be a positive integer", id)
            );
        }
    }

    /**
     * Validates the CSV string of IDs.
     *
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCsvLength(String ids) {
        if (ids == null || ids.trim().isEmpty()) {
            throw new IllegalArgumentException("ID parameter is required");
        }
        if (ids.length() >= MAX_CSV_LENGTH) {
            throw new IllegalArgumentException(
                    String.format("CSV string is too long: received %d characters, maximum allowed is %d", 
                            ids.length(), MAX_CSV_LENGTH)
            );
        }
    }
}

