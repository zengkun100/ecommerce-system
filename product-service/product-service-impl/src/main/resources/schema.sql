CREATE TABLE "t_product"
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255),
    price    DECIMAL(19, 2),
    quantity INTEGER
);

INSERT INTO "t_product" (name, price, quantity) VALUES ('iPhone 16 Pro', 8999.99, 10);
INSERT INTO "t_product" (name, price, quantity) VALUES ('HUAWEI Pura 70 Pro', 5999.99, 10);