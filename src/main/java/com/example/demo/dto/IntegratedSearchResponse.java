package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegratedSearchResponse {

    private Long id;
    private String title;
    private String overview;
    private String posterPath;
    private String releaseDate;
    private String mediaType;
    private List<TmdbProviderResponse.ProviderInfo> ottProviders; // 해당 작품의 OTT 목록 (스트리밍 기준)
}