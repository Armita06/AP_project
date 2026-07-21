package repository;

import model.Advertisement;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
    boolean existsByTitleAndSeller(String title, User seller);
    List<Advertisement> findByStatus(String status);
    List<Advertisement> findBySellerId(Long sellerId);
    List<Advertisement> findByCategory(String category);
    List<Advertisement> findByTitleContaining(String keyword);
}