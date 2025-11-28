package com.epam.microservices.songservice.controller;

import com.epam.microservices.songservice.dto.DeleteResponse;
import com.epam.microservices.songservice.dto.SongDto;
import com.epam.microservices.songservice.dto.SongIdResponse;
import com.epam.microservices.songservice.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongIdResponse> createSong(@Valid @RequestBody SongDto songDto) {
        Long id = songService.createSong(songDto);
        return ResponseEntity.ok(new SongIdResponse(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongDto> getSong(@PathVariable Long id) {
        SongDto songDto = songService.getSong(id);
        return ResponseEntity.ok(songDto);
    }

    @DeleteMapping
    public ResponseEntity<DeleteResponse> deleteSongs(@RequestParam String id) {
        List<Long> deletedIds = songService.deleteSongs(id);
        return ResponseEntity.ok(new DeleteResponse(deletedIds));
    }
}


