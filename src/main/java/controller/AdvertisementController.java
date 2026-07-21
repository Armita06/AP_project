package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.Advertisement;
import service.AdvertisementService;
import service.FileStorageService;
import security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {

    private final AdvertisementService advertisementService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    public AdvertisementController(AdvertisementService advertisementService, FileStorageService fileStorageService, ObjectMapper objectMapper) {
        this.advertisementService = advertisementService;
        this.fileStorageService = fileStorageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<?> createAd(
            @RequestParam("data") String adData,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "مشکل در احراز هویت توکن!"));
            }

            String token = header.substring(7);
            if (!JwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "توکن منقضی یا نامعتبر است!"));
            }

            String username = JwtUtil.extractUsername(token);

            Advertisement ad = objectMapper.readValue(adData, Advertisement.class);

            if (image != null && !image.isEmpty()) {
                String imageName = fileStorageService.storeFile(image);
                ad.setImageUrl(imageName);
            }

            Advertisement newAd = advertisementService.createAdvertisement(ad, username);
            return ResponseEntity.ok(newAd);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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

    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateAd(
            @PathVariable Long id,
            @RequestParam("data") String adData,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "مشکل در احراز هویت توکن!"));
            }

            String token = header.substring(7);
            if (!JwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "توکن منقضی یا نامعتبر است!"));
            }

            String username = JwtUtil.extractUsername(token);
            Advertisement updatedAdData = objectMapper.readValue(adData, Advertisement.class);

            Advertisement updatedAd = advertisementService.updateAdvertisement(id, updatedAdData, image, username);
            return ResponseEntity.ok(updatedAd);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "مشکل در احراز هویت توکن!"));
            }

            String token = header.substring(7);
            if (!JwtUtil.isTokenValid(token)) {
                return ResponseEntity.status(401).body(Map.of("error", "توکن منقضی یا نامعتبر است!"));
            }

            String username = JwtUtil.extractUsername(token);
            advertisementService.deleteAdvertisement(id, username);

            return ResponseEntity.ok(Map.of("message", "آگهی با موفقیت حذف شد."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/my-ads")
    public org.springframework.http.ResponseEntity<?> getMyAds(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "مشکل در احراز هویت توکن!"));
            }

            String token = header.substring(7);
            if (!security.JwtUtil.isTokenValid(token)) {
                return org.springframework.http.ResponseEntity.status(401).body(java.util.Map.of("error", "توکن منقضی یا نامعتبر است!"));
            }

            String username = security.JwtUtil.extractUsername(token);
            List<Advertisement> myAds = advertisementService.getUserAdvertisements(username);

            return org.springframework.http.ResponseEntity.ok(myAds);

        } catch (Exception e) {
            return org.springframework.http.ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}