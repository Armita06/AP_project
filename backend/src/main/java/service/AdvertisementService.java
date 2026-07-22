package service;

import model.Advertisement;
import model.User;
import org.springframework.data.domain.Sort;
import repository.AdvertisementRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository, UserRepository userRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Advertisement> searchActiveAdvertisements(String keyword, String category, String city, Double minPrice, Double maxPrice, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        if ("cheapest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "price");
        } else if ("expensive".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "price");
        }

        return advertisementRepository.searchAdvertisements(keyword, category, city, minPrice, maxPrice, sort);
    }

    @Transactional(readOnly = true)
    public List<Advertisement> getUserAdvertisements(String username) {
        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("کاربر یافت نشد!"));
        return advertisementRepository.findBySellerOrderByCreatedAtDesc(seller);
    }

    @Transactional
    public Advertisement createAdvertisement(Advertisement adRequest, String username) {
        if (adRequest.getTitle() == null || adRequest.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("عنوان آگهی نمی‌تواند خالی باشد!");
        }
        if (adRequest.getPrice() == null || adRequest.getPrice() < 0) {
            throw new IllegalArgumentException("قیمت آگهی نامعتبر است!");
        }
        if (adRequest.getCategory() == null || adRequest.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("دسته‌بندی آگهی نمی‌تواند خالی باشد!");
        }
        if (adRequest.getCity() == null || adRequest.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("شهر آگهی نمی‌تواند خالی باشد!");
        }

        User seller = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("کاربر یافت نشد!"));

        adRequest.setSeller(seller);
        adRequest.setStatus("PENDING");

        return advertisementRepository.save(adRequest);
    }

    @Transactional
    public Advertisement updateAdvertisement(Long adId, Advertisement adRequest, String username) {
        Advertisement existingAd = advertisementRepository.findById(adId)
                .orElseThrow(() -> new NoSuchElementException("آگهی یافت نشد!"));

        if (!existingAd.getSeller().getUsername().equals(username)) {
            throw new IllegalStateException("شما فقط مجاز به ویرایش آگهی‌های خودتان هستید!");
        }

        if ("DELETED".equals(existingAd.getStatus()) || "SOLD".equals(existingAd.getStatus())) {
            throw new IllegalStateException("آگهی حذف شده یا فروخته شده قابل ویرایش نیست!");
        }

        if (adRequest.getTitle() != null && !adRequest.getTitle().trim().isEmpty()) {
            existingAd.setTitle(adRequest.getTitle());
        }
        if (adRequest.getDescription() != null) {
            existingAd.setDescription(adRequest.getDescription());
        }
        if (adRequest.getPrice() != null && adRequest.getPrice() >= 0) {
            existingAd.setPrice(adRequest.getPrice());
        }
        if (adRequest.getCategory() != null && !adRequest.getCategory().trim().isEmpty()) {
            existingAd.setCategory(adRequest.getCategory());
        }
        if (adRequest.getCity() != null && !adRequest.getCity().trim().isEmpty()) {
            existingAd.setCity(adRequest.getCity());
        }
        if (adRequest.getImageUrl() != null) {
            existingAd.setImageUrl(adRequest.getImageUrl());
        }

        existingAd.setStatus("PENDING");

        return advertisementRepository.save(existingAd);
    }

    @Transactional
    public void markAsSold(Long adId, String username) {
        Advertisement existingAd = advertisementRepository.findById(adId)
                .orElseThrow(() -> new NoSuchElementException("آگهی یافت نشد!"));

        if (!existingAd.getSeller().getUsername().equals(username)) {
            throw new IllegalStateException("شما فقط مجاز به تغییر وضعیت آگهی‌های خودتان هستید!");
        }

        existingAd.setStatus("SOLD");
        advertisementRepository.save(existingAd);
    }

    @Transactional
    public void deleteAdvertisement(Long adId, String username) {
        Advertisement existingAd = advertisementRepository.findById(adId)
                .orElseThrow(() -> new NoSuchElementException("آگهی یافت نشد!"));

        if (!existingAd.getSeller().getUsername().equals(username)) {
            throw new IllegalStateException("شما فقط مجاز به حذف آگهی‌های خودتان هستید!");
        }

        existingAd.setStatus("DELETED");
        advertisementRepository.save(existingAd);
    }
}