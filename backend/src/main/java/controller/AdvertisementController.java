package controller;

import model.Advertisement;
import service.AdvertisementService;
import security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    private String extractUsername(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("احراز هویت نامعتبر است!");
        }
        String token = header.substring(7);
        if (token.trim().isEmpty() || !JwtUtil.isTokenValid(token)) {
            throw new SecurityException("توکن نامعتبر یا منقضی شده است!");
        }
        return JwtUtil.extractUsername(token);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(Exception e, HttpStatus status) {
        return ResponseEntity.status(status).body(Map.of(
                "message", e.getMessage() != null ? e.getMessage() : "خطای ناشناخته",
                "status", status.value()
        ));
    }

    private HttpStatus determineHttpStatus(Exception e) {
        if (e instanceof NoSuchElementException) {
            return HttpStatus.NOT_FOUND;
        } else if (e instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (e instanceof IllegalStateException) {
            return HttpStatus.FORBIDDEN;
        } else if (e instanceof SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchAds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy) {
        try {
            List<Advertisement> ads = advertisementService.searchActiveAdvertisements(keyword, category, city, minPrice, maxPrice, sortBy);
            List<Map<String, Object>> safeAds = ads.stream().map(ad -> {
                Map<String, Object> adMap = new HashMap<>();
                adMap.put("id", ad.getId());
                adMap.put("title", ad.getTitle());
                adMap.put("price", ad.getPrice());
                adMap.put("category", ad.getCategory());
                adMap.put("city", ad.getCity());
                adMap.put("imageUrl", ad.getImageUrl());
                return adMap;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(safeAds);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @GetMapping("/my-ads")
    public ResponseEntity<?> getMyAds(@RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            List<Advertisement> myAds = advertisementService.getUserAdvertisements(username);
            List<Map<String, Object>> safeAds = myAds.stream().map(ad -> {
                Map<String, Object> adMap = new HashMap<>();
                adMap.put("id", ad.getId());
                adMap.put("title", ad.getTitle());
                adMap.put("price", ad.getPrice());
                adMap.put("category", ad.getCategory());
                adMap.put("city", ad.getCity());
                adMap.put("status", ad.getStatus());
                adMap.put("imageUrl", ad.getImageUrl());
                return adMap;
            }).collect(Collectors.toList());
            return ResponseEntity.ok(safeAds);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdById(@PathVariable Long id) {
        try {
            Advertisement ad = advertisementService.getAdById(id);
            Map<String, Object> adMap = new HashMap<>();
            adMap.put("id", ad.getId());
            adMap.put("title", ad.getTitle());
            adMap.put("description", ad.getDescription());
            adMap.put("price", ad.getPrice());
            adMap.put("category", ad.getCategory());
            adMap.put("city", ad.getCity());
            adMap.put("status", ad.getStatus());
            adMap.put("seller", ad.getSeller().getUsername());
            adMap.put("imageUrl", ad.getImageUrl());
            return ResponseEntity.ok(adMap);
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAd(
            @RequestBody Advertisement adRequest,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            Advertisement newAd = advertisementService.createAdvertisement(adRequest, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "آگهی با موفقیت ثبت شد.",
                    "advertisementId", newAd.getId()
            ));
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAd(
            @PathVariable Long id,
            @RequestBody Advertisement adRequest,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            Advertisement updatedAd = advertisementService.updateAdvertisement(id, adRequest, username);
            return ResponseEntity.ok(Map.of(
                    "message", "آگهی با موفقیت به روزرسانی شد.",
                    "advertisementId", updatedAd.getId()
            ));
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @PutMapping("/sold/{id}")
    public ResponseEntity<?> markAdAsSold(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            advertisementService.markAsSold(id, username);
            return ResponseEntity.ok(Map.of(
                    "message", "وضعیت آگهی به فروخته شده تغییر یافت."
            ));
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAd(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            String username = extractUsername(header);
            advertisementService.deleteAdvertisement(id, username);
            return ResponseEntity.ok(Map.of(
                    "message", "آگهی با موفقیت حذف شد."
            ));
        } catch (Exception e) {
            return buildErrorResponse(e, determineHttpStatus(e));
        }
    }
}