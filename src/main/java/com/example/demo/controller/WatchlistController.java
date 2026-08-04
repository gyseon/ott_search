package com.example.demo.controller;

import com.example.demo.dto.IntegratedSearchResponse;
import com.example.demo.dto.WatchlistRequest;
import com.example.demo.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    // 찜 토글 (추가/삭제)
    @PostMapping("/toggle")
    public boolean toggleWatchlist(@RequestBody WatchlistRequest request) {
        return watchlistService.toggleWatchlist(request);
    }

    // 내 찜 목록 전체 조회
    @GetMapping
    public List<IntegratedSearchResponse> getMyWatchlist() {
        return watchlistService.getMyWatchlist();
    }

    // 특정 작품 찜 여부 조회
    @GetMapping("/check")
    public boolean checkSaved(@RequestParam Long mediaId, @RequestParam String mediaType) {
        return watchlistService.isSaved(mediaId, mediaType);
    }
}