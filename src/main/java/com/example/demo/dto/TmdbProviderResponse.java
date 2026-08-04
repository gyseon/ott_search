package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
public class TmdbProviderResponse {

    private Map<String, CountryProvider> results;

    @Getter
    @NoArgsConstructor
    public static class CountryProvider {
        private String link;
        private List<ProviderInfo> flatrate; // 스트리밍(구독) 서비스 목록
        private List<ProviderInfo> rent;     // 대여 목록
        private List<ProviderInfo> buy;      // 구매 목록
    }

    @Getter
    @NoArgsConstructor
    public static class ProviderInfo {
        @JsonProperty("provider_id")
        private Long providerId;

        @JsonProperty("provider_name")
        private String providerName;

        @JsonProperty("logo_path")
        private String logoPath;

    }
}
