-- ====================================
-- Energy Factory ERD (ERD Cloud Import용)
-- ====================================

-- 1. 사용자 테이블
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '아이디',
  `email` VARCHAR(255) NOT NULL COMMENT '이메일',
  `password` VARCHAR(255) NOT NULL COMMENT '비밀번호',
  `name` VARCHAR(100) NOT NULL COMMENT '이름',
  `phone_number` VARCHAR(20) NULL COMMENT '전화번호',
  `birth_date` DATE NULL COMMENT '생년월일',
  `address` TEXT NULL COMMENT '기본 배송지 주소',
  `provider` VARCHAR(50) NOT NULL COMMENT '소셜 로그인 제공자',
  `provider_id` VARCHAR(255) NULL COMMENT '소셜 계정 식별자',
  `role` VARCHAR(50) NOT NULL COMMENT '권한',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_email` (`email`),
  UNIQUE KEY `UQ_phone_number` (`phone_number`),
  UNIQUE KEY `UQ_provider_id` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 테이블';

-- 2. 상품 테이블
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 고유 ID',
  `name` VARCHAR(255) NOT NULL COMMENT '상품명',
  `price` DECIMAL(10,2) NOT NULL COMMENT '상품 가격',
  `category` VARCHAR(100) NOT NULL COMMENT '카테고리(고기,채소,생선 등등)',
  `image_url` TEXT NULL COMMENT '이미지 URL',
  `brand` VARCHAR(100) NULL COMMENT '브랜드명',
  `weight` DECIMAL(10,2) NULL COMMENT '판매 중량(숫자만)',
  `description` TEXT NULL COMMENT '상세 설명',
  `status` VARCHAR(50) NOT NULL COMMENT '판매 상태',
  `storage` VARCHAR(100) NULL COMMENT '보관 방법',
  `weight_unit` VARCHAR(10) NOT NULL COMMENT '중량 단위(g,ml,L 등)',
  `average_rating` DECIMAL(2,1) NULL DEFAULT 0.0 COMMENT '평균 별점 (0.0 ~ 5.0)',
  `review_count` BIGINT NULL DEFAULT 0 COMMENT '리뷰 개수',
  `score_muscle_gain` DECIMAL(2,1) NULL COMMENT '근육 증가 점수 (0.0-5.0)',
  `score_weight_loss` DECIMAL(2,1) NULL COMMENT '체중 감량 점수 (0.0-5.0)',
  `score_energy` DECIMAL(2,1) NULL COMMENT '에너지 향상 점수 (0.0-5.0)',
  `score_recovery` DECIMAL(2,1) NULL COMMENT '회복 촉진 점수 (0.0-5.0)',
  `score_health` DECIMAL(2,1) NULL COMMENT '전반적 건강 점수 (0.0-5.0)',
  `original_price` DECIMAL(10,2) NULL COMMENT '할인 전 원가',
  `discount_rate` INT NULL COMMENT '할인율 (%)',
  `shipping_fee` DECIMAL(10,2) NULL DEFAULT 0 COMMENT '배송비',
  `free_shipping_threshold` DECIMAL(10,2) NULL COMMENT '무료배송 기준 금액',
  `estimated_delivery_days` VARCHAR(20) NULL COMMENT '예상 배송 기간(예: 1-2일)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품';

-- 3. 태그 테이블
CREATE TABLE `tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '태그 ID',
  `name` VARCHAR(255) NOT NULL COMMENT '태그명(고단백,다이어트,저지방 등등)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='태그';

-- 4. 사용자 배송지 테이블
CREATE TABLE `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '배송지 아이디',
  `user_id` BIGINT NOT NULL COMMENT '사용자 아이디',
  `recipient_name` VARCHAR(100) NOT NULL COMMENT '수령인',
  `phone` VARCHAR(20) NULL COMMENT '수령인 연락처',
  `postal_code` VARCHAR(10) NOT NULL COMMENT '우편번호',
  `address_line1` TEXT NOT NULL COMMENT '기본주소',
  `address_line2` TEXT NULL COMMENT '상세주소',
  `is_default` BOOLEAN NULL DEFAULT FALSE COMMENT '기본 배송지 여부',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `FK_user_TO_user_address` (`user_id`),
  CONSTRAINT `FK_user_TO_user_address` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자 배송지';

-- 5. 상품 변형(옵션) 테이블
CREATE TABLE `product_variant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 변형 고유 ID',
  `product_id` BIGINT NOT NULL COMMENT '상품 ID',
  `variant_name` VARCHAR(100) NOT NULL COMMENT '변형 이름(예: 500g, 1kg)',
  `price` DECIMAL(10,2) NOT NULL COMMENT '해당 변형의 가격',
  `stock` BIGINT NOT NULL DEFAULT 0 COMMENT '해당 변형의 총 재고',
  `reserved_stock` BIGINT NOT NULL DEFAULT 0 COMMENT '예약된 재고(결제 진행 중)',
  `is_default` BOOLEAN NOT NULL DEFAULT FALSE COMMENT '기본 옵션 여부',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `FK_product_TO_product_variant` (`product_id`),
  CONSTRAINT `FK_product_TO_product_variant` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 변형(옵션)';

-- 6. 상품 영양성분 테이블
CREATE TABLE `product_nutrients` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 영양성분 ID',
  `product_id` BIGINT NOT NULL COMMENT '상품 고유 ID',
  `name` VARCHAR(100) NOT NULL COMMENT '영양소 이름(칼로리,탄단지)',
  `value` VARCHAR(50) NOT NULL COMMENT '영양소 값',
  `unit` VARCHAR(20) NOT NULL COMMENT '단위(g,kcal)',
  `daily_percentage` INT NULL COMMENT '일일 권장 섭취량 대비 % (0-100)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `FK_product_TO_product_nutrients` (`product_id`),
  CONSTRAINT `FK_product_TO_product_nutrients` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 영양성분';

-- 7. 상품 태그 연결 테이블
CREATE TABLE `product_tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 태그ID',
  `product_id` BIGINT NOT NULL COMMENT '상품 ID',
  `tag_id` BIGINT NOT NULL COMMENT '태그 ID',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_product_tag` (`product_id`, `tag_id`),
  KEY `FK_product_TO_product_tags` (`product_id`),
  KEY `FK_tags_TO_product_tags` (`tag_id`),
  CONSTRAINT `FK_product_TO_product_tags` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK_tags_TO_product_tags` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='상품 태그';

-- 8. 리뷰 테이블
CREATE TABLE `review` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '리뷰 ID',
  `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
  `product_id` BIGINT NOT NULL COMMENT '상품 ID',
  `rating` DECIMAL(2,1) NOT NULL COMMENT '별점 (1.0 ~ 5.0, 0.5 단위)',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_user_product` (`user_id`, `product_id`),
  KEY `FK_user_TO_review` (`user_id`),
  KEY `FK_product_TO_review` (`product_id`),
  CONSTRAINT `FK_user_TO_review` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_product_TO_review` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='리뷰';

-- 9. 주문 테이블
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '주문 ID',
  `user_id` BIGINT NOT NULL COMMENT '주문자 아이디',
  `order_number` BIGINT NOT NULL COMMENT '주문번호',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '주문 총 합계',
  `status` VARCHAR(20) NOT NULL COMMENT '주문 상태(배송 상태)',
  `payment_status` VARCHAR(20) NOT NULL COMMENT '결제 상태(완료,취소,환불)',
  `recipient_name` VARCHAR(255) NOT NULL COMMENT '수령인',
  `phone_number` VARCHAR(20) NOT NULL COMMENT '수령인 전화번호',
  `postal_code` VARCHAR(20) NOT NULL COMMENT '우편번호',
  `address_line1` TEXT NOT NULL COMMENT '기본주소',
  `address_line2` TEXT NULL COMMENT '상세주소',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_order_number` (`order_number`),
  KEY `FK_user_TO_orders` (`user_id`),
  CONSTRAINT `FK_user_TO_orders` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='주문';

-- 10. 주문 상세 테이블
CREATE TABLE `order_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '주문 상세 ID',
  `order_id` BIGINT NOT NULL COMMENT '주문 ID',
  `product_id` BIGINT NOT NULL COMMENT '상품 ID',
  `variant_id` BIGINT NULL COMMENT '상품 변형 ID (옵션)',
  `quantity` INT NOT NULL COMMENT '주문 수량',
  `price` DECIMAL(10,2) NOT NULL COMMENT '단가',
  `total_price` DECIMAL(10,2) NOT NULL COMMENT '상품별 총액',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  KEY `FK_orders_TO_order_items` (`order_id`),
  KEY `FK_product_TO_order_items` (`product_id`),
  KEY `FK_variant_TO_order_items` (`variant_id`),
  CONSTRAINT `FK_orders_TO_order_items` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FK_product_TO_order_items` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK_variant_TO_order_items` FOREIGN KEY (`variant_id`) REFERENCES `product_variant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='주문 상세';

-- 11. 결제 테이블
CREATE TABLE `payments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '결제 ID',
  `order_id` BIGINT NOT NULL COMMENT '주문 ID',
  `payment_method` VARCHAR(50) NOT NULL COMMENT '결제 수단',
  `payment_status` VARCHAR(50) NOT NULL COMMENT '결제 상태(결제 여부,환불)',
  `transaction_id` VARCHAR(255) NULL COMMENT 'PG사 거래 ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '결제 금액',
  `paid_at` TIMESTAMP NULL COMMENT '결제 완료 시각',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_transaction_id` (`transaction_id`),
  KEY `FK_orders_TO_payments` (`order_id`),
  CONSTRAINT `FK_orders_TO_payments` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='결제';

-- 12. 장바구니 테이블
CREATE TABLE `cart_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '장바구니 아이템 ID',
  `user_id` BIGINT NOT NULL COMMENT '사용자 ID',
  `product_id` BIGINT NOT NULL COMMENT '상품 ID',
  `variant_id` BIGINT NOT NULL COMMENT '상품 변형 ID',
  `quantity` INT NOT NULL COMMENT '수량',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일',
  `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UQ_user_variant` (`user_id`, `variant_id`),
  KEY `FK_user_TO_cart_items` (`user_id`),
  KEY `FK_product_TO_cart_items` (`product_id`),
  KEY `FK_variant_TO_cart_items` (`variant_id`),
  CONSTRAINT `FK_user_TO_cart_items` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FK_product_TO_cart_items` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FK_variant_TO_cart_items` FOREIGN KEY (`variant_id`) REFERENCES `product_variant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='장바구니';
