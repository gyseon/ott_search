package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbMultiSearchResponse {
    private List<MultiResult> results;

    @Getter
    @NoArgsConstructor
    public static class MultiResult {
        private Long id;

        @JsonProperty("media_type")
        private String mediaType; // "movie" 또는 "tv"

        // 영화는 title, TV는 name을 사용하므로 둘 다 받아둡니다.
        private String title;
        private String name;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("poster_path")
        private String posterPath;

        @JsonProperty("release_date")
        private String releaseDate;

        @JsonProperty("first_air_date")
        private String firstAirDate;
    }
}