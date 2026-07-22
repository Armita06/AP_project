package repository;

import model.Advertisement;
import model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    List<Advertisement> findBySellerOrderByCreatedAtDesc(User seller);

    List<Advertisement> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT a FROM Advertisement a WHERE a.status = 'ACTIVE' " +
            "AND (:keyword IS NULL OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:category IS NULL OR a.category = :category) " +
            "AND (:city IS NULL OR a.city = :city) " +
            "AND (:minPrice IS NULL OR a.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR a.price <= :maxPrice)")
    List<Advertisement> searchAdvertisements(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("city") String city,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Sort sort
    );
}