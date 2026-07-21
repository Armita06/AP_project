package service;

import model.Advertisement;
import model.User;
import repository.AdvertisementRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public AdvertisementService(AdvertisementRepository advertisementRepository, UserRepository userRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    public Advertisement createAdvertisement(Advertisement ad, String username) {
        User realSeller = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        if (advertisementRepository.existsByTitleAndSeller(ad.getTitle(), realSeller)) {
            throw new RuntimeException("شما قبلاً آگهی با این عنوان ثبت کرده‌اید!");
        }

        ad.setSeller(realSeller);
        ad.setStatus("ACTIVE");
        ad.setCreatedAt(LocalDateTime.now());

        return advertisementRepository.save(ad);
    }

    public List<Advertisement> getAllAdvertisements() {
        return advertisementRepository.findAll();
    }

    public List<Advertisement> getAdsByCategory(String category) {
        return advertisementRepository.findByCategory(category);
    }

    public List<Advertisement> searchAds(String keyword) {
        return advertisementRepository.findByTitleContaining(keyword);
    }

    public void deleteAdvertisement(Long adId, String username) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی مورد نظر یافت نشد!"));

        if (!ad.getSeller().getUsername().equals(username)) {
            throw new RuntimeException("خطای امنیتی: شما اجازه حذف آگهی دیگران را ندارید!");
        }

        advertisementRepository.delete(ad);
    }
}