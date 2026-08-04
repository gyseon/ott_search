package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
public class TmdbDetailResponse {

    private Long id;
    private String title;          // 영화 제목
    private String name;           // TV 제목
    private String overview;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("vote_average")
    private Double voteAverage;    // 평점

    private List<Genre> genres;    // 장르 목록
    private Credits credits;       // 출연진 및 감독 정보

    @Getter
    @NoArgsConstructor
    public static class Genre {
        private String name;
    }

    @Getter
    @NoArgsConstructor
    public static class Credits {
        private List<Cast> cast;
        private List<Crew> crew;
    }

    @Getter
    @NoArgsConstructor
    public static class Cast {
        private String name;       // 배우 이름
        private String character;  // 맡은 배역
    }

    @Getter
    @NoArgsConstructor
    public static class Crew {
        private String name;       // 스태프 이름
        private String job;        // 직책 (Directing 등)
    }
}