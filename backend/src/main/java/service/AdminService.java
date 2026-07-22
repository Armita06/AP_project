package service;

import model.Advertisement;
import model.User;
import repository.AdvertisementRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public AdminService(AdvertisementRepository advertisementRepository, UserRepository userRepository) {
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    private User verifyAdmin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new SecurityException("کاربر یافت نشد!"));
        if (!"ADMIN".equals(user.getRole())) {
            throw new SecurityException("خطای دسترسی: شما مدیر سیستم نیستید!");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public List<Advertisement> getPendingAdvertisements(String username) {
        verifyAdmin(username);
        return advertisementRepository.findByStatusOrderByCreatedAtDesc("PENDING");
    }

    @Transactional
    public void approveAdvertisement(Long adId, String username) {
        verifyAdmin(username);
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        ad.setStatus("ACTIVE");
        ad.setRejectReason(null);
        advertisementRepository.save(ad);
    }

    @Transactional
    public void rejectAdvertisement(Long adId, String reason, String username) {
        verifyAdmin(username);
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        ad.setStatus("REJECTED");
        ad.setRejectReason(reason);
        advertisementRepository.save(ad);
    }

    @Transactional
    public void deleteAdvertisement(Long adId, String username) {
        verifyAdmin(username);
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        ad.setStatus("DELETED");
        advertisementRepository.save(ad);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers(String username) {
        verifyAdmin(username);
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserBlockStatus(Long userId, boolean block, String username) {
        verifyAdmin(username);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("کاربر مورد نظر یافت نشد!"));

        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("نمی‌توانید یک مدیر دیگر را مسدود کنید!");
        }

        user.setStatus(block ? "BLOCKED" : "ACTIVE");
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSystemStats(String username) {
        verifyAdmin(username);

        long totalUsers = userRepository.count();
        long pendingAds = advertisementRepository.findByStatusOrderByCreatedAtDesc("PENDING").size();
        long activeAds = advertisementRepository.findByStatusOrderByCreatedAtDesc("ACTIVE").size();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("pendingAds", pendingAds);
        stats.put("activeAds", activeAds);
        return stats;
    }
}
