package com.restobook.restaurantservice.repositories;

import com.restobook.restaurantservice.entities.OpeningHour;
import com.restobook.restaurantservice.enums.DayOfWeek;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpeningHourRepository extends JpaRepository<OpeningHour, Long> {

    List<OpeningHour> findByRestaurantIdOrderByDayOfWeek(Long restaurantId);

    Optional<OpeningHour> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    List<OpeningHour> findAllByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    @Modifying
    @Query("DELETE FROM OpeningHour o WHERE o.restaurant.id = :restaurantId")
    void deleteByRestaurantId(@Param("restaurantId") Long restaurantId);

    boolean existsByRestaurantId(Long restaurantId);
}
