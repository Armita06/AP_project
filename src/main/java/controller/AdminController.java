package controller;

import model.Advertisement;
import model.User;
import service.AdminService;
import security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
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

    @GetMapping("/ads/pending")
    public ResponseEntity<?> getPendingAds(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            List<Advertisement> pendingAds = adminService.getPendingAdvertisements(username);
            return ResponseEntity.ok(pendingAds);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/ads/approve/{id}")
    public ResponseEntity<?> approveAd(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            adminService.approveAdvertisement(id, username);
            return ResponseEntity.ok(Map.of("message", "آگهی با موفقیت تایید شد و در لیست فعال قرار گرفت."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/ads/reject/{id}")
    public ResponseEntity<?> rejectAd(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            String reason = request.getOrDefault("reason", "به دلیل عدم رعایت قوانین سایت.");
            adminService.rejectAdvertisement(id, reason, username);
            return ResponseEntity.ok(Map.of("message", "آگهی رد شد."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/ads/delete/{id}")
    public ResponseEntity<?> deleteAd(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            adminService.deleteAdvertisement(id, username);
            return ResponseEntity.ok(Map.of("message", "آگهی نامناسب توسط مدیر حذف شد."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            List<User> users = adminService.getAllUsers(username);

            List<Map<String, Object>> safeUsers = users.stream().map(u -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", u.getId());
                userMap.put("username", u.getUsername());
                userMap.put("fullName", u.getFullName() != null ? u.getFullName() : "");
                userMap.put("role", u.getRole());
                userMap.put("status", u.getStatus());
                return userMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(safeUsers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/block/{id}")
    public ResponseEntity<?> blockUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            adminService.toggleUserBlockStatus(id, true, username);
            return ResponseEntity.ok(Map.of("message", "کاربر متخلف با موفقیت مسدود شد."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/unblock/{id}")
    public ResponseEntity<?> unblockUser(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            adminService.toggleUserBlockStatus(id, false, username);
            return ResponseEntity.ok(Map.of("message", "کاربر با موفقیت رفع مسدودی شد."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            Map<String, Object> stats = adminService.getSystemStats(username);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}