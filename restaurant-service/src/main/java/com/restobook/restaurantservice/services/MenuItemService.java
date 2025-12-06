package com.restobook.restaurantservice.services;

import com.restobook.restaurantservice.dtos.request.CreateMenuItemRequest;
import com.restobook.restaurantservice.dtos.request.UpdateMenuItemRequest;
import com.restobook.restaurantservice.dtos.response.MenuItemResponse;
import com.restobook.restaurantservice.enums.MenuCategory;

import java.util.List;

public interface MenuItemService {

    /**
     * Liste tous les plats d'un restaurant
     */
    List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId);

    /**
     * Liste les plats disponibles d'un restaurant
     */
    List<MenuItemResponse> getAvailableMenuItems(Long restaurantId);

    /**
     * Récupère un plat par son ID
     */
    MenuItemResponse getMenuItemById(Long id);

    /**
     * Liste les plats d'un restaurant par catégorie
     */
    List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, MenuCategory category);

    /**
     * Liste les plats végétariens disponibles d'un restaurant
     */
    List<MenuItemResponse> getVegetarianItems(Long restaurantId);

    /**
     * Liste les plats vegan disponibles d'un restaurant
     */
    List<MenuItemResponse> getVeganItems(Long restaurantId);

    /**
     * Liste les plats sans gluten disponibles d'un restaurant
     */
    List<MenuItemResponse> getGlutenFreeItems(Long restaurantId);

    /**
     * Recherche des plats par mot-clé dans un restaurant
     */
    List<MenuItemResponse> searchMenuItems(Long restaurantId, String keyword);

    /**
     * Crée un nouveau plat pour un restaurant
     */
    MenuItemResponse createMenuItem(Long restaurantId, CreateMenuItemRequest request, Long userId, String role);

    /**
     * Met à jour un plat
     */
    MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request, Long userId, String role);

    /**
     * Active ou désactive la disponibilité d'un plat
     */
    MenuItemResponse toggleAvailability(Long id, Long userId, String role);

    /**
     * Supprime un plat
     */
    void deleteMenuItem(Long id, Long userId, String role);
}
