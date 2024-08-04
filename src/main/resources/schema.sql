CREATE TABLE IF NOT EXISTS room (
    id int NOT NULL AUTO_INCREMENT,
    room_number varchar(50) NOT NULL,
    room_type varchar(50) NOT NULL,
    price decimal(10, 2) NOT NULL,
    status varchar(50) NOT NULL,
    payment_transaction_id varchar(100) DEFAULT NULL,
    reservation_id varchar(100) DEFAULT NULL,
    PRIMARY KEY (id)
);