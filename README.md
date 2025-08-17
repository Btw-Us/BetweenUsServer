# BetweenUsServer

![Code](https://tokei.rs/b1/github/Btw-Us/BetweenUsServer?category=code)
[![wakatime](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs.svg)](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs)

> *A humble server, fueling fragments of a quiet dream.*

**BetweenUsServer** is the backend powering the chat application. It is built with a modern technology stack to ensure scalability, reliability, and maintainability.

⚠️ **Warning:** This project is currently under active development. Features may be incomplete or unstable.

---

## Technology Stack

* **Framework**: [Ktor](https://ktor.io/) – Modern asynchronous Kotlin framework
* **Databases**:

    * **MySQL** – Structured relational data (users, auth, metadata)
    * **MongoDB Replica Set** – Flexible, unstructured chat data with HA & scalability
* **Dependency Injection**: [Dagger](https://dagger.dev/)
* **SQL Library**: [Exposed](https://github.com/JetBrains/Exposed)
* **Serialization**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
* **Authentication**:

    * **JWT** – Secure, stateless auth
    * **Google OAuth** – Social login
* **Environment Variables**: [Dotenv](https://github.com/cdimascio/dotenv-kotlin)
* **Logging**: [Logback](http://logback.qos.ch/)

---

## Directory Structure

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

---

## Configuration

### 1. Environment Variables (`.env`)

Sensitive configs are stored here (ignored by git):

```dotenv
SERVER_PORT=8080
SERVER_HOST=0.0.0.0

# --- MySQL ---
MYSQL_USER_NAME=your_mysql_username
MYSQL_PASSWORD=your_mysql_password
DATABASE_NAME=your_database_name

# --- MongoDB Replica Set ---
MANGO_DB_URL=your_mongodb_url
MANGO_DB_USER_NAME=your_mongodb_username
MANGO_DB_PASSWORD=your_mongodb_password
MONGO_DB_REPLICA_SET=your_mongo_replica_set_name

# --- JWT ---
JWT_SECRET=your_super_secret_jwt_key_that_is_very_long
JWT_REALM=Access to all routes '/user'
JWT_EXPIRATION=3600

# --- Google OAuth ---
GOOGLE_CLIENT_ID=your_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your_google_client_secret
```

---

### 2. MongoDB Replica Initialization Script

`scripts/init-replica-set.sh`

```bash
#!/bin/bash
set -e
echo "Starting replica set initialization"
until mongosh --host mongo1 --eval "print(\"waited for connection\")"; do sleep 2; done

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

sleep 20
mongosh --host mongo1 <<EOF
db.getSiblingDB('admin').createUser({
  user: '${MONGO_INITDB_ROOT_USERNAME}',
  pwd: '${MONGO_INITDB_ROOT_PASSWORD}',
  roles: [{ role: 'root', db: 'admin' }]
})
EOF
```

Make it executable:

```bash
chmod +x scripts/init-replica-set.sh
```

---

### 3. Docker Compose (`docker-compose.yml`)

Defines and orchestrates all services:

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
      mongo1:
        condition: service_healthy
      mongo2:
        condition: service_healthy
      mongo3:
        condition: service_healthy
      mongo-setup:
        condition: service_completed_successfully
    environment:
      - DATABASE_URL=jdbc:mysql://mysql:3306/${MYSQL_DATABASE}
      - MYSQL_USER_NAME=${MYSQL_USER}
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
      - DATABASE_NAME=${MYSQL_DATABASE}
      - MANGO_DB_USER_NAME=${MONGO_USER}
      - MANGO_DB_PASSWORD=${MONGO_PASSWORD}
      - SERVER_PORT=8080
      - SERVER_HOST=0.0.0.0
      - JWT_SECRET=${JWT_SECRET}
      - JWT_REALM=${JWT_REALM}
      - JWT_EXPIRATION=${JWT_EXPIRATION}
      - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
      - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
    networks:
      - between-us-network

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./init-scripts/mysql:/docker-entrypoint-initdb.d
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_PASSWORD}" ]
      timeout: 20s
      retries: 10
    networks:
      - between-us-network

  # phpMyAdmin
  phpmyadmin:
    image: phpmyadmin:latest
    container_name: between_us_phpmyadmin
    restart: unless-stopped
    ports:
      - "8081:80"
    environment:
      - PMA_HOST=mysql
      - PMA_PORT=3306
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - between-us-network

  mongo1:
    image: mongo:7.0
    container_name: between_us_mongo_1
    restart: unless-stopped
    ports:
      - "27017:27017"
    command: mongod --replSet myReplicaSet --bind_ip_all
    volumes:
      - mongodb_data:/data/db
      - ./init-scripts/mongo:/docker-entrypoint-initdb.d
    healthcheck:
      test: [ "CMD", "mongosh", "--eval", "db.adminCommand('ping')" ]
      timeout: 20s
      retries: 10
    networks:
      - between-us-network

  mongo2:
    image: mongo:7.0
    container_name: between_us_mongo_2
    restart: unless-stopped
    ports:
      - "27018:27017"
    command: mongod --replSet myReplicaSet --bind_ip_all
    volumes:
      - mongo2_data:/data/db
    networks:
      - between-us-network
    healthcheck:
      test: [ "CMD", "mongosh", "--eval", "db.adminCommand('ping')" ]
      timeout: 20s
      retries: 10

  mongo3:
    image: mongo:7.0
    container_name: between_us_mongo_3
    restart: unless-stopped
    ports:
      - "27019:27017"
    command: mongod --replSet myReplicaSet --bind_ip_all
    volumes:
      - mongo3_data:/data/db
    networks:
      - between-us-network
    healthcheck:
      test: [ "CMD", "mongosh", "--eval", "db.adminCommand('ping')" ]
      timeout: 20s
      retries: 10

  # Mongo Express
  mongo-express:
    image: mongo-express:latest
    container_name: between_us_mongo_express
    restart: unless-stopped
    ports:
      - "8082:8081"
    environment:
      - ME_CONFIG_MONGODB_URL=mongodb://mongo1:27017,mongo2:27017,mongo3:27017/?replicaSet=myReplicaSet
      - ME_CONFIG_BASICAUTH_USERNAME=${ME_ADMIN_USER}
      - ME_CONFIG_BASICAUTH_PASSWORD=${ME_ADMIN_PASSWORD}
      - ME_CONFIG_MONGODB_ENABLE_ADMIN=true
    depends_on:
      mongo1:
        condition: service_healthy
      mongo-setup:
        condition: service_completed_successfully
    networks:
      - between-us-network

  mongo-setup:
    image: mongo:7.0
    container_name: mongo_setup
    restart: "no"
    depends_on:
      mongo1:
        condition: service_healthy
      mongo2:
        condition: service_healthy
      mongo3:
        condition: service_healthy
    volumes:
      - ./scripts:/scripts
    command: /scripts/init-replica-set.sh
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

---

## Service Details

| Service         | Port  | Description                      |
| --------------- | ----- | -------------------------------- |
| `app`           | 8080  | Main BetweenUsServer application |
| `mysql`         | 3306  | MySQL database server            |
| `phpmyadmin`    | 8081  | Web interface for MySQL          |
| `mongo1`        | 27017 | MongoDB primary replica          |
| `mongo2`        | 27018 | MongoDB secondary replica        |
| `mongo3`        | 27019 | MongoDB secondary replica        |
| `mongo-express` | 8082  | Web interface for MongoDB        |

---

## Running the Application

1. **Prerequisites**
   Install Docker & Docker Compose

2. **Clone Repo**

   ```bash
   git clone https://github.com/aiyu-ayaan/BetweenUsServer.git
   cd BetweenUsServer
   ```

3. **Create Configs**

    * `.env` file
    * `init-replica-set.sh` script

4. **Run with Docker**

   ```bash
   docker-compose up --build
   ```

5. **Access Services**

    * Main App → `http://localhost:8080`
    * MySQL (phpMyAdmin) → `http://localhost:8081`
    * MongoDB (Mongo Express) → `http://localhost:8082`

---

## Development Commands

```bash
docker-compose up -d               # Start in detached mode
docker-compose logs app            # Logs for app
docker-compose down                # Stop services
docker-compose down -v             # Stop & delete volumes (⚠️ data loss)
docker-compose up --build app      # Rebuild specific service
```

---
