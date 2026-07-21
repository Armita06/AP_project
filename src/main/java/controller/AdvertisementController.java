package controller;

import model.Advertisement;
import service.AdvertisementService;
import security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAd(@RequestBody Advertisement ad, @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("مشکل در احراز هویت توکن!");
            }

            String token = header.substring(7);
            if (!JwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body("توکن منقضی یا نامعتبر است!");
            }

            String username = JwtUtil.extractUsername(token);
            Advertisement newAd = advertisementService.createAdvertisement(ad, username);

            return ResponseEntity.ok(newAd);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Advertisement>> getAllAds() {
        return ResponseEntity.ok(advertisementService.getAllAdvertisements());
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<Advertisement>> getAdsByCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(advertisementService.getAdsByCategory(categoryName));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Advertisement>> searchAds(@RequestParam String keyword) {
        return ResponseEntity.ok(advertisementService.searchAds(keyword));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("مشکل در احراز هویت توکن!");
            }

            String token = header.substring(7);
            if (!JwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body("توکن منقضی یا نامعتبر است!");
            }

            String username = JwtUtil.extractUsername(token);
            advertisementService.deleteAdvertisement(id, username);

            return ResponseEntity.ok("آگهی با موفقیت حذف شد.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}