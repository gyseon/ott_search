package com.example.demo.service;

import com.example.demo.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    public TmdbProviderResponse.CountryProvider getMovieProviders(Long movieId) {
        TmdbProviderResponse response = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{movie_id}/watch/providers")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbProviderResponse.class)
                .block();

        if (response != null && response.getResults() != null) {
            // 한국(KR) 지역의 Provider 정보만 추출하여 반환
            return response.getResults().get("KR");
        }
        return null;
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
        TmdbProviderResponse response = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{tv_id}/watch/providers")
                        .build(tvId))
                .retrieve()
                .bodyToMono(TmdbProviderResponse.class)
                .block();

        if (response != null && response.getResults() != null) {
            return response.getResults().get("KR");
        }
        return null;
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
    public List<IntegratedSearchResponse> getTrendingMoviesWithProviders(int page) {
        // TMDB 주간 트렌딩 영화 API 호출
        TmdbSearchResponse trendingResponse = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/trending/movie/week")
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page) // ✨ 페이지 번호 전달
                        .build())
                .retrieve()
                .bodyToMono(TmdbSearchResponse.class)
                .block();

        List<IntegratedSearchResponse> integratedList = new ArrayList<>();

        if (trendingResponse != null && trendingResponse.getResults() != null) {
            // 상위 6개 인기 영화만 추출
            List<TmdbSearchResponse.MovieResult> movieResults = trendingResponse.getResults();
            int limit = Math.min(movieResults.size(), 6);

            for (int i = 0; i < limit; i++) {
                TmdbSearchResponse.MovieResult movie = movieResults.get(i);
                TmdbProviderResponse.CountryProvider countryProvider = getMovieProviders(movie.getId());

                List<TmdbProviderResponse.ProviderInfo> ottList = null;
                if (countryProvider != null && countryProvider.getFlatrate() != null) {
                    ottList = countryProvider.getFlatrate();
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

    // ✨ [신규] 영화 + TV 통합 검색 및 OTT 정보 조립
    public List<IntegratedSearchResponse> searchMultiWithProviders(String query, int page) {
        TmdbMultiSearchResponse multiResponse = tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/multi")
                        .queryParam("query", query)
                        .queryParam("language", "ko-KR")
                        .queryParam("page", page) // ✨ 페이지 번호 전달
                        .build())
                .retrieve()
                .bodyToMono(TmdbMultiSearchResponse.class)
                .block();

        List<IntegratedSearchResponse> integratedList = new ArrayList<>();

        if (multiResponse != null && multiResponse.getResults() != null) {
            List<TmdbMultiSearchResponse.MultiResult> results = multiResponse.getResults();

            // 상위 6개 검색 결과 중 인물(person) 데이터를 제외하고 영화/TV만 필터링하여 처리
            int count = 0;
            for (TmdbMultiSearchResponse.MultiResult item : results) {
                if (count >= 6) break;

                String mediaType = item.getMediaType();
                if ("movie".equals(mediaType)) {
                    TmdbProviderResponse.CountryProvider provider = getMovieProviders(item.getId());
                    List<TmdbProviderResponse.ProviderInfo> ottList = (provider != null) ? provider.getFlatrate() : null;

                    integratedList.add(IntegratedSearchResponse.builder()
                            .id(item.getId())
                            .title("[영화] " + item.getTitle()) // 구분하기 쉽게 태그 추가
                            .overview(item.getOverview())
                            .posterPath(item.getPosterPath())
                            .releaseDate(item.getReleaseDate())
                            .ottProviders(ottList)
                            .build());
                    count++;

                } else if ("tv".equals(mediaType)) {
                    TmdbProviderResponse.CountryProvider provider = getTvProviders(item.getId());
                    List<TmdbProviderResponse.ProviderInfo> ottList = (provider != null) ? provider.getFlatrate() : null;

                    integratedList.add(IntegratedSearchResponse.builder()
                            .id(item.getId())
                            .title("[TV] " + item.getName()) // 구분하기 쉽게 태그 추가
                            .overview(item.getOverview())
                            .posterPath(item.getPosterPath())
                            .releaseDate(item.getFirstAirDate())
                            .ottProviders(ottList)
                            .build());
                    count++;
                }
            }
        }
        return integratedList;
    }

    // ✨ [신규] 영화/TV 상세 정보 (출연진, 감독, 장르, 평점) 조회
    public TmdbDetailResponse getDetail(String type, Long id) {
        String path = "movie".equals(type) ? "/movie/{id}" : "/tv/{id}";

        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(path)
                        .queryParam("language", "ko-KR")
                        .queryParam("append_to_response", "credits") // 출연진/감독 정보 함께 요청
                        .build(id))
                .retrieve()
                .bodyToMono(TmdbDetailResponse.class)
                .block();
    }
}
