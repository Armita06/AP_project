package repository;

import model.Advertisement;
import model.Bookmark;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserAndAdvertisement(User user, Advertisement advertisement);

    boolean existsByUserAndAdvertisement(User user, Advertisement advertisement);

    List<Bookmark> findByUserOrderByCreatedAtDesc(User user);
}