-- One Postgres instance, one database per service (each service owns its schema via Flyway).
CREATE DATABASE userdb;
CREATE DATABASE postdb;
CREATE DATABASE commentdb;
CREATE DATABASE notificationdb;
