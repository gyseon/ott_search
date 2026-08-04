package com.example.demo.repository;

import com.example.demo.domain.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    // 특정 작품이 이미 찜 되어 있는지 확인하기 위한 조회 메서드
    Optional<Watchlist> findByMediaIdAndMediaType(Long mediaId, String mediaType);

    // 이미 찜 되어 있는지 여부 체크
    boolean existsByMediaIdAndMediaType(Long mediaId, String mediaType);

    // 찜 해제(삭제)용 메서드
    void deleteByMediaIdAndMediaType(Long mediaId, String mediaType);
}