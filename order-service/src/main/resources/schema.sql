-- 创建订单表
CREATE TABLE t_order
(
    order_id     BIGINT AUTO_INCREMENT PRIMARY KEY,   -- 订单 ID，主键
    user_id      BIGINT         NOT NULL,             -- 用户 ID，用于关联用户
    order_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 下单日期
    status       VARCHAR(50)    NOT NULL,             -- 订单状态，如 'PENDING', 'SHIPPED', 'DELIVERED'
    total_amount DECIMAL(19, 2) NOT NULL              -- 订单总金额
);

-- 创建订单明细表
CREATE TABLE t_order_detail
(
    order_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,  -- 订单明细 ID，主键
    order_id        BIGINT         NOT NULL,            -- 关联的订单 ID
    product_id      BIGINT         NOT NULL,            -- 商品 ID
    quantity        INTEGER        NOT NULL,            -- 购买数量
    price           DECIMAL(19, 2) NOT NULL,            -- 商品单价
    total_price     DECIMAL(19, 2) NOT NULL,            -- 该商品的总价格（quantity * price）

    FOREIGN KEY (order_id) REFERENCES t_order (order_id) -- 外键，关联到订单表
);

-- 添加索引以提高查询性能
CREATE INDEX idx_order_user ON t_order (user_id);
CREATE INDEX idx_order_detail_order ON t_order_detail (order_id);
