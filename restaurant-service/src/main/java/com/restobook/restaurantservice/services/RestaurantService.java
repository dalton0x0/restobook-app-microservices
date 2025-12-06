package com.restobook.restaurantservice.services;

import com.restobook.restaurantservice.dtos.request.CreateRestaurantRequest;
import com.restobook.restaurantservice.dtos.request.OpeningHoursRequest;
import com.restobook.restaurantservice.dtos.request.UpdateRestaurantRequest;
import com.restobook.restaurantservice.dtos.response.OpeningHoursResponse;
import com.restobook.restaurantservice.dtos.response.RestaurantResponse;
import com.restobook.restaurantservice.enums.DayOfWeek;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;

public interface RestaurantService {

    Page<@NonNull RestaurantResponse> getAllRestaurants(Pageable pageable);

    RestaurantResponse getRestaurantById(Long id);

    Page<@NonNull RestaurantResponse> getRestaurantsByCity(String city, Pageable pageable);

    Page<@NonNull RestaurantResponse> getRestaurantsByCuisineType(String cuisineType, Pageable pageable);

    Page<@NonNull RestaurantResponse> getTopRatedRestaurants(Pageable pageable);

    List<OpeningHoursResponse> getOpeningHours(Long restaurantId);

    List<String> getAllCities();

    List<String> getAllCuisineTypes();

    Page<@NonNull RestaurantResponse> searchRestaurants(String keyword, Pageable pageable);

    Page<@NonNull RestaurantResponse> getRestaurantsByFilters(String city, String cuisineType, Double minRating, Pageable pageable);

    RestaurantResponse createRestaurant(CreateRestaurantRequest request, Long ownerId);

    RestaurantResponse updateRestaurant(Long id, UpdateRestaurantRequest request, Long userId, String role);

    List<OpeningHoursResponse> updateOpeningHours(Long restaurantId, List<OpeningHoursRequest> requests, Long userId, String role);

    void updateRestaurantRating(Long restaurantId, Double newRating, Integer totalReviews);

    void deleteRestaurant(Long id, Long userId, String role);

    RestaurantResponse activateRestaurant(Long id, Long userId, String role);

    RestaurantResponse deactivateRestaurant(Long id, Long userId, String role);

    Page<@NonNull RestaurantResponse> getRestaurantsByOwner(Long ownerId, Pageable pageable);

    boolean restaurantExists(Long id);

    Integer getRestaurantCapacity(Long id);

    boolean isRestaurantOpen(Long id, DayOfWeek dayOfWeek, LocalTime time);
}
