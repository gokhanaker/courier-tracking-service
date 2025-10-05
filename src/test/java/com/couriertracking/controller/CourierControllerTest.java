package com.couriertracking.controller;

import com.couriertracking.dto.CourierCreateRequest;
import com.couriertracking.dto.CourierResponse;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.service.CourierService;
import com.couriertracking.service.DistanceCalculationService;
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

@WebMvcTest(CourierController.class)
@Import({com.couriertracking.security.SecurityConfig.class, com.couriertracking.security.ApiKeyAuthFilter.class})
@TestPropertySource(properties = {
    "courier-tracking.api.key=CT-SECURE-API-KEY-12345",
    "courier-tracking.api.header-name=X-API-Key"
})
@DisplayName("Courier Controller Tests")
class CourierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourierService courierService;

    @MockitoBean
    private DistanceCalculationService distanceCalculationService;

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_API_KEY = "CT-SECURE-API-KEY-12345";

    @Test
    @DisplayName("Should create courier successfully with valid request")
    void shouldCreateCourierSuccessfully() throws Exception {
        CourierCreateRequest request = new CourierCreateRequest(
                "John Doe",
                "john.doe@example.com",
                "+1234567890"
        );

        UUID courierId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        CourierResponse response = new CourierResponse(
                courierId,
                "John Doe",
                "john.doe@example.com",
                "+1234567890",
                createdAt
        );

        when(courierService.createCourier(any(CourierCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/couriers")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courierId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(courierService).createCourier(any(CourierCreateRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when creating courier with invalid email")
    void shouldReturn400WhenCreatingCourierWithInvalidEmail() throws Exception {
        CourierCreateRequest request = new CourierCreateRequest(
                "John Doe",
                "invalid-email",
                "+1234567890"
        );

        mockMvc.perform(post("/couriers")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courierService);
    }

    @Test
    @DisplayName("Should return 400 when creating courier with blank name")
    void shouldReturn400WhenCreatingCourierWithBlankName() throws Exception {
        CourierCreateRequest request = new CourierCreateRequest(
                "",
                "john.doe@example.com",
                "+1234567890"
        );

        mockMvc.perform(post("/couriers")
                        .header(API_KEY_HEADER, VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(courierService);
    }

    @Test
    @DisplayName("Should get courier by ID successfully")
    void shouldGetCourierByIdSuccessfully() throws Exception {
        UUID courierId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        CourierResponse response = new CourierResponse(
                courierId,
                "John Doe",
                "john.doe@example.com",
                "+1234567890",
                createdAt
        );

        when(courierService.getCourierById(courierId)).thenReturn(response);

        mockMvc.perform(get("/couriers/{courierId}", courierId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(courierId.toString()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phoneNumber").value("+1234567890"));

        verify(courierService).getCourierById(courierId);
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent courier")
    void shouldReturn404WhenGettingNonExistentCourier() throws Exception {
        UUID courierId = UUID.randomUUID();
        when(courierService.getCourierById(courierId))
                .thenThrow(new CourierNotFoundException("Courier not found"));

        mockMvc.perform(get("/couriers/{courierId}", courierId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isNotFound());

        verify(courierService).getCourierById(courierId);
    }

    @Test
    @DisplayName("Should get total travel distance successfully")
    void shouldGetTotalTravelDistanceSuccessfully() throws Exception {
        UUID courierId = UUID.randomUUID();
        Double totalDistance = 15.75;

        when(distanceCalculationService.getTotalTravelDistance(courierId))
                .thenReturn(totalDistance);

        mockMvc.perform(get("/couriers/{courierId}/total-travel-distance", courierId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.distance").value(totalDistance))
                .andExpect(jsonPath("$.unit").value("km"));

        verify(distanceCalculationService).getTotalTravelDistance(courierId);
    }

    @Test
    @DisplayName("Should return 404 when getting distance for non-existent courier")
    void shouldReturn404WhenGettingDistanceForNonExistentCourier() throws Exception {
        UUID courierId = UUID.randomUUID();
        when(distanceCalculationService.getTotalTravelDistance(courierId))
                .thenThrow(new CourierNotFoundException("Courier not found"));

        mockMvc.perform(get("/couriers/{courierId}/total-travel-distance", courierId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isNotFound());

        verify(distanceCalculationService).getTotalTravelDistance(courierId);
    }

    @Test
    @DisplayName("Should return zero distance for courier with no locations")
    void shouldReturnZeroDistanceForCourierWithNoLocations() throws Exception {
        UUID courierId = UUID.randomUUID();
        Double zeroDistance = 0.0;

        when(distanceCalculationService.getTotalTravelDistance(courierId))
                .thenReturn(zeroDistance);

        mockMvc.perform(get("/couriers/{courierId}/total-travel-distance", courierId)
                        .header(API_KEY_HEADER, VALID_API_KEY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.distance").value(zeroDistance))
                .andExpect(jsonPath("$.unit").value("km"));

        verify(distanceCalculationService).getTotalTravelDistance(courierId);
    }

    @Test
    @DisplayName("Should return 401 when no API key provided")
    void shouldReturn401WhenNoApiKeyProvided() throws Exception {
        UUID courierId = UUID.randomUUID();

        mockMvc.perform(get("/couriers/{courierId}", courierId))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(courierService);
    }

    @Test
    @DisplayName("Should return 401 when invalid API key provided")
    void shouldReturn401WhenInvalidApiKeyProvided() throws Exception {
        UUID courierId = UUID.randomUUID();

        mockMvc.perform(get("/couriers/{courierId}", courierId)
                        .header(API_KEY_HEADER, "invalid-api-key"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(courierService);
    }
}