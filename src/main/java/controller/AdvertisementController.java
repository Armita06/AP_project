package controller;

import model.Advertisement;
import service.AdvertisementService;
import security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/ads")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    private String extractUsername(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new SecurityException("توکن ارسال نشده یا نامعتبر است!");
        }
        String token = header.substring(7);
        if (token.trim().isEmpty() || !JwtUtil.isTokenValid(token)) {
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
                    "message", "آگهی با موفقیت ویرایش شد.",
                    "advertisementId", updatedAd.getId()
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