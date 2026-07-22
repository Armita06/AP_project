package controller;

import model.Bookmark;
import service.BookmarkService;
import security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    private String extractUsername(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("توکن ارسال نشده یا نامعتبر است!");
        }
        String token = header.substring(7);
        if (!JwtUtil.isTokenValid(token)) {
            throw new SecurityException("توکن منقضی یا نامعتبر است!");
        }
        return JwtUtil.extractUsername(token);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(Exception e, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "message", e.getMessage(),
                "status", status.value()
        ));
    }

    private HttpStatus determineHttpStatus(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return HttpStatus.NOT_FOUND;
        } else if (e instanceof IllegalStateException) {
            return HttpStatus.BAD_REQUEST;
        } else if (e instanceof SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @PostMapping("/toggle/{adId}")
    public ResponseEntity<?> toggleBookmark(
            @PathVariable Long adId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            boolean isBookmarked = bookmarkService.toggleBookmark(adId, username);
            String msg = isBookmarked ? "آگهی با موفقیت نشان شد." : "نشان آگهی برداشته شد.";
            return ResponseEntity.ok(Map.of(
                    "isBookmarked", isBookmarked,
                    "message", msg
            ));
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserBookmarks(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            List<Bookmark> bookmarks = bookmarkService.getUserBookmarks(username);

            // مَپ کردنِ دقیقِ داده‌ها برای فرانت‌اِند (بدون نشت اطلاعات کاربر و با ارسال عکس آگهی)
            List<Map<String, Object>> safeBookmarks = bookmarks.stream().map(b -> Map.of(
                    "id", b.getId(),
                    "createdAt", b.getCreatedAt(),
                    "advertisement", Map.of(
                            "id", b.getAdvertisement().getId(),
                            "title", b.getAdvertisement().getTitle(),
                            "price", b.getAdvertisement().getPrice(),
                            "status", b.getAdvertisement().getStatus(),
                            "imageUrl", b.getAdvertisement().getImageUrl() != null ? b.getAdvertisement().getImageUrl() : ""
                    )
            )).collect(Collectors.toList());

            return ResponseEntity.ok(safeBookmarks);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @GetMapping("/check/{adId}")
    public ResponseEntity<?> checkBookmark(
            @PathVariable Long adId,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            boolean isBookmarked = bookmarkService.isAdBookmarked(adId, username);
            return ResponseEntity.ok(Map.of("isBookmarked", isBookmarked));
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }
}