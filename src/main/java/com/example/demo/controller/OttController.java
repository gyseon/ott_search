package com.example.demo.controller;

import com.example.demo.dto.IntegratedSearchResponse;
import com.example.demo.dto.TmdbDetailResponse;
import com.example.demo.dto.TmdbProviderResponse;
import com.example.demo.dto.TmdbSearchResponse;
import com.example.demo.service.TmdbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ott")
@RequiredArgsConstructor
public class OttController {
    private final TmdbService tmdbService;

    // 영화 검색 API
    @GetMapping("/search")
    public TmdbSearchResponse search(@RequestParam String query) {
        return tmdbService.searchMovie(query);
    }

    // 영화 ID로 OTT 정보 조회 API (추가된 엔드포인트)
    @GetMapping("/providers/{movieId}")
    public TmdbProviderResponse.CountryProvider getProviders(@PathVariable Long movieId) {
        return tmdbService.getMovieProviders(movieId);
    }

    // ✨ [신규] 검색어 하나로 영화 정보와 OTT 목록을 한 번에 반환하는 API
    @GetMapping("/search/integrated")
    public List<IntegratedSearchResponse> searchIntegrated(@RequestParam String query) {
        return tmdbService.searchMovieWithProviders(query);
    }

    // ✨ TV 프로그램 통합 검색 API 추가
    @GetMapping("/search/tv/integrated")
    public List<IntegratedSearchResponse> searchTvIntegrated(@RequestParam String query) {
        return tmdbService.searchTvWithProviders(query);
    }

    // ✨ [신규] 인기 영화 랭킹 API
    @GetMapping("/trending/movies")
    public List<IntegratedSearchResponse> getTrendingMovies(@RequestParam(defaultValue = "1") int page) {
        return tmdbService.getTrendingMoviesWithProviders(page);
    }

    // ✨ [신규] 영화 + TV 통합 검색 API
    @GetMapping("/search/multi/integrated")
    public List<IntegratedSearchResponse> searchMultiIntegrated(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        return tmdbService.searchMultiWithProviders(query, page);
    }

    // ✨ [신규] 상세 정보 조회 API (type: movie 또는 tv)
    @GetMapping("/detail/{type}/{id}")
    public TmdbDetailResponse getDetail(@PathVariable String type, @PathVariable Long id) {
        return tmdbService.getDetail(type, id);
    }
}
