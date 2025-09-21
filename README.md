# Courier Tracking Service

A RESTful web service for tracking courier locations and monitoring store visits, built with **Java + Spring Boot** with a scalable, maintainable architecture.

## ✨ Features

### Core Functionality

- ✅ **Location Tracking** - Submit and store courier GPS coordinates with timestamps
- ✅ **Store Entrance Detection** - Automatically detect when couriers enter Migros stores (100m radius)
- ✅ **Distance Calculation** - Calculate total travel distance for each courier
- ✅ **Cooldown Management** - Prevent duplicate store entrance logging (1-minute cooldown)

### Technical Features

- 🔐 **API Key Authentication** - Secure endpoints with custom authentication filter
- ⚡ **Performance Optimization** - Database indexing and incremental distance caching
- 🎯 **Strategy Pattern** - Configurable distance calculation algorithms (Euclidean/Haversine)
- 🗄️ **H2 Database** - In-memory database for development and testing

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.3.4
- **Language:** Java 21
- **Database:** H2 (in-memory)
- **ORM:** Spring Data JPA / Hibernate
- **Testing:** JUnit 5, Mockito, Spring Test
- **Build Tool:** Maven

## 🏗️ Architecture & Design Patterns

### Primary Design Patterns

#### 1. **Strategy Pattern** 🎯

Configurable distance calculation algorithms:

```yaml
courier-tracking:
  distance:
    calculation-algorithm: euclidean # or haversine
```

#### 2. **Observer Pattern** 🔍

Location updates trigger multiple business reactions:

```java
// When location updates, multiple services "observe" and react
distanceCalculationService.updateDistanceForNewLocation(courier, location);
storeEntranceService.checkAndLogStoreEntrance(courier, location);
```

#### 3. **Chain of Responsibility** 🔗

Security filter chain for API authentication:

```java
ApiKeyAuthFilter → Spring Security Filter Chain → Controller
```

### Additional Design Patterns

- **Dependency Injection** - Constructor-based dependency injection implemented throughout the application using Spring's IoC container:
  It is achieved through @RequiredArgsConstructor lombok annotation
- **Singleton Pattern** - Some spring-managed beans ensuring single instance like: @Service, @Component, @Repository etc.

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+

### Installation & Setup

1. **Clone the repository**

```bash
git clone https://github.com/gokhanaker/courier-tracking-service
cd courier-tracking-service
```

2. **Build the project**

```bash
mvn clean compile
```

3. **Run the application**

```bash
mvn spring-boot:run
```

4. **Access the application**

- **API Base URL:** `http://localhost:8080/api`
- **H2 Console:** `http://localhost:8080/api/h2-console`
- **Health Check:** `http://localhost:8080/api/actuator/health`

### H2 Database Connection

- **JDBC URL:** `jdbc:h2:mem:courier_tracking_db`
- **Username:** `admin`
- **Password:** `password`

## API Documentation

### Authentication

All API endpoints require authentication using an API key:

```bash
X-API-Key: CT-SECURE-API-KEY-12345
```

### Endpoints

#### 1. Create Courier

```http
POST /api/couriers
Content-Type: application/json
X-API-Key: CT-SECURE-API-KEY-12345

{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+90 555 123 4567"
}
```

**Response:**

```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+90 555 123 4567"
}
```

#### 2. Get Courier Details

```http
GET /api/couriers/{courierId}
X-API-Key: CT-SECURE-API-KEY-12345
```

#### 3. Submit Location Update

```http
POST /api/locations
Content-Type: application/json
X-API-Key: CT-SECURE-API-KEY-12345

{
  "courierId": "123e4567-e89b-12d3-a456-426614174000",
  "latitude": 40.9923307,
  "longitude": 29.1244229,
  "timestamp": "2025-09-21T10:00:00"
}
```

**Response:**

```json
{
  "message": "Location updated successfully for courier: 123e4567-e89b-12d3-a456-426614174000",
  "storeEntrance": "Courier entered: Ataşehir MMM Migros"
}
```

#### 4. Get Total Travel Distance

```http
GET /api/couriers/{courierId}/total-travel-distance
X-API-Key: CT-SECURE-API-KEY-12345
```

**Response:**

```json
{
  "distance": 15.75,
  "unit": "km"
}
```

### Store Locations

The system monitors 5 Migros stores in Istanbul:

- **Ataşehir MMM Migros** - (40.9923307, 29.1244229)
- **Novada MMM Migros** - (40.986106, 29.1161293)
- **Beylikdüzü 5M Migros** - (41.0066851, 28.6552262)
- **Ortaköy MMM Migros** - (41.055783, 29.0210292)
- **Caddebostan MMM Migros** - (40.9632463, 29.0630908)

## 🗄️ Database Schema

### Core Tables

- **`couriers`** - Courier master data (name, email, phone)
- **`locations`** - GPS tracking data with timestamps
- **`stores`** - Migros store locations and details
- **`store_entrances`** - Records of store visits with cooldown management
- **`courier_distances`** - Total distances by couriers

## Testing

### Run All Tests

```bash
mvn test
```

### Postman Collection

Import `Courier_Tracking_Service_API.postman_collection.json` for:

- **Complete API Testing** - All endpoints with authentication
- **Test Scenarios** - End-to-end workflow validation

## Production Considerations

While this is a case study implementation, the following production-ready features were considered during design:

- **Data Cleanup Scheduler** - Periodic cleanup of historical location data to prevent unlimited table growth
- **Caching Layers** - Redis for frequently accessed data
- **Connection Pooling** - Database connection management
