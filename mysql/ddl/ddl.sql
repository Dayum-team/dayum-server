CREATE TABLE members (
                           `member_id` bigint NOT NULL AUTO_INCREMENT,
                           `created_at` datetime(6) NOT NULL,
                           `updated_at` datetime(6) NOT NULL,
                           `email` varchar(255),
                           `name` varchar(255),
                           `nickname` varchar(255),
                           `profile_image` varchar(255),
                           `bio` varchar(1000) COLLATE utf8mb4_general_ci DEFAULT NULL,
                           `oauth2provider` enum('APPLE','NAVER') COLLATE utf8mb4_general_ci NOT NULL,
                           `deleted` bit(1) NOT NULL,
                           `deleted_at` datetime(6) DEFAULT NULL,
                           PRIMARY KEY (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_members_nickname ON members (nickname);

CREATE TABLE contents
(
    contents_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id     BIGINT    NOT NULL,
    title         VARCHAR(255),
    description   VARCHAR(1024),
    thumbnail_url VARCHAR(255),
    url           VARCHAR(255),
    status        ENUM('PENDING') NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ingredients
(
    ingredient_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    standard_quantity VARCHAR(100) NOT NULL,
    calories DOUBLE NOT NULL DEFAULT 0.0,
    carbohydrates DOUBLE NOT NULL DEFAULT 0.0,
    proteins DOUBLE NOT NULL DEFAULT 0.0,
    fats DOUBLE NOT NULL DEFAULT 0.0,
    sugars DOUBLE NOT NULL DEFAULT 0.0,
    sodium DOUBLE NOT NULL DEFAULT 0.0,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_ingredients_name ON ingredients (name);

CREATE TABLE contents_ingredients
(
    contents_ingredient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contents_id            BIGINT    NOT NULL,
    ingredient_id          BIGINT    NOT NULL,
    quantity               BIGINT    NOT NULL DEFAULT 0.0,
    created_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_contents_ingredients_contents_id ON contents_ingredients (contents_id);
CREATE INDEX idx_contents_ingredients_ingredient_id ON contents_ingredients (ingredient_id);
