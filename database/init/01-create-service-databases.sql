CREATE DATABASE IF NOT EXISTS orders_db;
CREATE DATABASE IF NOT EXISTS products_db;

GRANT ALL PRIVILEGES ON orders_db.* TO 'user'@'%';
GRANT ALL PRIVILEGES ON products_db.* TO 'user'@'%';
