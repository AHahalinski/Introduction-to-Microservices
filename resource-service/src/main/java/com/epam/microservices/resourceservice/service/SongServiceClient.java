package com.epam.microservices.resourceservice.service;

import com.epam.microservices.resourceservice.dto.SongIdResponse;
import com.epam.microservices.resourceservice.dto.SongMetadataDto;
import com.epam.microservices.resourceservice.exception.SongServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Client service for communicating with the Song Service.
 * Handles metadata creation and deletion operations.
 * Uses Eureka service discovery and client-side load balancing.
 */
@Service
@Slf4j
public class SongServiceClient {

    private static final String SONGS_ENDPOINT = "/songs";
    private static final String ID_PARAM = "id";

    private final RestTemplate restTemplate;
    private final String songServiceName;

    public SongServiceClient(RestTemplate restTemplate,
                            @Value("${song-service.name}") String songServiceName) {
        this.restTemplate = restTemplate;
        this.songServiceName = songServiceName;
    }

    /**
     * Sends song metadata to Song Service for creation.
     *
     * @param metadata the song metadata to save
     * @throws SongServiceCommunicationException if communication with Song Service fails
     */
    public void saveSongMetadata(SongMetadataDto metadata) {
        URI uri = buildSongsUri();
        log.debug("Sending metadata to Song Service: {} for resource ID: {}", uri, metadata.getId());

        try {
            HttpEntity<SongMetadataDto> request = createJsonRequest(metadata);
            ResponseEntity<SongIdResponse> response = restTemplate.postForEntity(
                    uri,
                    request,
                    SongIdResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully saved metadata for resource ID: {}", metadata.getId());
            } else {
                log.warn("Unexpected response status from Song Service: {}", response.getStatusCode());
            }

        } catch (RestClientException e) {
            String errorMessage = String.format(
                    "Failed to save metadata to Song Service for resource ID: %d",
                    metadata.getId()
            );
            log.error(errorMessage, e);
            throw new SongServiceCommunicationException(errorMessage, e);
        }
    }

    /**
     * Requests Song Service to delete metadata for given IDs.
     * Logs but does not throw exceptions to prevent cascading failures.
     *
     * @param ids comma-separated list of metadata IDs to delete
     */
    public void deleteSongMetadata(String ids) {
        URI uri = buildDeleteUri(ids);
        log.debug("Requesting metadata deletion from Song Service: {} for IDs: {}", uri, ids);

        try {
            restTemplate.delete(uri);
            log.info("Successfully requested deletion of metadata for IDs: {}", ids);

        } catch (RestClientException e) {
            log.error("Failed to delete metadata from Song Service for IDs: {}. " +
                    "This may result in orphaned metadata records.", ids, e);
        }
    }

    private URI buildSongsUri() {
        // Use service name instead of hardcoded URL for load balancing
        return UriComponentsBuilder
                .fromUriString("http://" + songServiceName)
                .path(SONGS_ENDPOINT)
                .build()
                .toUri();
    }

    private URI buildDeleteUri(String ids) {
        return UriComponentsBuilder
                .fromUriString("http://" + songServiceName)
                .path(SONGS_ENDPOINT)
                .queryParam(ID_PARAM, ids)
                .build()
                .toUri();
    }

    private <T> HttpEntity<T> createJsonRequest(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}

