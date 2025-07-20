# BetweenUsServer
[![wakatime](https://wakatime.com/badge/user/3a4240f0-6bea-4626-be2a-1129790e4336/project/20c35e40-c5c6-47d6-8a78-bed434780be2.svg)](https://wakatime.com/badge/user/3a4240f0-6bea-4626-be2a-1129790e4336/project/20c35e40-c5c6-47d6-8a78-bed434780be2)


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
MYSQL_DATABASE=       # Your MySQL database name

```