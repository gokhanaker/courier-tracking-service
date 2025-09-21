package com.couriertracking.controller;

import com.couriertracking.dto.LocationUpdateRequest;
import com.couriertracking.dto.LocationUpdateResponse;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.service.LocationTrackingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@Import({com.couriertracking.config.security.SecurityConfig.class, com.couriertracking.config.security.ApiKeyAuthFilter.class})
@TestPropertySource(properties = {
    "courier-tracking.api.key=CT-SECURE-API-KEY-12345",
    "courier-tracking.api.header-name=X-API-Key"
})
@DisplayName("Location Controller Tests")
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LocationTrackingService locationTrackingService;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "CT-SECURE-API-KEY-12345";

    @Test
    @DisplayName("Should update location successfully with valid request")
    void shouldUpdateLocationSuccessfully() throws Exception {
        UUID courierId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();
        LocationUpdateRequest request = new LocationUpdateRequest(
                courierId,
                40.9923307,
                29.1244229,
                timestamp
        );

        LocationUpdateResponse response = new LocationUpdateResponse(
                UUID.randomUUID(), // locationId
                courierId,
                40.9923307,
                29.1244229,
                timestamp,
                "Location updated successfully"
        );

        when(locationTrackingService.updateCourierLocation(any(LocationUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.courierId").value(courierId.toString()))
                .andExpect(jsonPath("$.latitude").value(40.9923307))
                .andExpect(jsonPath("$.longitude").value(29.1244229))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").value("Location updated successfully"));

        verify(locationTrackingService).updateCourierLocation(any(LocationUpdateRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when updating location with null courier ID")
    void shouldReturn400WhenUpdatingLocationWithNullCourierId() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                null,
                40.9923307,
                29.1244229,
                LocalDateTime.now()
        );

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(locationTrackingService);
    }

    @Test
    @DisplayName("Should return 400 when updating location with invalid latitude")
    void shouldReturn400WhenUpdatingLocationWithInvalidLatitude() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                UUID.randomUUID(),
                95.0, // Invalid latitude > 90
                29.1244229,
                LocalDateTime.now()
        );

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(locationTrackingService);
    }

    @Test
    @DisplayName("Should return 400 when updating location with invalid longitude")
    void shouldReturn400WhenUpdatingLocationWithInvalidLongitude() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                UUID.randomUUID(),
                40.9923307,
                185.0, // Invalid longitude > 180
                LocalDateTime.now()
        );

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(locationTrackingService);
    }


    @Test
    @DisplayName("Should return 400 when updating location with null timestamp")
    void shouldReturn400WhenUpdatingLocationWithNullTimestamp() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                UUID.randomUUID(),
                40.9923307,
                29.1244229,
                null
        );

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(locationTrackingService);
    }


    @Test
    @DisplayName("Should return 404 when updating location for non-existent courier")
    void shouldReturn404WhenUpdatingLocationForNonExistentCourier() throws Exception {
        UUID courierId = UUID.randomUUID();
        LocationUpdateRequest request = new LocationUpdateRequest(
                courierId,
                40.9923307,
                29.1244229,
                LocalDateTime.now()
        );

        when(locationTrackingService.updateCourierLocation(any(LocationUpdateRequest.class)))
                .thenThrow(new CourierNotFoundException("Courier not found with id: " + courierId));

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(locationTrackingService).updateCourierLocation(any(LocationUpdateRequest.class));
    }

    @Test
    @DisplayName("Should return 401 when no API key provided")
    void shouldReturn401WhenNoApiKeyProvided() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                UUID.randomUUID(),
                40.9923307,
                29.1244229,
                LocalDateTime.now()
        );

        mockMvc.perform(post("/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(locationTrackingService);
    }

    @Test
    @DisplayName("Should return 401 when invalid API key provided")
    void shouldReturn401WhenInvalidApiKeyProvided() throws Exception {
        LocationUpdateRequest request = new LocationUpdateRequest(
                UUID.randomUUID(),
                40.9923307,
                29.1244229,
                LocalDateTime.now()
        );

        mockMvc.perform(post("/locations")
                        .header(API_KEY_HEADER, "invalid-api-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(locationTrackingService);
    }
}