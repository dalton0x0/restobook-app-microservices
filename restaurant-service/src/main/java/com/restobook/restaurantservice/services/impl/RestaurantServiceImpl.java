package com.restobook.restaurantservice.services.impl;

import com.restobook.restaurantservice.dtos.request.CreateRestaurantRequest;
import com.restobook.restaurantservice.dtos.request.OpeningHoursRequest;
import com.restobook.restaurantservice.dtos.request.UpdateRestaurantRequest;
import com.restobook.restaurantservice.dtos.response.OpeningHoursResponse;
import com.restobook.restaurantservice.dtos.response.RestaurantResponse;
import com.restobook.restaurantservice.entities.OpeningHour;
import com.restobook.restaurantservice.entities.Restaurant;
import com.restobook.restaurantservice.enums.DayOfWeek;
import com.restobook.restaurantservice.exceptions.ForbiddenException;
import com.restobook.restaurantservice.exceptions.ResourceNotFoundException;
import com.restobook.restaurantservice.repositories.OpeningHourRepository;
import com.restobook.restaurantservice.repositories.RestaurantRepository;
import com.restobook.restaurantservice.services.RestaurantService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final OpeningHourRepository openingHourRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getAllActiveRestaurants(Pageable pageable) {
        log.debug("Récupération de tous les restaurants actifs - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());
        return restaurantRepository.findByActiveTrue(pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getAllRestaurants(Pageable pageable) {
        log.debug("Récupération de tous les restaurants actifs et non actifs - Page: {}, Taille: {}", pageable.getPageNumber(), pageable.getPageSize());
        return restaurantRepository.findAll(pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(Long id) {
        log.debug("Récupération du restaurant ID: {}", id);

        Restaurant restaurant = findRestaurantByIdOrThrow(id);
        log.debug("Restaurant trouvé: {}", restaurant.getName());

        return RestaurantResponse.fromEntityWithHours(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getRestaurantsByCity(String city, Pageable pageable) {
        log.debug("Récupération des restaurants de la ville: {}", city);
        return restaurantRepository.findByCityIgnoreCaseAndActiveTrue(city, pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getRestaurantsByCuisineType(String cuisineType, Pageable pageable) {
        log.debug("Récupération des restaurants de type cuisine: {}", cuisineType);
        return restaurantRepository.findByCuisineTypeIgnoreCaseAndActiveTrue(cuisineType, pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getTopRatedRestaurants(Pageable pageable) {
        log.debug("Récupération des restaurants les mieux notes");
        return restaurantRepository.findByActiveTrueAndTotalReviewsGreaterThanOrderByAverageRatingDesc(0, pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpeningHoursResponse> getOpeningHours(Long restaurantId) {
        log.debug("Récupération des horaires d'ouverture du restaurant ID: {}", restaurantId);
        findRestaurantByIdOrThrow(restaurantId);
        return openingHourRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId)
                .stream()
                .map(OpeningHoursResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OpeningHoursResponse> getOpeningHoursByDayOfWeek(Long restaurantId, DayOfWeek dayOfWeek) {
        log.debug("Récupération des horaires du restaurant ID: {} pour: {}", restaurantId, dayOfWeek);
        findRestaurantByIdOrThrow(restaurantId);
        return openingHourRepository.findAllByRestaurantIdAndDayOfWeek(restaurantId, dayOfWeek)
                .stream()
                .map(OpeningHoursResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCities() {
        log.debug("Recherche des villes avec des restaurants");
        return restaurantRepository.findDistinctCities();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCuisineTypes() {
        log.debug("Recherche des types de cuisine disponibles");
        return restaurantRepository.findDistinctCuisineTypes();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> searchRestaurants(String keyword, Pageable pageable) {
        log.debug("Recherche des restaurants avec le mot-clé: {}", keyword);
        return restaurantRepository.searchRestaurants(keyword, pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getRestaurantsByFilters(String city, String cuisineType, Double minRating, Pageable pageable) {
        log.debug("Recherche avancée des restaurants - Ville: {}, Cuisine: {}, Note min: {}", city, cuisineType, minRating);
        return restaurantRepository.findByFilters(city, cuisineType, minRating, pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<@NonNull RestaurantResponse> getRestaurantsByOwner(Long ownerId, Pageable pageable) {
        log.debug("Récupération des restaurants du propriétaire ID: {}", ownerId);
        return restaurantRepository.findByOwnerId(ownerId, pageable)
                .map(RestaurantResponse::fromEntity);
    }

    @Override
    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId) {
        log.info("Création d'un nouveau restaurant par le propriétaire ID: {}", ownerId);

        Restaurant newRestaurant = buildNewRestaurant(request, ownerId);
        Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);
        log.info("Restaurant créé: {} (ID: {})", savedRestaurant.getName(), savedRestaurant.getId());

        if (request.getOpeningHours() != null && !request.getOpeningHours().isEmpty()) {
            saveOpeningHours(savedRestaurant, request.getOpeningHours());
        }

        return RestaurantResponse.fromEntity(savedRestaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request, Long userId, String role) {
        log.info("Mise à jour du restaurant ID: {} par l'utilisateur ID: {}", id, userId);

        Restaurant restaurant = findRestaurantByIdOrThrow(id);
        checkPermission(restaurant, userId, role);
        updateRestaurantFields(restaurant, request);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant mis à jour: {} (ID: {})", updatedRestaurant.getName(), id);

        return RestaurantResponse.fromEntity(updatedRestaurant);
    }

    @Override
    @Transactional
    public List<OpeningHoursResponse> updateOpeningHours(Long restaurantId, List<OpeningHoursRequest> requests, Long userId, String role) {
        log.info("Mise à jour des horaires d'ouverture du restaurant ID: {}", restaurantId);

        Restaurant restaurant = findRestaurantByIdOrThrow(restaurantId);
        checkPermission(restaurant, userId, role);
        openingHourRepository.deleteByRestaurantId(restaurantId);
        saveOpeningHours(restaurant, requests);
        log.info("Horaires d'ouverture mis à jour pour le restaurant: {}", restaurant.getName());

        return openingHourRepository.findByRestaurantIdOrderByDayOfWeek(restaurantId)
                .stream()
                .map(OpeningHoursResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void updateRestaurantRating(Long restaurantId, Double newRating, Integer totalReviews) {
        log.info("Mise à jour de la note du restaurant ID: {} - Note: {}, Avis: {}", restaurantId, newRating, totalReviews);

        Restaurant restaurant = findRestaurantByIdOrThrow(restaurantId);
        restaurant.setAverageRating(newRating);
        restaurant.setTotalReviews(totalReviews);
        restaurantRepository.save(restaurant);

        log.info("Note du restaurant {} mise à jour avec succès", restaurant.getName());
    }

    @Override
    @Transactional
    public void deleteRestaurant(Long id, Long userId, String role) {
        log.info("Suppression du restaurant ID: {} par l'utilisateur ID: {}", id, userId);

        Restaurant restaurant = findRestaurantByIdOrThrow(id);
        checkPermission(restaurant, userId, role);
        restaurantRepository.delete(restaurant);
        log.info("Restaurant supprimé: {} (ID: {})", restaurant.getName(), id);
    }

    @Override
    @Transactional
    public RestaurantResponse activateRestaurant(Long id, Long userId, String role) {
        log.info("Activation du restaurant ID: {} par l'utilisateur ID: {}", id, userId);

        Restaurant restaurant = findRestaurantByIdOrThrow(id);
        restaurant.setActive(true);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant activé: {}", savedRestaurant.getName());

        return RestaurantResponse.fromEntity(savedRestaurant);
    }

    @Override
    @Transactional
    public RestaurantResponse deactivateRestaurant(Long id, Long userId, String role) {
        log.info("Désactivation du restaurant ID: {} par l'utilisateur ID: {}", id, userId);

        Restaurant restaurant = findRestaurantByIdOrThrow(id);
        restaurant.setActive(false);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant désactive: {}", savedRestaurant.getName());

        return RestaurantResponse.fromEntity(savedRestaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean restaurantExists(Long id) {
        log.debug("Vérification de l'existence du restaurant ID: {}", id);
        return restaurantRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getRestaurantCapacity(Long id) {
        log.debug("Obtention de la capacité du restaurant ID: {}", id);
        return restaurantRepository.findById(id)
                .map(Restaurant::getTotalCapacity)
                .orElse(0);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRestaurantOpen(Long id, DayOfWeek dayOfWeek, LocalTime time) {
        log.debug("Vérification des heures d'ouverture du restaurant ID: {} - {} a {}", id, dayOfWeek, time);
        Optional<OpeningHour> hour = openingHourRepository.findByRestaurantIdAndDayOfWeek(id, dayOfWeek);
        return hour.map(h -> h.isOpenAt(time)).orElse(false);
    }

    /**
     * Recherche un restaurant par ID ou lève une exception si non trouvée.
     *
     * @param id l'identifiant du restaurant
     * @return le restaurant trouvé
     * @throws ResourceNotFoundException si le restaurant n'existe pas
     */
    private Restaurant findRestaurantByIdOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> {
                    log.debug("Restaurant non trouvé avec l'ID: {}", id);
                    return new ResourceNotFoundException("id", id);
                });
    }

    /**
     * Construit un nouveau restaurant à partir de la requête de creation.
     *
     * @param request la requête de creation contenant les informations du restaurant
     * @param ownerId l'identifiant du propriétaire
     * @return le nouveau restaurant construit
     */
    private Restaurant buildNewRestaurant(CreateRestaurantRequest request, Long ownerId) {
        return Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .city(request.getCity())
                .postalCode(request.getPostalCode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .imageUrl(request.getImageUrl())
                .cuisineType(request.getCuisineType())
                .totalCapacity(request.getTotalCapacity())
                .ownerId(ownerId)
                .active(true)
                .build();
    }

    /**
     * Met à jour les champs modifiables d'un restaurant.
     * Chaque champ est mis à jour uniquement s'il est present et non vide.
     *
     * @param restaurant le restaurant a modifier
     * @param request la requête de mise à jour contenant les nouveaux champs
     */
    private void updateRestaurantFields(Restaurant restaurant, UpdateRestaurantRequest request) {
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            restaurant.setName(request.getName().trim());
        }

        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            restaurant.setDescription(request.getDescription());
        }

        if (request.getAddress() != null && !request.getAddress().trim().isEmpty()) {
            restaurant.setAddress(request.getAddress().trim());
        }

        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            restaurant.setCity(request.getCity().trim());
        }

        if (request.getPostalCode() != null && !request.getPostalCode().trim().isEmpty()) {
            restaurant.setPostalCode(request.getPostalCode());
        }

        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            restaurant.setPhone(request.getPhone());
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            restaurant.setEmail(request.getEmail());
        }

        if (request.getImageUrl() != null && !request.getImageUrl().trim().isEmpty()) {
            restaurant.setImageUrl(request.getImageUrl());
        }

        if (request.getCuisineType() != null && !request.getCuisineType().trim().isEmpty()) {
            restaurant.setCuisineType(request.getCuisineType());
        }

        if (request.getTotalCapacity() != null) {
            restaurant.setTotalCapacity(request.getTotalCapacity());
        }
    }

    /**
     * Sauvegarde les horaires d'ouverture pour un restaurant.
     *
     * @param restaurant le restaurant auquel il faut mettre les horaires
     * @param requests la liste des horaires à sauvegarder
     */
    private void saveOpeningHours(Restaurant restaurant, List<OpeningHoursRequest> requests) {
        for (OpeningHoursRequest request : requests) {
            OpeningHour openingHours = OpeningHour.builder()
                    .restaurant(restaurant)
                    .dayOfWeek(request.getDayOfWeek())
                    .openingTimeMorning(request.getOpeningTimeMorning())
                    .closingTimeMorning(request.getClosingTimeMorning())
                    .openingTimeEvening(request.getOpeningTimeEvening())
                    .closingTimeEvening(request.getClosingTimeEvening())
                    .closed(request.getClosed())
                    .build();
            openingHourRepository.save(openingHours);
        }
    }

    /**
     * Vérifie que l'utilisateur possède les permissions necessaires pour modifier un restaurant.
     * Les administrateurs ont accès à tous les restaurants.
     * Les propriétaires ont accès uniquement à leurs propres restaurants.
     *
     * @param restaurant le restaurant à modifier
     * @param userId l'identifiant de l'utilisateur
     * @param role le rôle de l'utilisateur
     * @throws ForbiddenException si l'utilisateur n'a pas les droits necessaires
     */
    private void checkPermission(Restaurant restaurant, Long userId, String role) {
        // Les administrateurs ont accès à tous les restaurants
        if ("ROLE_ADMIN".equals(role)) {
            return;
        }

        // Les propriétaires ont accès uniquement à leurs propres restaurants
        if (restaurant.getOwnerId().equals(userId)) {
            return;
        }

        // Aucune permission correspondante, accès refuse
        throw new ForbiddenException("Vous n'avez pas les droits suffisants pour éffectuer cette action.");
    }
}
