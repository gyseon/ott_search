package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TmdbService {
    private final WebClient tmdbWebClient;

    // 1. 영화 검색
    public TmdbSearchResponse searchMovie(String query) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .block();
    }

    // 2. 특정 영화의 OTT 제공자 조회 (추가된 메서드)
    // 기존 동기 메서드(WatchlistService 등에서 사용하는 용도) 유지
    public TmdbProviderResponse.CountryProvider getMovieProviders(Long movieId) {
        return getMovieProvidersAsync(movieId).block();
    }

    // 3. ✨ [신규] 검색 결과 + OTT 정보 통합 반환 메서드
    public List<IntegratedSearchResponse> searchMovieWithProviders(String query) {
        TmdbSearchResponse searchResponse = searchMovie(query);
        List<IntegratedSearchResponse> integratedList = new ArrayList<>();

        if (searchResponse != null && searchResponse.getResults() != null) {
            // 상위 검색 결과 5개에 대해서만 OTT 정보를 조립 (호출 제한 및 속도 고려)
            List<TmdbSearchResponse.MovieResult> movieResults = searchResponse.getResults();
            int limit = Math.min(movieResults.size(), 5);

            for (int i = 0; i < limit; i++) {
                TmdbSearchResponse.MovieResult movie = movieResults.get(i);
                TmdbProviderResponse.CountryProvider countryProvider = getMovieProviders(movie.getId());

                List<TmdbProviderResponse.ProviderInfo> ottList = null;
                if (countryProvider != null && countryProvider.getFlatrate() != null) {
                    ottList = countryProvider.getFlatrate(); // 구독형 OTT 목록 (Netflix, Wavve 등)
                }

                IntegratedSearchResponse integratedItem = IntegratedSearchResponse.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .overview(movie.getOverview())
                        .posterPath(movie.getPosterPath())
                        .releaseDate(movie.getReleaseDate())
                        .ottProviders(ottList)
                        .build();

                integratedList.add(integratedItem);
            }
        }
        return integratedList;
    }

    // 4. TV 프로그램 검색
    public TmdbTvSearchResponse searchTv(String query) {
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/tv")
                        .queryParam("query", query)
                        .queryParam("language", "ko-KR")
                        .build())
                .retrieve()
                .bodyToMono(TmdbTvSearchResponse.class)
                .block();
    }

    // 5. TV 프로그램 OTT 제공자 조회
    public TmdbProviderResponse.CountryProvider getTvProviders(Long tvId) {
        return getTvProvidersAsync(tvId).block();
    }

    // 6. ✨ TV 프로그램 검색 + OTT 정보 통합 반환
    public List<IntegratedSearchResponse> searchTvWithProviders(String query) {
        TmdbTvSearchResponse tvResponse = searchTv(query);
        List<IntegratedSearchResponse> integratedList = new ArrayList<>();

        if (tvResponse != null && tvResponse.getResults() != null) {
            List<TmdbTvSearchResponse.TvResult> tvResults = tvResponse.getResults();
            int limit = Math.min(tvResults.size(), 5);

            for (int i = 0; i < limit; i++) {
                TmdbTvSearchResponse.TvResult tv = tvResults.get(i);
                TmdbProviderResponse.CountryProvider countryProvider = getTvProviders(tv.getId());

                List<TmdbProviderResponse.ProviderInfo> ottList = null;
                if (countryProvider != null && countryProvider.getFlatrate() != null) {
                    ottList = countryProvider.getFlatrate();
                }

                // 기존 통합 DTO(IntegratedSearchResponse) 구조재활용
                IntegratedSearchResponse integratedItem = IntegratedSearchResponse.builder()
                        .id(tv.getId())
                        .title(tv.getName()) // TV 이름 적용
                        .overview(tv.getOverview())
                        .posterPath(tv.getPosterPath())
                        .releaseDate(tv.getFirstAirDate()) // 방영일 적용
                        .ottProviders(ottList)
                        .build();

                integratedList.add(integratedItem);
            }
        }
        return integratedList;
    }

    // ✨ [신규] 인기 영화 목록 + OTT 정보 반환 (주간 트렌딩 기준)
    @Cacheable(value = "trendingMovies", key = "#page")
    public List<IntegratedSearchResponse> getTrendingMoviesWithProviders(int page) {
        long startTime = System.currentTimeMillis();

        TmdbSearchResponse trendingResponse = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/trending/movie/week")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .block();

        if (trendingResponse == null || trendingResponse.getResults() == null) {
            return new ArrayList<>();
        }

        List<IntegratedSearchResponse> results = Flux.fromIterable(trendingResponse.getResults())
                .flatMap(movie -> getMovieProvidersAsync(movie.getId())
                        .map(provider -> IntegratedSearchResponse.builder()
                                .id(movie.getId())
                                .title(movie.getTitle())
                                .overview(movie.getOverview())
                                .posterPath(movie.getPosterPath())
                                .releaseDate(movie.getReleaseDate())
                                .ottProviders(provider != null ? provider.getFlatrate() : null)
                                .build()))
                .collectList()
                .block();

        long endTime = System.currentTimeMillis();
        System.out.printf("⚡ [트렌딩 Executed] 소요시간: %d ms%n", (endTime - startTime));

        return results;
    }

    // ✨ [신규] 영화 + TV 통합 검색 및 OTT 정보 조립 (비동기 병렬 처리 + 10분 메모리 캐싱)
    @Cacheable(value = "searchResults", key = "#query + '_' + #page")
    public List<IntegratedSearchResponse> searchMultiWithProviders(String query, int page) {
        long startTime = System.currentTimeMillis();

        TmdbMultiSearchResponse multiResponse = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/multi")
                        .queryParam("query", query)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(TmdbMultiSearchResponse.class)
                .block();

        if (multiResponse == null || multiResponse.getResults() == null || multiResponse.getResults().isEmpty()) {
            return new ArrayList<>();
        }

        // ✨ [비동기 병렬 처리] Flux + flatMap을 이용해 N개의 OTT API를 동시(Parallel)에 호출
        List<IntegratedSearchResponse> results = Flux.fromIterable(multiResponse.getResults())
                .flatMap(item -> {
                    String mediaType = item.getMediaType();

                    if ("movie".equals(mediaType)) {
                        return getMovieProvidersAsync(item.getId())
                                .map(provider -> IntegratedSearchResponse.builder()
                                        .id(item.getId())
                                        .title("[영화] " + item.getTitle())
                                        .overview(item.getOverview())
                                        .posterPath(item.getPosterPath())
                                        .releaseDate(item.getReleaseDate())
                                        .ottProviders(provider != null ? provider.getFlatrate() : null)
                                        .build());
                    } else if ("tv".equals(mediaType)) {
                        return getTvProvidersAsync(item.getId())
                                .map(provider -> IntegratedSearchResponse.builder()
                                        .id(item.getId())
                                        .title("[TV] " + item.getName())
                                        .overview(item.getOverview())
                                        .posterPath(item.getPosterPath())
                                        .releaseDate(item.getFirstAirDate())
                                        .ottProviders(provider != null ? provider.getFlatrate() : null)
                                        .build());
                    }
                    return Mono.empty();
                })
                .collectList()
                .block(); // 전체 병렬 처리 완료 후 단 1번만 block

        long endTime = System.currentTimeMillis();
        System.out.printf("⚡ [통합검색 Executed] 키워드: '%s', 소요시간: %d ms%n", query, (endTime - startTime));

        return results;
    }

    // ✨ [신규] 영화/TV 상세 정보 (출연진, 감독, 장르, 평점) 조회(캐싱 적용)
    @Cacheable(value = "movieDetails", key = "#type + '_' + #id")
    public TmdbDetailResponse getDetail(String type, Long id) {
        String path = "movie".equals(type) ? "/movie/{id}" : "/tv/{id}";
        try {
            return tmdbWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("language", "ko-KR")
                            .queryParam("append_to_response", "credits") // 출연진/감독 정보 함께 요청
                            .build(id))
                    .retrieve()
                    .bodyToMono(TmdbDetailResponse.class)
                    .block();
        } catch (WebClientResponseException e) {
            // TMDB가 404 Not Found 응답을 준 경우 CustomException으로 변환
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CustomException("MEDIA_NOT_FOUND", "해당 작품의 상세 정보를 찾을 수 없습니다. (ID: " + id + ")", HttpStatus.NOT_FOUND);
            }
            throw new CustomException("TMDB_API_ERROR", "TMDB API 호출 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY);
        } catch (Exception e) {
            throw new CustomException("INTERNAL_ERROR", "상세 정보 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // --- 비동기 Mono 반환 Helper 메서드 ---

    public Mono<TmdbProviderResponse.CountryProvider> getMovieProvidersAsync(Long movieId) {
        return tmdbWebClient.get()
                .uri("/movie/{id}/watch/providers", movieId)
                .retrieve()
                .bodyToMono(TmdbProviderResponse.class)
                .timeout(java.time.Duration.ofSeconds(3))  // ⏳ 3초 이상 응답 없으면 타임아웃
                .retryWhen(reactor.util.retry.Retry.max(2) // 🔄 네트워크 지연 시 최대 2회 재시도
                        .filter(throwable -> !(throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.NotFound)))
                .map(res -> res.getResults() != null ? res.getResults().get("KR") : null)
                .onErrorReturn(new TmdbProviderResponse.CountryProvider()); // 🛡️ 장애 시 빈 객체로 Fallback (서비스 중단 방지)
    }

    public Mono<TmdbProviderResponse.CountryProvider> getTvProvidersAsync(Long tvId) {
        return tmdbWebClient.get()
                .uri("/tv/{id}/watch/providers", tvId)
                .retrieve()
                .bodyToMono(TmdbProviderResponse.class)
                .timeout(java.time.Duration.ofSeconds(3))
                .retryWhen(reactor.util.retry.Retry.max(2)
                        .filter(throwable -> !(throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException.NotFound)))
                .map(res -> res.getResults() != null ? res.getResults().get("KR") : null)
                .onErrorReturn(new TmdbProviderResponse.CountryProvider());
    }

}
