package com.restobook.restaurantservice.repositories;

import com.restobook.restaurantservice.entities.MenuItem;
import com.restobook.restaurantservice.enums.MenuCategory;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<@NonNull MenuItem, @NonNull Long> {

    /**
     * Récupère tous les plats d'un restaurant triés par ordre d'affichage puis par nom.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats triée
     */
    List<MenuItem> findByRestaurantIdOrderByDisplayOrderAscNameAsc(Long restaurantId);

    /**
     * Récupère les plats d'un restaurant par catégorie triés par ordre d'affichage puis par nom.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param category la catégorie de menu
     * @return une liste de plats de la catégorie spécifiée
     */
    List<MenuItem> findByRestaurantIdAndCategoryOrderByDisplayOrderAscNameAsc(Long restaurantId, MenuCategory category);

    /**
     * Récupère les plats disponibles d'un restaurant triés par ordre d'affichage puis par nom.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats disponibles
     */
    List<MenuItem> findByRestaurantIdAndAvailableTrueOrderByDisplayOrderAscNameAsc(Long restaurantId);

    /**
     * Récupère les plats disponibles d'un restaurant par catégorie triés par ordre d'affichage puis par nom.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param category la catégorie de menu
     * @return une liste de plats disponibles de la catégorie spécifiée
     */
    List<MenuItem> findByRestaurantIdAndCategoryAndAvailableTrueOrderByDisplayOrderAscNameAsc(Long restaurantId, MenuCategory category);

    /**
     * Recherche des plats par mot-clé dans le nom pour un restaurant spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param keyword le mot-clé de recherche
     * @return une liste de plats correspondant à la recherche
     */
    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MenuItem> searchByName(@Param("restaurantId") Long restaurantId, @Param("keyword") String keyword);

    /**
     * Récupère les plats végétariens disponibles d'un restaurant triés par ordre d'affichage.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats végétariens disponibles
     */
    List<MenuItem> findByRestaurantIdAndVegetarianTrueAndAvailableTrueOrderByDisplayOrderAsc(Long restaurantId);

    /**
     * Récupère les plats vegan disponibles d'un restaurant triés par ordre d'affichage.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats vegan disponibles
     */
    List<MenuItem> findByRestaurantIdAndVeganTrueAndAvailableTrueOrderByDisplayOrderAsc(Long restaurantId);

    /**
     * Récupère les plats sans gluten disponibles d'un restaurant triés par ordre d'affichage.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats sans gluten disponibles
     */
    List<MenuItem> findByRestaurantIdAndGlutenFreeTrueAndAvailableTrueOrderByDisplayOrderAsc(Long restaurantId);

    /**
     * Supprime tous les plats d'un restaurant.
     * Cette méthode modifie la base de données.
     *
     * @param restaurantId l'identifiant du restaurant
     */
    @Modifying
    @Query("DELETE FROM MenuItem m WHERE m.restaurant.id = :restaurantId")
    void deleteByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Compte le nombre de plats d'un restaurant pour une catégorie spécifique.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param category la catégorie de menu
     * @return le nombre de plats dans la catégorie
     */
    long countByRestaurantIdAndCategory(Long restaurantId, MenuCategory category);

    /**
     * Vérifie l'existence d'un plat pour un restaurant spécifique.
     *
     * @param id l'identifiant du plat
     * @param restaurantId l'identifiant du restaurant
     * @return true si le plat existe pour ce restaurant, false sinon
     */
    boolean existsByIdAndRestaurantId(Long id, Long restaurantId);

    /**
     * Récupère une page de plats pour un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param pageable les informations de pagination
     * @return une page de plats
     */
    Page<@NonNull MenuItem> findByRestaurantId(Long restaurantId, Pageable pageable);
}
