# Courier Tracking Service

A RESTful web service for tracking courier locations and monitoring store visits, built with **Java + Spring Boot** with a maintainable architecture.

## ✨ Features

### Core Functionality

- ✅ **Location Tracking** - Submit and store courier GPS coordinates with timestamps
- ✅ **Store Entrance Detection** - Automatically detect when couriers enter Migros stores (100m radius)
- ✅ **Distance Calculation** - Calculate total travel distance for each courier
- ✅ **Cooldown Management** - Prevent duplicate store entrance logging (1-minute cooldown)
- ✅ **Redis Caching** - Cache total travel distance per courier for faster reads

### Technical Features

- 🔐 **API Key Authentication** - Secure endpoints with custom authentication filter
- ⚡ **Performance Optimization** - Database indexing and incremental distance calculation
- 🎯 **Strategy Pattern** - Configurable distance calculation algorithms (Euclidean/Haversine)
- 🗄️ **H2 Database** - In-memory database for development and testing

## 🛠️ Technology Stack

- **Framework:** Spring Boot 3.3.4
- **Language:** Java 21
- **Database:** H2 (in-memory)
- **ORM:** Spring Data JPA / Hibernate
- **Cache:** Redis (via Spring Data Redis)
- **Testing:** JUnit 5, Mockito, Spring Test
- **Build Tool:** Maven

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- Redis 7+

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

3. **Run Redis locally first**

macOS with Docker:

```bash
docker run -p 6379:6379 --name redis -d redis:7-alpine
```

macOS with Homebrew:

```bash
brew install redis
brew services start redis
```

4. **Run the application**

```bash
mvn spring-boot:run
```

5. **Access the application**

- **API Base URL:** `http://localhost:8080/api`
- **H2 Console:** `http://localhost:8080/api/h2-console`
- **Health Check:** `http://localhost:8080/api/actuator/health`

### H2 Database Connection

- **JDBC URL:** `jdbc:h2:mem:courier_tracking_db`
- **Username:** `admin`
- **Password:** `password`

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
