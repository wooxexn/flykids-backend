-- missions 테이블에 1번 미션 추가
INSERT INTO missions (id, time_limit, total_coin_count, title, type)
VALUES (1, 300, 10, '드론 도전 미션', 'COMPLEX');

-- mission_items 테이블에 미션 아이템 3개 추가 (mission_id=1)
INSERT INTO mission_items (id, mission_id, time_limit, total_coin_count, type, title)
VALUES
    (1, 1, 300, 10, 'COIN', '코인 모으기'),
(2, 1, 300, 0, 'OBSTACLE', '장애물 피하기'),
(3, 1, 300, 0, 'PHOTO', '사진 찍기');