package repository;

import model.Advertisement;
import model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findBySellerOrderByCreatedAtDesc(User seller);

    List<Advertisement> findByStatusOrderByCreatedAtDesc(String status);
}