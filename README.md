## Database Setup (Local Development)

Each developer runs their own local MySQL instance.

Before running the application, define the following environment variables:

DB_URL=jdbc:mysql://localhost:3306/<your_database_name>
DB_USER=<your_mysql_user>
DB_PASS=<your_mysql_password>

Spring Boot will automatically create the required tables.
