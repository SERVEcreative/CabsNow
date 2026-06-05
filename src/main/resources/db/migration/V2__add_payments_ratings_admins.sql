-- Tables missed when Flyway baselined an existing schema at v1
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
