package com.epam.microservices.resourceservice.controller;

import com.epam.microservices.resourceservice.dto.DeleteResponse;
import com.epam.microservices.resourceservice.dto.ResourceIdResponse;
import com.epam.microservices.resourceservice.service.ContentTypeValidationService;
import com.epam.microservices.resourceservice.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final ContentTypeValidationService contentTypeValidationService;

    @PostMapping
    public ResponseEntity<ResourceIdResponse> uploadResource(
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
            @RequestBody byte[] audioData) {
        
        contentTypeValidationService.validateAudioMpegContentType(contentType);
        Long id = resourceService.uploadResource(audioData);
        return ResponseEntity.ok(new ResourceIdResponse(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getResource(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resourceService.getResource(id));
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponse> deleteResources(@RequestParam String id) {
        List<Long> deletedIds = resourceService.deleteResources(id);
        return ResponseEntity.ok(new DeleteResponse(deletedIds));
    }
}


