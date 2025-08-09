# BetweenUsServer
![Code](https://tokei.rs/b1/github/Btw-Us/BetweenUsServer?category=code)
[![wakatime](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs.svg)](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs)

> *A humble server, fueling fragments of a quiet dream.*


**BetweenUsServer** is the backend powering the chat application. It is built with a modern technology stack to ensure scalability, reliability, and maintainability.


⚠️ **Warning:** This project is currently under active development. Features may be incomplete or unstable.

---

## Technology Stack

*   **Framework**: [Ktor](https://ktor.io/) - A modern and asynchronous web framework for Kotlin.
*   **Databases**:
    *   **MySQL**: Used for storing structured, relational data such as user accounts, authentication details, and other metadata.
    *   **MongoDB**: Employed for flexible, unstructured data like chat messages and threads, allowing for scalability and performance.
*   **Dependency Injection**: [Dagger](https://dagger.dev/) - A compile-time dependency injection framework for building robust and maintainable applications.
*   **SQL Library**: [Exposed](https://github.com/JetBrains/Exposed) - A lightweight SQL library for Kotlin, providing a type-safe and expressive way to interact with databases.
*   **Environment Variables**: [Dotenv](https://github.com/cdimascio/dotenv-kotlin) - A library for loading environment variables from a `.env` file, ensuring secure and flexible configuration.
*   **Authentication**:
    *   **JWT (JSON Web Tokens)**: For secure and stateless authentication.
    *   **OAuth**: Integrated with Google for social login capabilities.
*   **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) - For converting Kotlin objects to and from JSON.
*   **Logging**: [Logback](http://logback.qos.ch/) - A robust and flexible logging framework.

---

## Features

*   **User Authentication**: Secure user login and registration with JWT and Google OAuth.
*   **User Profile Management**: Users can set up and manage their profiles.
*   **Real-time Chat**: Personal chat functionality with support for sending and receiving messages.
*   **Chat Management**: Create, retrieve, update, and delete chat conversations.
*   **Message Management**: Send, retrieve, update, and delete individual messages within a chat.
*   **Health Checks**: Endpoints for monitoring the server's health and authentication status.

---

## API Endpoints

### Authentication

*   `POST /auth/token`: Generate a new authentication token.
*   `POST /auth/login/google`: Log in or register a user using a Google account.
*   `POST /auth/login/setup`: Set up a user's profile after registration.
*   `GET /auth/login/oauth`: Initiate OAuth login with Google.
*   `GET /auth/login/callback`: Handle the OAuth callback from Google.

### Chat

*   `POST /chat/personal`: Create a new personal chat.
*   `GET /chat/personal`: Get all personal chats for the authenticated user.
*   `GET /chat/personal/{chatId}`: Get a specific personal chat by its ID.
*   `PUT /chat/personal/{chatId}`: Update a personal chat.
*   `DELETE /chat/personal/{chatId}`: Delete a personal chat.
*   `GET /chat/personal/{chatId}/messages`: Get all messages for a specific chat.
*   `POST /chat/personal/{chatId}/messages`: Add a new message to a chat.
*   `PUT /chat/personal/{chatId}/messages/{messageId}`: Update a message in a chat.
*   `DELETE /chat/personal/{chatId}/messages/{messageId}`: Delete a message from a chat.

### Health Check

*   `GET /health`: Check the health of the server.

---

## Database Schema

### MySQL

The MySQL database stores relational data, including:

*   **User**: User account information (e.g., `username`, `email`, `profilePicture`).
*   **UserPassword**: Hashed passwords for users.
*   **UserPrivacySettings**: User-specific privacy settings.
*   **UserStatus**: The online/offline status of users.
*   **UserDevices**: Information about devices logged in by the user.
*   **Friends**: Friendship relationships between users.
*   **AuthToken**: Authentication tokens for users.

### MongoDB

The MongoDB database is used for chat-related data:

*   **PersonalChat**: Represents a personal chat conversation between two users.
*   **ChatEntry**: Represents a single message within a chat.

---


## Directory Structure

```
D:\Test\BetweenUsServer\
├───.git
├───.kotlin
│   └───errors
├───gradle
│   └───wrapper
└───src
    └───main
        ├───kotlin
        │   ├───config
        │   │   ├───api_config
        │   │   └───response
        │   ├───dagger
        │   │   ├───components
        │   │   └───modules
        │   ├───database
        │   │   ├───mangodb
        │   │   │   ├───model
        │   │   │   └───repository
        │   │   │       └───impl
        │   │   └───mysql
        │   │       ├───config
        │   │       ├───mapper
        │   │       ├───model
        │   │       │   └───entity
        │   │       ├───repository
        │   │       │   ├───auth_token
        │   │       │   │   └───imp
        │   │       │   └───user
        │   │       │       └───impl
        │   │       └───services
        │   ├───plugin
        │   ├───routes
        │   └───utils
        └───resources
```

---

## Configuration

### Environment Variables Setup

⚠️ **Important**: Configuration files are not included in the repository for security reasons.

To get started, configure your environment variables:

1. Create a `.env` file in the **root directory** of the project.
2. Add the following environment variables:

```dotenv
# MySQL Configuration
DATABASE_URL=jdbc:mysql://localhost:3306/your_database_name
MYSQL_USER_NAME=your_mysql_username
MYSQL_PASSWORD=your_mysql_password
DATABASE_NAME=your_database_name

# MongoDB Configuration
MANGO_DB_USER_NAME=your_mongodb_username
MANGO_DB_PASSWORD=your_mongodb_password
MANGO_DB_URL=localhost:27017

# Server Configuration
SERVER_PORT=8080
SERVER_HOST=localhost

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_REALM=your_jwt_realm
JWT_EXPIRATION=3600

# Google OAuth Configuration
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### Docker Compose Configuration

Create a `docker-compose.yml` file in the **root directory** of the project:

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
      mongodb:
        condition: service_healthy
    environment:
      # Database Configuration
      - DATABASE_URL=jdbc:mysql://mysql:3306/your_database_name
      - MYSQL_USER_NAME=your_mysql_username
      - MYSQL_PASSWORD=your_mysql_password
      - DATABASE_NAME=your_database_name
      - MANGO_DB_USER_NAME=your_mongodb_username
      - MANGO_DB_PASSWORD=your_mongodb_password
      - MANGO_DB_URL=mongodb:27017

      # Server Configuration
      - SERVER_PORT=8080
      - SERVER_HOST=0.0.0.0

      # JWT Configuration
      - JWT_SECRET=your_jwt_secret_key_here
      - JWT_REALM=your_jwt_realm
      - JWT_EXPIRATION=3600

      # Google OAuth
      - GOOGLE_CLIENT_ID=your_google_client_id
      - GOOGLE_CLIENT_SECRET=your_google_client_secret
    networks:
      - app-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD=your_mysql_password
      MYSQL_DATABASE=your_database_name
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-scripts/mysql:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-pyour_mysql_password"]
      timeout: 20s
      retries: 10
    networks:
      - app-network

  mongodb:
    image: mongo:7.0
    environment:
      MONGO_INITDB_ROOT_USERNAME=your_mongodb_username
      MONGO_INITDB_ROOT_PASSWORD=your_mongodb_password
      MONGO_INITDB_DATABASE=your_database_name
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
      - ./init-scripts/mongo:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      timeout: 20s
      retries: 10
    networks:
      - app-network

volumes:
  mysql_data:
  mongodb_data:

networks:
  app-network:
    driver: bridge
```

---

## Running the Application

### Prerequisites

- Java 17 or higher
- Gradle
- Docker and Docker Compose (for Docker setup)

### Option 1: Local Development

1. **Clone the repository**:
   ```bash
   git clone https://github.com/aiyu-ayaan/BetweenUsServer.git
   cd BetweenUsServer
   ```

2. **Set up the environment variables**:
   Create a `.env` file in the root directory and add the required variables as described in the [Environment Variables Setup](#environment-variables-setup) section.

3. **Set up databases locally** (MySQL and MongoDB must be running on your machine):
    - MySQL on `localhost:3306`
    - MongoDB on `localhost:27017`

4. **Build the application**:
   ```bash
   ./gradlew build
   ```

5. **Run the application**:
   ```bash
   ./gradlew run
   ```

The server will start on `http://localhost:8080`.

### Option 2: Docker (Recommended)

This is the recommended way to run the application as it sets up all dependencies automatically.

1. **Clone the repository**:
   ```bash
   git clone https://github.com/aiyu-ayaan/BetweenUsServer.git
   cd BetweenUsServer
   ```

2. **Create the Docker Compose configuration**:
   Create a `docker-compose.yml` file as described in the [Docker Compose Configuration](#docker-compose-configuration) section.

3. **Build and run with Docker Compose**:
   ```bash
   docker-compose up --build
   ```

   This command will:
    - Build the application Docker image
    - Start MySQL and MongoDB containers
    - Wait for databases to be healthy
    - Start the application container
    - Set up networking between containers

4. **Access the application**:
   The server will be available at `http://localhost:8080`

### Option 3: Docker (Manual)

You can also run the application using Docker manually:

1. **Build the Docker image**:
   ```bash
   docker build -t between-us-server .
   ```

2. **Run the Docker container**:
   ```bash
   docker run --env-file .env -p 8080:8080 between-us-server
   ```

   **Note**: This method requires you to have MySQL and MongoDB running separately and accessible from the Docker container.

---

## Docker Configuration Details

### Multi-stage Build

The Dockerfile uses a multi-stage build to create an optimized Docker image:

- **Stage 1: Build**
    - Uses `openjdk:17-jdk-slim` image to build the application
    - Copies source code and Gradle files
    - Runs `./gradlew installDist` to build and create distribution

- **Stage 2: Final Image**
    - Uses `openjdk:17-jre-slim` image for runtime
    - Copies built application from build stage
    - Exposes port 8080
    - Runs application using Gradle-generated start script

### Health Checks

The Docker Compose configuration includes health checks for both databases:
- **MySQL**: Uses `mysqladmin ping` to verify database connectivity
- **MongoDB**: Uses `mongosh` to execute a ping command

The application container waits for both databases to be healthy before starting.

---

## Security Note

⚠️ **Important Security Information**:

- The configuration files (`.env`, `docker-compose.yml`) contain sensitive information and are excluded from the repository
- Always use strong, unique passwords in production environments
- The JWT secret should be generated securely and kept confidential
- Google OAuth credentials should be obtained from your own Google Cloud Console project
- Never commit configuration files containing credentials to version control

---

## Troubleshooting

### Common Issues

1. **Database Connection Failed**: Ensure MySQL and MongoDB are running and accessible
2. **Port Already in Use**: Change the `SERVER_PORT` in your configuration
3. **Docker Permission Issues**: Run Docker commands with appropriate permissions
4. **Health Check Failures**: Wait longer for databases to initialize, especially on first run

### Logs

To view application logs when running with Docker Compose:
```bash
docker-compose logs -f app
```

To view database logs:
```bash
docker-compose logs -f mysql
docker-compose logs -f mongodb
```