package com.couriertracking.service;

import com.couriertracking.dto.CourierCreateRequest;
import com.couriertracking.dto.CourierResponse;
import com.couriertracking.exception.CourierNotFoundException;
import com.couriertracking.model.Courier;
import com.couriertracking.repository.CourierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourierService Tests")
class CourierServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CourierService courierService;

    private CourierCreateRequest validRequest;
    private Courier savedCourier;
    private UUID courierId;

    @BeforeEach
    void setUp() {
        courierId = UUID.randomUUID();
        
        validRequest = new CourierCreateRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john.doe@courier.com");
        validRequest.setPhoneNumber("+905551234567");

        savedCourier = new Courier();
        savedCourier.setId(courierId);
        savedCourier.setName("John Doe");
        savedCourier.setEmail("john.doe@courier.com");
        savedCourier.setPhoneNumber("+905551234567");
        savedCourier.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should create courier successfully with valid request")
    void shouldCreateCourierSuccessfully() {
        when(courierRepository.save(any(Courier.class))).thenReturn(savedCourier);

        CourierResponse response = courierService.createCourier(validRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(courierId);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@courier.com");
        assertThat(response.getPhoneNumber()).isEqualTo("+905551234567");
        assertThat(response.getCreatedAt()).isNotNull();

        verify(courierRepository).save(any(Courier.class));
    }

    @Test
    @DisplayName("Should retrieve courier by ID successfully")
    void shouldGetCourierByIdSuccessfully() {
        when(courierRepository.findById(courierId)).thenReturn(Optional.of(savedCourier));

        CourierResponse response = courierService.getCourierById(courierId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(courierId);
        assertThat(response.getName()).isEqualTo("John Doe");
        assertThat(response.getEmail()).isEqualTo("john.doe@courier.com");

        verify(courierRepository).findById(courierId);
    }

    @Test
    @DisplayName("Should throw CourierNotFoundException when courier not found")
    void shouldThrowExceptionWhenCourierNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(courierRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.getCourierById(nonExistentId))
                .isInstanceOf(CourierNotFoundException.class)
                .hasMessageContaining("Courier not found with ID: " + nonExistentId);

        verify(courierRepository).findById(nonExistentId);
    }
}