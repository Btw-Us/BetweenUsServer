# BetweenUsServer

![Code](https://tokei.rs/b1/github/Btw-Us/BetweenUsServer?category=code)
[![wakatime](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs.svg)](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs)

> *A humble server, fueling fragments of a quiet dream.*

**BetweenUsServer** is the backend powering the chat application. It is built with a modern technology stack to ensure scalability, reliability, and maintainability.

⚠️ **Warning:** This project is currently under active development. Features may be incomplete or unstable.

-----

## Technology Stack

* **Framework**: [Ktor](https://ktor.io/) - A modern and asynchronous web framework for Kotlin.
* **Databases**:
    * **MySQL**: Used for storing structured, relational data such as user accounts, authentication details, and other metadata.
    * **MongoDB**: Employed as a replica set for flexible, unstructured data like chat messages and threads, ensuring high availability and scalability.
* **Dependency Injection**: [Dagger](https://dagger.dev/) - A compile-time dependency injection framework.
* **SQL Library**: [Exposed](https://github.com/JetBrains/Exposed) - A lightweight SQL library for Kotlin.
* **Environment Variables**: [Dotenv](https://github.com/cdimascio/dotenv-kotlin) - A library for loading environment variables.
* **Authentication**:
    * **JWT (JSON Web Tokens)**: For secure and stateless authentication.
    * **OAuth**: Integrated with Google for social login capabilities.
* **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization) - For converting Kotlin objects to and from JSON.
* **Logging**: [Logback](http://logback.qos.ch/) - A robust and flexible logging framework.

-----

## Directory Structure

A brief overview of the project's structure.

```
.
├── scripts
│   └── init-replica-set.sh
├── src
│   └── main
│       ├── kotlin
│       └── resources
├── .env
├── build.gradle.kts
└── docker-compose.yml
```

-----

## Configuration

Setting up the server requires creating a few configuration files that are not included in the repository for security reasons.

### 1\. Environment Variables (`.env` file)

⚠️ **Important**: This file contains sensitive data. It is listed in `.gitignore` and should **never** be committed to version control.

Create a file named `.env` in the project's root directory and populate it with your credentials.

```dotenv
# --- Server Configuration ---
SERVER_PORT=8080
SERVER_HOST=0.0.0.0

# --- MySQL Database ---
MYSQL_USER_NAME=your_mysql_username
MYSQL_PASSWORD=your_mysql_password
DATABASE_NAME=your_database_name

# --- MongoDB Replica Set ---
MANGO_DB_URL=your_mongodb_url
MANGO_DB_USER_NAME=your_mongodb_username
MANGO_DB_PASSWORD=your_mongodb_password
MONGO_DB_REPLICA_SET=your_mongo_replica_set_name


# --- JWT Authentication ---
JWT_SECRET=your_super_secret_jwt_key_that_is_very_long
JWT_REALM=Access to all routes '/user'
JWT_EXPIRATION=3600

# --- Google OAuth ---
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

### 2\. MongoDB Initialization Script

The `docker-compose` setup uses a script to initialize the MongoDB replica set.

1.  Create the directory: `mkdir -p scripts`
2.  Create a file at `scripts/init-replica-set.sh` and add the following content. This script will use the environment variables from the `.env` file to configure the database.

<!-- end list -->

```bash
#!/bin/bash
set -e

echo "Starting replica set initialization"
until mongosh --host mongo1 --eval "print(\"waited for connection\")"
do
    sleep 2
done

echo "Connection finished"
echo "Creating replica set"
mongosh --host mongo1 <<EOF
rs.initiate({
  _id: "${MONGO_REPLICA_SET_NAME}",
  members: [
    { _id: 0, host: "mongo1:27017" },
    { _id: 1, host: "mongo2:27017" },
    { _id: 2, host: "mongo3:27017" }
  ]
})
EOF

echo "Replica set created"
echo "Waiting for all nodes to be healthy..."
sleep 20

echo "Creating user..."
mongosh --host mongo1 <<EOF
db.getSiblingDB('admin').createUser({
  user: '${MONGO_INITDB_ROOT_USERNAME}',
  pwd: '${MONGO_INITDB_ROOT_PASSWORD}',
  roles: [{ role: 'root', db: 'admin' }]
})
EOF
echo "User created"
echo "Initialization complete"
```

3.  Make the script executable:
    ```bash
    chmod +x scripts/init-replica-set.sh
    ```

### 3\. Docker Compose Configuration

Create a `docker-compose.yml` file in the root directory. This file defines and orchestrates all the services, pulling sensitive values from the `.env` file.

```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "${SERVER_PORT:-8080}:8080"
    depends_on:
      mysql:
        condition: service_healthy
      mongo-setup:
        condition: service_completed_successfully
    env_file:
      - .env
    environment:
      - DATABASE_URL=jdbc:mysql://mysql:3306/${MYSQL_DATABASE}
      - MYSQL_USER_NAME=${MYSQL_USER}
      - MANGO_DB_USER_NAME=${MONGO_INITDB_ROOT_USERNAME}
      - MANGO_DB_PASSWORD=${MONGO_INITDB_ROOT_PASSWORD}
    networks:
      - between-us-network

  mysql:
    image: mysql:8.0
    env_file:
      - .env
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "${MYSQL_USER}", "-p${MYSQL_PASSWORD}"]
      timeout: 20s
      retries: 10
    networks:
      - between-us-network

  mongo1:
    image: mongo:7.0
    container_name: between_us_mongo_1
    command: mongod --replSet ${MONGO_REPLICA_SET_NAME} --bind_ip_all
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      timeout: 20s
      retries: 10
    networks:
      - between-us-network

  mongo2:
    image: mongo:7.0
    container_name: between_us_mongo_2
    command: mongod --replSet ${MONGO_REPLICA_SET_NAME} --bind_ip_all
    ports:
      - "27018:27017"
    volumes:
      - mongo2_data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      timeout: 20s
      retries: 10
    networks:
      - between-us-network

  mongo3:
    image: mongo:7.0
    container_name: between_us_mongo_3
    command: mongod --replSet ${MONGO_REPLICA_SET_NAME} --bind_ip_all
    ports:
      - "27019:27017"
    volumes:
      - mongo3_data:/data/db
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      timeout: 20s
      retries: 10
    networks:
      - between-us-network

  mongo-setup:
    image: mongo:7.0
    container_name: mongo_setup
    restart: "no"
    env_file:
      - .env
    depends_on:
      mongo1:
        condition: service_healthy
      mongo2:
        condition: service_healthy
      mongo3:
        condition: service_healthy
    volumes:
      - ./scripts:/scripts
    entrypoint: ["bash", "/scripts/init-replica-set.sh"]
    networks:
      - between-us-network

volumes:
  mysql_data:
  mongodb_data:
  mongo2_data:
  mongo3_data:

networks:
  between-us-network:
    driver: bridge
```

-----

## Running the Application

This is the recommended way to run the application, as Docker handles the entire environment setup.

1.  **Prerequisites**:

    * Docker and Docker Compose

2.  **Clone the repository**:

    ```bash
    git clone https://github.com/aiyu-ayaan/BetweenUsServer.git
    cd BetweenUsServer
    ```

3.  **Create configurations**:

    * Create the `.env` file as described in [Step 1](https://www.google.com/search?q=%231-environment-variables-env-file).
    * Create the `init-replica-set.sh` script as described in [Step 2](https://www.google.com/search?q=%232-mongodb-initialization-script).

4.  **Build and run with Docker Compose**:

    ```bash
    docker-compose up --build
    ```

5.  **Access the application**:
    The server will be available at `http://localhost:8080`.