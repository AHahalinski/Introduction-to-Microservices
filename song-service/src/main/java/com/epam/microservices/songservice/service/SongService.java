package com.epam.microservices.songservice.service;

import com.epam.microservices.songservice.dto.SongDto;
import com.epam.microservices.songservice.entity.Song;
import com.epam.microservices.songservice.exception.SongAlreadyExistsException;
import com.epam.microservices.songservice.exception.SongNotFoundException;
import com.epam.microservices.songservice.mapper.SongMapper;
import com.epam.microservices.songservice.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SongService {

    private final SongRepository songRepository;
    private final SongMapper songMapper;

    @Transactional
    public Long createSong(SongDto songDto) {
        if (songRepository.existsById(songDto.getId())) {
            throw new SongAlreadyExistsException(
                    String.format("Metadata for resource ID=%d already exists", songDto.getId())
            );
        }

        Song song = songMapper.toEntity(songDto);
        song = songRepository.save(song);
        log.info("Song metadata created with ID: {}", song.getId());
        return song.getId();
    }

    @Transactional(readOnly = true)
    public SongDto getSong(Long id) {
        validateId(id);
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException(
                        String.format("Song metadata for ID=%d not found", id)
                ));
        return songMapper.toDto(song);
    }

    @Transactional
    public List<Long> deleteSongs(String ids) {
        validateCsvLength(ids);
        List<Long> deletedIds = new ArrayList<>();

        String[] idArray = ids.split(",");
        for (String idStr : idArray) {
            String trimmedId = idStr.trim();
            if (trimmedId.isEmpty()) {
                continue;
            }
            
            Long id = parseAndValidateId(trimmedId);
            if (songRepository.existsById(id)) {
                songRepository.deleteById(id);
                deletedIds.add(id);
            }
        }

        return deletedIds;
    }

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

    private void validateId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    String.format("Invalid value '%s' for ID. Must be a positive integer", id)
            );
        }
    }

    private void validateCsvLength(String ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("ID parameter is required");
        }
        if (ids.length() >= 200) {
            throw new IllegalArgumentException(
                    String.format("CSV string is too long: received %d characters, maximum allowed is 200", 
                            ids.length())
            );
        }
    }
}


