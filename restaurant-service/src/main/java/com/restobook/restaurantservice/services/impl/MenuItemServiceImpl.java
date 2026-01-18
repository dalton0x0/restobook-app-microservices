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
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId) {
        log.debug("Récupération de tous les plats du restaurant ID: {}", restaurantId);

        findRestaurantByIdOrThrow(restaurantId);
        List<MenuItemResponse> menuItems = menuItemRepository
                .findByRestaurantIdOrderByDisplayOrderAscNameAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();

        log.debug("{} plats trouvés pour le restaurant ID: {}", menuItems.size(), restaurantId);

        return menuItems;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAvailableMenuItems(Long restaurantId) {
        log.debug("Récupération des plats disponibles du restaurant ID: {}", restaurantId);
        return menuItemRepository
                .findByRestaurantIdAndAvailableTrueOrderByDisplayOrderAscNameAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        log.debug("Récupération du plat ID: {}", id);

        MenuItem menuItem = findMenuItemByIdOrThrow(id);
        log.debug("Plat trouvé: {}", menuItem.getName());

        return MenuItemResponse.fromEntity(menuItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(Long restaurantId, MenuCategory category) {
        log.debug("Récupération des plats du restaurant ID: {} pour la catégorie: {}", restaurantId, category);
        return menuItemRepository
                .findByRestaurantIdAndCategoryOrderByDisplayOrderAscNameAsc(restaurantId, category)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getVegetarianItems(Long restaurantId) {
        log.debug("Recherche des plats végétariens du restaurant ID: {}", restaurantId);
        return menuItemRepository
                .findByRestaurantIdAndVegetarianTrueAndAvailableTrueOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getVeganItems(Long restaurantId) {
        log.debug("Recherche des plats vegan du restaurant ID: {}", restaurantId);
        return menuItemRepository
                .findByRestaurantIdAndVeganTrueAndAvailableTrueOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(MenuItemResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getGlutenFreeItems(Long restaurantId) {
        log.debug("Recherche des plats sans gluten du restaurant ID: {}", restaurantId);
        return menuItemRepository
                .findByRestaurantIdAndGlutenFreeTrueAndAvailableTrueOrderByDisplayOrderAsc(restaurantId)
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
    @Transactional
    public MenuItemResponse createMenuItem(Long restaurantId, CreateMenuItemRequest request, Long userId, String role) {
        log.info("Création d'un plat pour le restaurant ID: {}", restaurantId);

        Restaurant restaurant = findRestaurantByIdOrThrow(restaurantId);
        checkPermission(restaurant, userId, role);
        MenuItem menuItem = buildNewMenuItem(request, restaurant);
        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        log.info("Plat créé: {} (ID: {})", savedMenuItem.getName(), savedMenuItem.getId());

        return MenuItemResponse.fromEntity(savedMenuItem);
    }

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request, Long userId, String role) {
        log.info("Mise à jour du plat ID: {} par l'utilisateur ID: {}", id, userId);

        MenuItem menuItem = findMenuItemByIdOrThrow(id);
        checkPermission(menuItem.getRestaurant(), userId, role);
        updateMenuItemFields(menuItem, request);
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info("Plat mis à jour: {} (ID: {})", updatedMenuItem.getName(), id);

        return MenuItemResponse.fromEntity(updatedMenuItem);
    }

    @Override
    @Transactional
    public MenuItemResponse toggleAvailability(Long id, Long userId, String role) {
        log.info("Mise à jour de la disponibilité du plat ID: {}", id);

        MenuItem menuItem = findMenuItemByIdOrThrow(id);
        checkPermission(menuItem.getRestaurant(), userId, role);
        menuItem.setAvailable(!menuItem.getAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        log.info("Disponibilité du plat {} mise a jour: {}", updatedMenuItem.getName(), updatedMenuItem.getAvailable());

        return MenuItemResponse.fromEntity(updatedMenuItem);
    }

    @Override
    @Transactional
    public void deleteMenuItem(Long id, Long userId, String role) {
        log.info("Suppression du plat ID: {} par l'utilisateur ID: {}", id, userId);

        MenuItem menuItem = findMenuItemByIdOrThrow(id);
        checkPermission(menuItem.getRestaurant(), userId, role);
        menuItemRepository.deleteById(id);
        log.info("Plat supprimé: {} (ID: {})", menuItem.getName(), id);
    }

    /**
     * Recherche un restaurant par ID ou lève une exception si non trouvée.
     *
     * @param restaurantId l'identifiant du restaurant
     * @return le restaurant trouvé
     * @throws ResourceNotFoundException si le restaurant n'existe pas
     */
    private Restaurant findRestaurantByIdOrThrow(Long restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> {
                    log.debug("Restaurant non trouvé avec l'ID: {}", restaurantId);
                    return new ResourceNotFoundException("Restaurant", "id", restaurantId);
                });
    }

    /**
     * Recherche un plat par ID ou lève une exception si non trouvée.
     *
     * @param id l'identifiant du plat
     * @return le plat trouvé
     * @throws ResourceNotFoundException si le plat n'existe pas
     */
    private MenuItem findMenuItemByIdOrThrow(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Plat non trouvé avec l'ID: {}", id);
                    return new ResourceNotFoundException("Plat", "id", id);
                });
    }

    /**
     * Construit un nouveau plat a partir de la requête de creation.
     *
     * @param request la requête de creation contenant les informations du plat
     * @param restaurant le restaurant auquel appartient le plat
     * @return le nouveau plat construit
     */
    private MenuItem buildNewMenuItem(CreateMenuItemRequest request, Restaurant restaurant) {
        return MenuItem.builder()
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
    }

    /**
     * Met à jour les champs modifiables d'un plat.
     * Chaque champ est mis à jour uniquement s'il est present (non null).
     *
     * @param menuItem le plat a modifier
     * @param request la requête de mise à jour contenant les nouveaux champs
     */
    private void updateMenuItemFields(MenuItem menuItem, UpdateMenuItemRequest request) {
        if (request.getName() != null) {
            menuItem.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }

        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }

        if (request.getAllergens() != null) {
            menuItem.setAllergens(request.getAllergens());
        }

        if (request.getNutritionalInfo() != null) {
            menuItem.setNutritionalInfo(request.getNutritionalInfo());
        }

        if (request.getAvailable() != null) {
            menuItem.setAvailable(request.getAvailable());
        }

        if (request.getVegetarian() != null) {
            menuItem.setVegetarian(request.getVegetarian());
        }

        if (request.getVegan() != null) {
            menuItem.setVegan(request.getVegan());
        }

        if (request.getGlutenFree() != null) {
            menuItem.setGlutenFree(request.getGlutenFree());
        }

        if (request.getDisplayOrder() != null) {
            menuItem.setDisplayOrder(request.getDisplayOrder());
        }

        if (request.getCategory() != null) {
            menuItem.setCategory(request.getCategory());
        }
    }

    /**
     * Vérifie que l'utilisateur possède les permissions necessaires pour modifier un plat.
     * Les administrateurs ont accès à tous les plats.
     * Les propriétaires ont accès uniquement aux plats de leurs propres restaurants.
     *
     * @param restaurant le restaurant auquel appartient le plat
     * @param userId l'identifiant de l'utilisateur
     * @param role le role de l'utilisateur
     * @throws ForbiddenException si l'utilisateur n'a pas les droits necessaires
     */
    private void checkPermission(Restaurant restaurant, Long userId, String role) {
        // Les administrateurs ont accès à tous les plats
        if ("ROLE_ADMIN".equals(role)) {
            return;
        }

        // Les propriétaires ont accès uniquement aux plats de leurs propres restaurants
        if (restaurant.getOwnerId().equals(userId)) {
            return;
        }

        // Aucune permission correspondante, accès refuse
        throw new ForbiddenException("Vous n'avez pas les droits suffisants pour effectuer cette action.");
    }
}
