package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbSearchResponse {
    private List<MovieResult> results;

    @Getter
    @NoArgsConstructor
    public static class MovieResult {
        private Long id;
        private String title;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("release_date")
        private String releaseDate;
    }
}

