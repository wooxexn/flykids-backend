-- 1. 동전 미션 (COIN)
INSERT INTO missions (id, title, type, time_limit, order_index, locked, intro_message)
VALUES (1, '동전 미션', 'COIN', 60, 1, false,
        '오늘 너의 첫 번째 임무는 하늘에 떠 있는 신비한 동전들을 모으는 거야! 부드럽게 드론을 조종해서, 동전을 모두 모아봐!');

INSERT INTO mission_items (id, mission_id, type, time_limit, title, total_coin_count, "order")
VALUES (1001, 1, 'COIN', 60, '동전 미션 아이템', 10, 0);

-- 2. 장애물 미션 (OBSTACLE)
INSERT INTO missions (id, title, type, time_limit, order_index, locked, intro_message)
VALUES (2, '장애물 미션', 'OBSTACLE', 60, 2, false,
        '이번엔 장애물을 피해가야 해! 빠르게 반응해서 충돌하지 않게 조심해. 네 실력을 보여줄 시간이야! 준비됐지? 출발!');

INSERT INTO mission_items (id, mission_id, type, time_limit, title, "order")
VALUES (1002, 2, 'OBSTACLE', 60, '장애물 미션 아이템', 0);

-- 3. 사진 미션 (PHOTO)
INSERT INTO missions (id, title, type, time_limit, order_index, locked, intro_message)
VALUES (3, '사진 미션', 'PHOTO', 60, 3, false,
        '이번 미션은 사진 촬영이야! 멋진 장면을 놓치지 말고 찰칵! 너라면 분명 멋지게 해낼 수 있어!');

INSERT INTO mission_items (id, mission_id, type, time_limit, title, "order")
VALUES (1003, 3, 'PHOTO', 60, '사진 미션 아이템', 0);


