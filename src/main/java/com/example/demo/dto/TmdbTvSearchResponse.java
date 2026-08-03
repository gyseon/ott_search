package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbTvSearchResponse {
    private List<TvResult> results;

    @Getter
    @NoArgsConstructor
    public static class TvResult {
        private Long id;

        // TV 프로그램은 title 대신 name을 사용합니다.
        private String name;

        @JsonProperty("overview")
        private String overview;

        @JsonProperty("poster_path")
        private String posterPath;

        // TV 프로그램은 release_date 대신 first_air_date를 사용합니다.
        @JsonProperty("first_air_date")
        private String firstAirDate;
    }
}