#!/bin/bash
# Script d'initialisation MySQL - Création des bases de données et de l'utilisateur applicatif

set -e

echo ">>> Initialisation des bases de données RestoBook..."

mysql -u root -p"${MYSQL_ROOT_PASSWORD}" <<-EOSQL
  -- Création des bases de données
  CREATE DATABASE IF NOT EXISTS auth_db       CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  CREATE DATABASE IF NOT EXISTS booking_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  CREATE DATABASE IF NOT EXISTS restaurant_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  CREATE DATABASE IF NOT EXISTS review_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

  -- Création de l'utilisateur applicatif (si non existant)
  CREATE USER IF NOT EXISTS '${MYSQL_APP_USER}'@'%' IDENTIFIED BY '${MYSQL_APP_PASSWORD}';

  -- Attribution des privilèges par base
  GRANT ALL PRIVILEGES ON auth_db.*       TO '${MYSQL_APP_USER}'@'%';
  GRANT ALL PRIVILEGES ON booking_db.*    TO '${MYSQL_APP_USER}'@'%';
  GRANT ALL PRIVILEGES ON restaurant_db.* TO '${MYSQL_APP_USER}'@'%';
  GRANT ALL PRIVILEGES ON review_db.*     TO '${MYSQL_APP_USER}'@'%';

  FLUSH PRIVILEGES;
EOSQL

echo ">>> Bases de données initialisées avec succès."
