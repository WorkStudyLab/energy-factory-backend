-- 상품 더미 데이터 (쇼핑몰 테스트용)
-- ERD 기준 Product 테이블 구조에 맞춰 작성

INSERT INTO product (
    name, price, category, image_url, brand, weight, description, 
    stock, status, storage, weight_unit, created_at, updated_at
) VALUES
-- 고기류
('한우 등심 1++등급', 45000.00, '고기', 'https://example.com/beef1.jpg', '한우명가', 500.00, 
 '최고급 한우 등심으로 부드럽고 고소한 맛이 일품입니다.', 50, 'AVAILABLE', '냉장보관', 'g', 
 NOW(), NOW()),

('돼지 삼겹살', 12000.00, '고기', 'https://example.com/pork1.jpg', '신선육류', 1000.00,
 '신선한 국내산 돼지 삼겹살입니다.', 100, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

('닭가슴살 (훈제)', 8500.00, '고기', 'https://example.com/chicken1.jpg', '프리미엄닭', 300.00,
 '고단백 저지방 훈제 닭가슴살, 다이어트에 최적', 80, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

-- 채소류  
('유기농 상추', 3500.00, '채소', 'https://example.com/lettuce1.jpg', '자연농원', 200.00,
 '농약을 사용하지 않은 신선한 유기농 상추입니다.', 150, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

('방울토마토', 4200.00, '채소', 'https://example.com/tomato1.jpg', '선농', 500.00,
 '달콤하고 싱싱한 방울토마토, 비타민 풍부', 120, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

('브로콜리', 2800.00, '채소', 'https://example.com/broccoli1.jpg', '그린팜', 300.00,
 '영양가 높은 신선한 브로콜리', 90, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

-- 생선류
('연어 필렛', 18000.00, '생선', 'https://example.com/salmon1.jpg', '바다마트', 400.00,
 '노르웨이산 신선한 연어 필렛, 오메가3 풍부', 60, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

('고등어 (국내산)', 6500.00, '생선', 'https://example.com/mackerel1.jpg', '푸른바다', 600.00,
 '국내산 신선한 고등어, DHA 풍부', 70, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

-- 기타
('프리미엄 달걀', 7200.00, '기타', 'https://example.com/egg1.jpg', '행복농장', 1200.00,
 '방사유정란 30개입, 신선하고 영양가 높은 달걀', 200, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW()),

('아보카도', 3200.00, '과일', 'https://example.com/avocado1.jpg', '트로피컬', 200.00,
 '신선한 멕시코산 아보카도, 건강한 지방 공급원', 85, 'AVAILABLE', '냉장보관', 'g',
 NOW(), NOW());