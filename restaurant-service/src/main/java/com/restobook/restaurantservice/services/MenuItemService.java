package com.restobook.restaurantservice.services;

import com.restobook.restaurantservice.dtos.request.CreateMenuItemRequest;
import com.restobook.restaurantservice.dtos.request.UpdateMenuItemRequest;
import com.restobook.restaurantservice.dtos.response.MenuItemResponse;
import com.restobook.restaurantservice.enums.MenuCategory;

import java.util.List;

public interface MenuItemService {

    /**
     * Liste tous les plats d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats
     */
    List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId);

    /**
     * Liste les plats disponibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats disponibles
     */
    List<MenuItemResponse> getAvailableMenuItems(Long restaurantId);

    /**
     * Récupère un plat par son identifiant.
     *
     * @param id l'identifiant du plat
     * @return les informations du plat
     */
    MenuItemResponse getMenuItemById(Long id);

    /**
     * Liste les plats d'un restaurant par catégorie.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param category la catégorie de menu
     * @return une liste de plats de la catégorie spécifiée
     */
    List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, MenuCategory category);

    /**
     * Liste les plats végétariens disponibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats végétariens
     */
    List<MenuItemResponse> getVegetarianItems(Long restaurantId);

    /**
     * Liste les plats vegan disponibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats vegan
     */
    List<MenuItemResponse> getVeganItems(Long restaurantId);

    /**
     * Liste les plats sans gluten disponibles d'un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return une liste de plats sans gluten
     */
    List<MenuItemResponse> getGlutenFreeItems(Long restaurantId);

    /**
     * Recherche des plats par mot-clé dans un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param keyword le mot-clé de recherche
     * @return une liste de plats correspondant à la recherche
     */
    List<MenuItemResponse> searchMenuItems(Long restaurantId, String keyword);

    /**
     * Crée un nouveau plat pour un restaurant.
     *
     * @param restaurantId l'identifiant du restaurant
     * @param request les informations du plat à créer
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations du plat créé
     */
    MenuItemResponse createMenuItem(Long restaurantId, CreateMenuItemRequest request, Long userId, String role);

    /**
     * Met à jour les informations d'un plat.
     *
     * @param id l'identifiant du plat
     * @param request les nouvelles informations du plat
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations du plat mis à jour
     */
    MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request, Long userId, String role);

    /**
     * Active ou désactive la disponibilité d'un plat.
     *
     * @param id l'identifiant du plat
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     * @return les informations du plat avec le statut mis à jour
     */
    MenuItemResponse toggleAvailability(Long id, Long userId, String role);

    /**
     * Supprime un plat de façon définitive.
     *
     * @param id l'identifiant du plat à supprimer
     * @param userId l'identifiant de l'utilisateur effectuant l'opération
     * @param role le rôle de l'utilisateur
     */
    void deleteMenuItem(Long id, Long userId, String role);
}
