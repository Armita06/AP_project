package controller;

import model.Rating;
import service.RatingService;
import security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
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

    @PostMapping("/add/{adId}")
    public ResponseEntity<?> addRating(
            @PathVariable Long adId,
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);

            Integer score = null;
            if (request.containsKey("score") && request.get("score") != null) {
                score = Integer.parseInt(request.get("score").toString());
            }

            String comment = request.containsKey("comment") ? request.get("comment").toString() : null;

            Rating savedRating = ratingService.addRating(adId, score, comment, username);

            return ResponseEntity.ok(Map.of(
                    "message", "امتیاز شما با موفقیت ثبت شد.",
                    "ratingId", savedRating.getId()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/seller/{sellerUsername}")
    public ResponseEntity<?> getSellerStats(@PathVariable String sellerUsername) {
        try {
            Map<String, Object> stats = ratingService.getSellerRatingStats(sellerUsername);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}
