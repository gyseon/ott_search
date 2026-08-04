package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "watchlist")
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long mediaId;      // TMDB 작품 ID

    @Column(nullable = false)
    private String mediaType;    // "movie" 또는 "tv"

    @Column(nullable = false)
    private String title;        // 작품 제목

    private String posterPath;   // 포스터 이미지 경로

    private String releaseDate;  // 개봉일/방영일

    @Builder
    public Watchlist(Long mediaId, String mediaType, String title, String posterPath, String releaseDate) {
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
    }
}