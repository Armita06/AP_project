package controller;

import model.Comment;
import service.CommentService;
import security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add/{adId}")
    public ResponseEntity<?> addComment(@PathVariable Long adId, @RequestBody Map<String, String> payload, @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("مشکل در احراز هویت توکن!");
            }

            String token = header.substring(7);
            if (!JwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body("توکن منقضی یا نامعتبر است!");
            }

            String username = JwtUtil.extractUsername(token);
            String text = payload.get("text");

            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("متن کامنت نمی‌تواند خالی باشد!");
            }

            Comment comment = commentService.addComment(adId, text, username);
            return ResponseEntity.ok(comment);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/ad/{adId}")
    public ResponseEntity<?> getComments(@PathVariable Long adId) {
        try {
            return ResponseEntity.ok(commentService.getCommentsForAd(adId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}