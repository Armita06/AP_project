package service;

import model.Advertisement;
import model.Comment;
import model.User;
import repository.AdvertisementRepository;
import repository.CommentRepository;
import repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, AdvertisementRepository advertisementRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.advertisementRepository = advertisementRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long adId, String text, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("کاربر یافت نشد!"));

        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        Comment comment = new Comment();
        comment.setText(text);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setAuthor(author);
        comment.setAdvertisement(ad);

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsForAd(Long adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("آگهی یافت نشد!"));

        return commentRepository.findByAdvertisement(ad);
    }
}