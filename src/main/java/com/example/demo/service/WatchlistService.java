package com.example.demo.service;

import com.example.demo.domain.Watchlist;
import com.example.demo.dto.IntegratedSearchResponse;
import com.example.demo.dto.TmdbProviderResponse;
import com.example.demo.dto.WatchlistRequest;
import com.example.demo.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
}