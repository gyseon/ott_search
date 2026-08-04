package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WatchlistRequest {
    private Long mediaId;
    private String mediaType;
    private String title;
    private String posterPath;
    private String releaseDate;
}