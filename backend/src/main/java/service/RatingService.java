package service;

import model.Advertisement;
import model.Rating;
import model.User;
import repository.AdvertisementRepository;
import repository.ConversationRepository;
import repository.RatingRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;
    private final ConversationRepository conversationRepository;

    public RatingService(RatingRepository ratingRepository,
                         UserRepository userRepository,
                         AdvertisementRepository advertisementRepository,
                         ConversationRepository conversationRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.advertisementRepository = advertisementRepository;
        this.conversationRepository = conversationRepository;
    }

    @Transactional
    public Rating addRating(Long adId, Integer score, String comment, String username) {
        if (score == null || score < 1 || score > 5) {
            throw new IllegalArgumentException("امتیاز باید عددی بین ۱ تا ۵ باشد.");
        }

        User buyer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        if (ad.getSeller().getId().equals(buyer.getId())) {
            throw new IllegalStateException("شما نمی‌توانید به آگهی خودتان امتیاز دهید!");
        }

        boolean hasConversed = conversationRepository.findByBuyerAndAdvertisement(buyer, ad).isPresent();
        if (!hasConversed) {
            throw new IllegalStateException("شما تنها در صورتی می‌توانید امتیاز دهید که درباره این آگهی با فروشنده چت کرده باشید!");
        }

        if (ratingRepository.existsByBuyerAndAdvertisement(buyer, ad)) {
            throw new IllegalStateException("شما قبلاً برای این آگهی به فروشنده امتیاز داده‌اید!");
        }

        Rating rating = new Rating();
        rating.setBuyer(buyer);
        rating.setSeller(ad.getSeller());
        rating.setAdvertisement(ad);
        rating.setScore(score);
        rating.setComment(comment);

        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSellerRatingStats(String sellerUsername) {
        User seller = userRepository.findByUsername(sellerUsername)
                .orElseThrow(() -> new RuntimeException("فروشنده یافت نشد!"));

        List<Rating> ratings = ratingRepository.findBySeller(seller);

        int count = ratings.size();
        double average = 0.0;

        if (count > 0) {
            double sum = ratings.stream().mapToDouble(Rating::getScore).sum();
            average = Math.round((sum / count) * 10.0) / 10.0;
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("seller", seller.getUsername());
        stats.put("fullName", seller.getFullName());
        stats.put("averageScore", average);
        stats.put("totalRatings", count);

        return stats;
    }
}
