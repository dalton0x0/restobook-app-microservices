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

    /**
     * Récupère tous les horaires d'ouverture d'un restaurant triés par jour de la semaine.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste d'horaires d'ouverture triée
     */
    List<OpeningHour> findByRestaurantIdOrderByDayOfWeek(Long restaurantId);

    /**
     * Recherche l'horaire d'ouverture d'un restaurant pour un jour spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dayOfWeek le jour de la semaine
     * @return un Optional contenant l'horaire d'ouverture si trouvé
     */
    Optional<OpeningHour> findByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    /**
     * Récupère tous les horaires d'ouverture d'un restaurant pour un jour spécifique.
     * Utile si un restaurant a plusieurs plages horaires pour un même jour.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param dayOfWeek le jour de la semaine
     * @return une liste d'horaires d'ouverture pour le jour spécifié
     */
    List<OpeningHour> findAllByRestaurantIdAndDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek);

    /**
     * Supprime tous les horaires d'ouverture d'un restaurant.
     * Cette méthode modifie la base de données.
     *
     * @param restaurantId l'identifiant du restaurant
     */
    @Modifying
    @Query("DELETE FROM OpeningHour o WHERE o.restaurant.id = :restaurantId")
    void deleteByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Vérifie si un restaurant possède des horaires d'ouverture définis.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return true si des horaires existent, false sinon
     */
    boolean existsByRestaurantId(Long restaurantId);
}
