USE library_management_native;

INSERT INTO users(username, password_hash, role)
VALUES
('admin', '240be518fabd2724ddb6f04eeb9c342f0ed5c0f68d5f92815c90f95f0d45d2e5', 'ADMIN'),
('kiosk', '811cb434445bb03fd26a76db9902ee04a154d1f5d2e7834a6d460a6f28545d7b', 'KIOSK')
ON DUPLICATE KEY UPDATE username = VALUES(username);