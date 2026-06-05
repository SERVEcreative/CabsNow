CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS riders (
    rider_id INT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT fk_rider_user FOREIGN KEY (rider_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS drivers (
    driver_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    vehicle_number VARCHAR(255) NOT NULL UNIQUE,
    aadhar_number VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20),
    latitude DOUBLE,
    longitude DOUBLE
);

CREATE TABLE IF NOT EXISTS duties (
    duty_id INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(20),
    rider_id INT NOT NULL,
    driver_id INT,
    created_at DATETIME,
    pickup_location VARCHAR(255) NOT NULL,
    drop_location VARCHAR(255) NOT NULL,
    fare DOUBLE NOT NULL,
    vehicle_type VARCHAR(20) NOT NULL,
    CONSTRAINT fk_duty_rider FOREIGN KEY (rider_id) REFERENCES riders(rider_id),
    CONSTRAINT fk_duty_driver FOREIGN KEY (driver_id) REFERENCES drivers(driver_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    duty_id INT NOT NULL UNIQUE,
    amount DOUBLE NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_ref VARCHAR(100),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_payment_duty FOREIGN KEY (duty_id) REFERENCES duties(duty_id)
);

CREATE TABLE IF NOT EXISTS ratings (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    duty_id INT NOT NULL UNIQUE,
    rider_id INT NOT NULL,
    driver_id INT NOT NULL,
    stars INT NOT NULL,
    comment VARCHAR(500),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_rating_duty FOREIGN KEY (duty_id) REFERENCES duties(duty_id)
);

CREATE TABLE IF NOT EXISTS admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);
