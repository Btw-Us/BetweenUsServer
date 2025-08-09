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


## `.env` Setup

To get started, configure your environment variables:

1.  Create a `.env` file in the **root directory** of the project.
2.  Add the following environment variables:

```dotenv
# MySQL Configuration
DATABASE_URL=         # Your MySQL JDBC connection string
MYSQL_USER_NAME=      # Your MySQL username
MYSQL_PASSWORD=       # Your MySQL password
DATABASE_NAME=       # Your MySQL database name

# MongoDB Configuration
MANGO_DB_USER_NAME=     # Your MongoDB username
MANGO_DB_PASSWORD=      # Your MongoDB password
MANGO_DB_URL =          # Your MongoDB connection string

# JWT Configuration
JWT_SECRET=           # Your JWT secret key
JWT_ISSUER=           # Your JWT issuer
JWT_AUDIENCE=         # Your JWT audience
JWT_REALM=            # Your JWT realm

# Google OAuth Configuration
GOOGLE_CLIENT_ID=     # Your Google OAuth client ID
GOOGLE_CLIENT_SECRET= # Your Google OAuth client secret
```

---

## Running the Application

To run the application, you will need to have Java and Gradle installed.

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/aiyu-ayaan/BetweenUsServer.git
    ```
2.  **Set up the environment variables**:
    Create a `.env` file in the root directory and add the required variables as described in the [`.env` Setup](#env-setup) section.
3.  **Build the application**:
    ```bash
    ./gradlew build
    ```
4.  **Run the application**:
    ```bash
    ./gradlew run
    ```

The server will start on the configured port.


