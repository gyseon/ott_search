package com.example.demo.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class WatchlistStatsResponse {
    private int totalCount;                   // 총 찜한 작품 수
    private Map<String, Long> ottCounts;       // OTT별 보유 작품 수 (예: {"Netflix": 5, "Disney Plus": 2})
    private Map<String, Long> genreCounts;     // 장르별 작품 수 (예: {"액션": 4, "드라마": 3})
    private String topOtt;                     // 가장 많이 포함된 1위 OTT
    private long topOttCount;                  // 1위 OTT 보유 작품 수
    private String recommendationGuide;        // 개인화 스마트 추천 문구
}