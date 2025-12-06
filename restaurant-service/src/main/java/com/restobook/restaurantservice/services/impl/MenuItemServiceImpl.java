package com.restobook.restaurantservice.services.impl;

import com.restobook.restaurantservice.dtos.request.CreateMenuItemRequest;
import com.restobook.restaurantservice.dtos.request.UpdateMenuItemRequest;
import com.restobook.restaurantservice.dtos.response.MenuItemResponse;
import com.restobook.restaurantservice.entities.MenuItem;
import com.restobook.restaurantservice.entities.Restaurant;
import com.restobook.restaurantservice.enums.MenuCategory;
import com.restobook.restaurantservice.exceptions.ForbiddenException;
import com.restobook.restaurantservice.exceptions.ResourceNotFoundException;
import com.restobook.restaurantservice.repositories.MenuItemRepository;
import com.restobook.restaurantservice.repositories.RestaurantRepository;
import com.restobook.restaurantservice.services.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, CreateMenuItemRequest request, Long userId, String role) {
        log.info("Création d'un plat pour le restaurant ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(
                () -> new ResourceNotFoundException("Restaurant", "id", restaurantId)
        );

        checkPermission(restaurant, userId, role);

        MenuItem menuItem = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .allergens(request.getAllergens())
                .nutritionalInfo(request.getNutritionalInfo())
                .available(request.getAvailable())
                .vegetarian(request.getVegetarian())
                .vegan(request.getVegan())
                .glutenFree(request.getGlutenFree())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .restaurant(restaurant)
                .category(request.getCategory())
                .build();

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        log.info("Plat créé: {} (ID: {})", savedMenuItem.getName(), savedMenuItem.getId());

        return MenuItemResponse.fromEntity(savedMenuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        log.debug("Récupération du plat ID: {}", id);

        MenuItem menuItem = menuItemRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plat", "id", id)
        );
        log.debug("Plat trouvé: {}", menuItem.getName());

        return MenuItemResponse.fromEntity(menuItem);
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request, Long userId, String role) {
        log.info("Mise à jour du plat ID: {} par l'utilisateur ID: {}", id, userId);

        MenuItem menuItem = menuItemRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plat", "id", id)
        );

        checkPermission(menuItem.getRestaurant(), userId, role);

        if (request.getName() != null) menuItem.setName(request.getName().trim());
        if (request.getDescription() != null) menuItem.setDescription(request.getDescription());
        if (request.getPrice() != null) menuItem.setPrice(request.getPrice());
        if (request.getImageUrl() != null) menuItem.setImageUrl(request.getImageUrl());
        if (request.getAllergens() != null) menuItem.setAllergens(request.getAllergens());
        if (request.getNutritionalInfo() != null) menuItem.setNutritionalInfo(request.getNutritionalInfo());
        if (request.getAvailable() != null) menuItem.setAvailable(request.getAvailable());
        if (request.getVegetarian() != null) menuItem.setVegetarian(request.getVegetarian());
        if (request.getVegan() != null) menuItem.setVegan(request.getVegan());
        if (request.getGlutenFree() != null) menuItem.setGlutenFree(request.getGlutenFree());
        if (request.getDisplayOrder() != null) menuItem.setDisplayOrder(request.getDisplayOrder());
        if (request.getCategory() != null) menuItem.setCategory(request.getCategory());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info("Plat mis à jour: {} (ID: {})", updatedMenuItem.getName(), id);

        return MenuItemResponse.fromEntity(updatedMenuItem);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id, Long userId, String role) {
        log.info("Suppression du plat ID: {} par l'utilisateur ID: {}", id, userId);

        MenuItem menuItem = menuItemRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plat", "id", id)
        );

        checkPermission(menuItem.getRestaurant(), userId, role);
        menuItemRepository.deleteById(id);
        log.info("Plat supprimé: {} (ID: {})", menuItem.getName(), id);
    }

    @Override
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        log.debug("Récupération de tous les plats du restaurant ID: {}", restaurantId);

        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException("Restaurant", "id", restaurantId);
        }

        List<MenuItemResponse> menuItems = menuItemRepository.findByRestaurantIdOrderByDisplayOrderAscNameAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();

        log.debug("{} plats trouvés pour le restaurant ID: {}", menuItems.size(), restaurantId);

        return menuItems;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, MenuCategory category) {
        log.debug("Récupération des plats du restaurant ID: {} pour la catégorie: {}", restaurantId, category);
        return menuItemRepository.findByRestaurantIdAndCategoryOrderByDisplayOrderAscNameAsc(restaurantId, category)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAvailableMenuItems(Long restaurantId) {
        log.debug("Récupération des plats disponibles du restaurant ID: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndAvailableTrueOrderByDisplayOrderAscNameAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> searchMenuItems(Long restaurantId, String keyword) {
        log.debug("Recherche du plat '{}' pour le restaurant ID: {}", keyword, restaurantId);
        return menuItemRepository.searchByName(restaurantId, keyword)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getVegetarianItems(Long restaurantId) {
        log.debug("Recherche des plats végétariens du restaurant ID: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndVegetarianTrueAndAvailableTrueOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getVeganItems(Long restaurantId) {
        log.debug("Recherche des plats vegan du restaurant ID: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndVeganTrueAndAvailableTrueOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getGlutenFreeItems(Long restaurantId) {
        log.debug("Recherche des plats sans gluten du restaurant ID: {}", restaurantId);
        return menuItemRepository.findByRestaurantIdAndGlutenFreeTrueAndAvailableTrueOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    public MenuItemResponse toggleAvailability(Long id, Long userId, String role) {
        log.info("Mise à jour de la disponibilité du plat ID: {}", id);

        MenuItem menuItem = menuItemRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Plat", "id", id)
        );

        checkPermission(menuItem.getRestaurant(), userId, role);
        menuItem.setAvailable(!menuItem.getAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info("Disponibilité du plat {} mise à jour: {}", updatedMenuItem.getName(), updatedMenuItem.getAvailable());

        return MenuItemResponse.fromEntity(updatedMenuItem);
    }

    private void checkPermission(Restaurant restaurant, Long userId, String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return;
        }
        if (restaurant.getOwnerId().equals(userId)) {
            return;
        }
        throw new ForbiddenException("Vous n'avez pas les droits suffisants pour effectuer cette action.");
    }
}
