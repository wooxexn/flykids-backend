-- 1. 동전 미션 (COIN)
INSERT INTO missions (id, title, type, time_limit, order_index, locked)
VALUES (1, '동전 미션', 'COIN', 60, 1, false);

INSERT INTO mission_items (id, mission_id, type, time_limit, title, total_coin_count, "order")
VALUES (1001, 1, 'COIN', 60, '동전 미션 아이템', 10, 0);

-- 2. 장애물 미션 (OBSTACLE)
INSERT INTO missions (id, title, type, time_limit, order_index, locked)
VALUES (2, '장애물 미션', 'OBSTACLE', 60, 2, false);

INSERT INTO mission_items (id, mission_id, type, time_limit, title, "order")
VALUES (1002, 2, 'OBSTACLE', 60, '장애물 미션 아이템', 0);

-- 3. 사진 미션 (PHOTO)
INSERT INTO missions (id, title, type, time_limit, order_index, locked)
VALUES (3, '사진 미션', 'PHOTO', 60, 3, false);

INSERT INTO mission_items (id, mission_id, type, time_limit, title, "order")
VALUES (1003, 3, 'PHOTO', 60, '사진 미션 아이템', 0);