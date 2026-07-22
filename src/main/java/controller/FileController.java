package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import security.JwtUtil;
import service.FileStorageService;

import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
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

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            extractUsername(header);

            String fileName = fileStorageService.storeFile(file);

            return ResponseEntity.ok(Map.of(
                    "message", "فایل با موفقیت آپلود شد.",
                    "fileName", fileName,
                    "fileUrl", "/uploads/" + fileName
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "خطا در آپلود فایل!", "error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<?> deleteFile(
            @PathVariable String fileName,
            @RequestHeader(value = "Authorization", required = false) String header) {
        try {
            extractUsername(header);

            fileStorageService.deleteFile(fileName);
            return ResponseEntity.ok(Map.of("message", "فایل با موفقیت حذف شد."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
