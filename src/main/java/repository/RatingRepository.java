package repository;

import model.Advertisement;
import model.Rating;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    boolean existsByBuyerAndAdvertisement(User buyer, Advertisement advertisement);

    List<Rating> findBySeller(User seller);
}