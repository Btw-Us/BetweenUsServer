# BetweenUsServer
[![wakatime](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs.svg)](https://wakatime.com/badge/github/aiyu-ayaan/BetweenUs)

> *A humble server, fueling fragments of a quiet dream.*

⚠️ **Warning:** This project is currently under active development. Features may be incomplete or unstable.

---

## About

**BetweenUsServer** is the backend powering the chat application. It uses:

* **MySQL** – for structured relational data such as user accounts, authentication, and metadata.
* **MongoDB** – for flexible storage of unstructured or semi-structured data like chat messages and message threads.

---

## `.env` Setup

To get started, configure your environment variables:

1. Create a `.env` file in the **root directory** of the project.
2. Add the following environment variables:

```dotenv
# MySQL Configuration
DATABASE_URL=         # Your MySQL JDBC connection string
MYSQL_USER_NAME=      # Your MySQL username
MYSQL_PASSWORD=       # Your MySQL password
DATABASE_NAME=       # Your MySQL database name
MANGO_DB_USER_NAME=     # Your MongoDB username
MANGO_DB_PASSWORD=      # Your MongoDB password
MANGO_DB_URL =          # Your MongoDB connection string
```