package service;

import model.Advertisement;
import model.Bookmark;
import model.User;
import repository.AdvertisementRepository;
import repository.BookmarkRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final AdvertisementRepository advertisementRepository;

    public BookmarkService(BookmarkRepository bookmarkRepository,
                           UserRepository userRepository,
                           AdvertisementRepository advertisementRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.userRepository = userRepository;
        this.advertisementRepository = advertisementRepository;
    }

    @Transactional
    public boolean toggleBookmark(Long adId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد!"));

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("آگهی یافت نشد!"));

        if (ad.getStatus() == null || !ad.getStatus().equals("ACTIVE")) {
            throw new IllegalStateException("فقط آگهی‌های فعال قابل نشان‌کردن هستند!");
        }

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndAdvertisement(user, ad);

        if (existingBookmark.isPresent()) {
            bookmarkRepository.delete(existingBookmark.get());
            return false;
        } else {
            Bookmark newBookmark = new Bookmark();
            newBookmark.setUser(user);
            newBookmark.setAdvertisement(ad);
            bookmarkRepository.save(newBookmark);
            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<Bookmark> getUserBookmarks(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد!"));
        return bookmarkRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public boolean isAdBookmarked(Long adId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("کاربر یافت نشد!"));

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("آگهی یافت نشد!"));

        return bookmarkRepository.existsByUserAndAdvertisement(user, ad);
    }
}