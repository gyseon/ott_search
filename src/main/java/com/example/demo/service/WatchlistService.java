package com.example.demo.service;

import com.example.demo.domain.Watchlist;
import com.example.demo.dto.IntegratedSearchResponse;
import com.example.demo.dto.TmdbProviderResponse;
import com.example.demo.dto.WatchlistRequest;
import com.example.demo.dto.WatchlistStatsResponse;
import com.example.demo.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final TmdbService tmdbService; // OTT 정보 조립을 위해 주입

    // ✨ 찜 추가 / 토글 (이미 있으면 삭제, 없으면 추가)
    @Transactional
    public boolean toggleWatchlist(WatchlistRequest request) {
        boolean exists = watchlistRepository.existsByMediaIdAndMediaType(request.getMediaId(), request.getMediaType());

        if (exists) {
            watchlistRepository.deleteByMediaIdAndMediaType(request.getMediaId(), request.getMediaType());
            return false; // 찜 취소됨
        } else {
            Watchlist watchlist = Watchlist.builder()
                    .mediaId(request.getMediaId())
                    .mediaType(request.getMediaType())
                    .title(request.getTitle())
                    .posterPath(request.getPosterPath())
                    .releaseDate(request.getReleaseDate())
                    .build();
            watchlistRepository.save(watchlist);
            return true; // 찜 추가됨
        }
    }

    // ✨ 내 찜 목록 전체 조회 (OTT 정보까지 조립)
    @Transactional(readOnly = true)
    public List<IntegratedSearchResponse> getMyWatchlist() {
        List<Watchlist> watchlists = watchlistRepository.findAll();
        List<IntegratedSearchResponse> resultList = new ArrayList<>();

        for (Watchlist item : watchlists) {
            // 해당 ID의 OTT 제공자 정보 실시간 불러오기
            TmdbProviderResponse.CountryProvider provider = "movie".equals(item.getMediaType())
                    ? tmdbService.getMovieProviders(item.getMediaId())
                    : tmdbService.getTvProviders(item.getMediaId());

            List<TmdbProviderResponse.ProviderInfo> ottList = (provider != null) ? provider.getFlatrate() : null;

            resultList.add(IntegratedSearchResponse.builder()
                    .id(item.getMediaId())
                    .title(item.getTitle())
                    .posterPath(item.getPosterPath())
                    .releaseDate(item.getReleaseDate())
                    .ottProviders(ottList)
                    .build());
        }
        return resultList;
    }

    // ✨ 특정 작품 찜 여부 확인 API
    @Transactional(readOnly = true)
    public boolean isSaved(Long mediaId, String mediaType) {
        return watchlistRepository.existsByMediaIdAndMediaType(mediaId, mediaType);
    }

    // ✨ [신규] 내 찜 목록 기반 통계 분석 및 스마트 추천 가이드 생성
    @Transactional(readOnly = true)
    public WatchlistStatsResponse getWatchlistStats() {
        List<IntegratedSearchResponse> watchlist = getMyWatchlist();
        int totalCount = watchlist.size();

        if (totalCount == 0) {
            return WatchlistStatsResponse.builder()
                    .totalCount(0)
                    .ottCounts(Map.of())
                    .genreCounts(Map.of())
                    .topOtt("없음")
                    .topOttCount(0)
                    .recommendationGuide("아직 찜한 작품이 없습니다. 마음에 드는 작품을 ★ 찜해 보세요!")
                    .build();
        }

        // 1. OTT별 작품 수 집계
        Map<String, Long> ottCounts = watchlist.stream()
                .filter(item -> item.getOttProviders() != null)
                .flatMap(item -> item.getOttProviders().stream())
                .collect(Collectors.groupingBy(
                        TmdbProviderResponse.ProviderInfo::getProviderName,
                        Collectors.counting()
                ));

        // 가장 작품 수가 많은 1위 OTT 도출
        Map.Entry<String, Long> topOttEntry = ottCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(Map.entry("정보 없음", 0L));

        String topOtt = topOttEntry.getKey();
        long topOttCount = topOttEntry.getValue();

        // 2. 가성비 구독 추천 스마트 가이드 문구 생성
        String guideMessage;
        if (topOttCount > 0) {
            double percentage = Math.round(((double) topOttCount / totalCount) * 100);
            guideMessage = String.format(
                    "💡 찜한 작품의 %.0f%%(%d개)를 **%s**에서 감상할 수 있어요! 이번 달은 %s(만) 구독하는 것을 강력 추천합니다! 🔥",
                    percentage, topOttCount, topOtt, topOtt
            );
        } else {
            guideMessage = "현재 찜한 작품들 중 정액제로 제공되는 OTT 정보를 찾을 수 없습니다.";
        }

        return WatchlistStatsResponse.builder()
                .totalCount(totalCount)
                .ottCounts(ottCounts)
                .topOtt(topOtt)
                .topOttCount(topOttCount)
                .recommendationGuide(guideMessage)
                .build();
    }
}