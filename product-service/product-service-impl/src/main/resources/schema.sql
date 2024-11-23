CREATE TABLE "T_PRODUCT"
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    price    DECIMAL(19, 2) NOT NULL,
    stock    INTEGER NOT NULL,
    create_time TIMESTAMP NOT NULL        -- Record creation time
);

INSERT INTO "T_PRODUCT" (name, price, stock, create_time) VALUES ('iPhone 16 Pro', 8999.99, 10, CURRENT_TIMESTAMP);
INSERT INTO "T_PRODUCT" (name, price, stock, create_time) VALUES ('HUAWEI Pura 70 Pro', 5999.99, 10, CURRENT_TIMESTAMP);