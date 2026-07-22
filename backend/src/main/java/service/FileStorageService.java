package service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService() {
        this.fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("خطا در ایجاد پوشه ذخیره‌سازی فایل‌ها.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("فایل ارسالی نامعتبر است!");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";

            if (StringUtils.hasText(originalFilename)) {
                String extension = StringUtils.getFilenameExtension(originalFilename);
                if (StringUtils.hasText(extension)) {
                    fileExtension = "." + extension;
                }
            }

            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFilename).normalize();

            if (targetLocation.getParent() == null || !targetLocation.getParent().equals(this.fileStorageLocation)) {
                throw new SecurityException("مسیر ذخیره‌سازی فایل نامعتبر است!");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFilename;

        } catch (IOException ex) {
            throw new RuntimeException("خطا در ذخیره فایل: " + ex.getMessage());
        }
    }

    public void deleteFile(String filename) {
        if (StringUtils.hasText(filename)) {
            try {
                Path filePath = this.fileStorageLocation.resolve(filename).normalize();

                if (filePath.getParent() == null || !filePath.getParent().equals(this.fileStorageLocation)) {
                    throw new SecurityException("تلاش غیرمجاز برای دسترسی به فایل‌های خارج از پوشه مجاز!");
                }

                Files.deleteIfExists(filePath);
            } catch (IOException ex) {
                throw new RuntimeException("خطا در حذف فایل: " + ex.getMessage());
            }
        }
    }
}