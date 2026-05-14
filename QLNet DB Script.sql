CREATE DATABASE NetManager;
USE NetManager;

CREATE TABLE Computer (
    computerId INT PRIMARY KEY AUTO_INCREMENT,
    computerName VARCHAR(100) NOT NULL,
    pricePerHour DOUBLE NOT NULL,
    status VARCHAR(30) DEFAULT 'Available' -- Available, Occupied, Maintenance
);

CREATE TABLE UsageSession (
    sessionId INT PRIMARY KEY AUTO_INCREMENT,
    userId INT,
    computerId INT,
    startTime DATETIME DEFAULT CURRENT_TIMESTAMP,
    endTime DATETIME,
    totalPrice DOUBLE,
    FOREIGN KEY (userId) REFERENCES User(userId),
    FOREIGN KEY (computerId) REFERENCES Computer(computerId)
);


-- Bước 2: Bổ sung các cột phục vụ cho việc in Bill vào bảng UsageSession
ALTER TABLE `UsageSession`
ADD COLUMN `priceAtStart` DOUBLE NOT NULL AFTER `computerId`, -- Chốt đơn giá tại thời điểm bắt đầu chơi
ADD COLUMN `paymentStatus` ENUM('UNPAID', 'PAID', 'DEBT') NOT NULL DEFAULT 'UNPAID' AFTER `totalPrice`;

select * from UsageSession;
select * from Computer;

Update Computer SET status = 'AVAILABLE' where computerId = 5