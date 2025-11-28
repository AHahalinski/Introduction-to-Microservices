# Microservices System - Resource & Song Services

A microservices system consisting of two services for managing MP3 files and their metadata.

## Architecture

- **Resource Service** - Handles MP3 file storage and processing
- **Song Service** - Manages song metadata with validation

## Technology Stack

- **Spring Boot**: 3.4.0
- **Java**: 17
- **Build Tool**: Maven
- **Database**: PostgreSQL 16+
- **Libraries**: 
  - Apache Tika (MP3 metadata extraction)
  - Spring Data JPA
  - Spring Validation
  - Lombok

## Project Structure

```
introduction-to-microservices/
├── resource-service/
│   ├── src/main/java/com/epam/microservices/resourceservice/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions and global handler
│   │   ├── repository/      # Spring Data repositories
│   │   ├── service/         # Business logic
│   │   └── ResourceServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── song-service/
│   ├── src/main/java/com/epam/microservices/songservice/
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── entity/          # JPA entities
│   │   ├── exception/       # Custom exceptions and global handler
│   │   ├── mapper/          # Entity-DTO mappers
│   │   ├── repository/      # Spring Data repositories
│   │   ├── service/         # Business logic
│   │   ├── validation/      # Custom validators
│   │   └── SongServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── compose.yaml             # Docker Compose for databases
├── .gitignore
└── README.md
```

## Prerequisites

- Java 17 or later (LTS version)
- Maven 3.6+
- Docker and Docker Compose

## Setup Instructions

### 1. Start PostgreSQL Databases

```bash
docker-compose -f compose.yaml up -d
```

This will start:
- `resource-db` on port **5434**
- `song-db` on port **5435**

### 2. Build and Run Resource Service

```bash
cd resource-service
mvn clean install
mvn spring-boot:run
```

Resource Service will start on port **8082**

### 3. Build and Run Song Service

```bash
cd song-service
mvn clean install
mvn spring-boot:run
```

Song Service will start on port **8083**
