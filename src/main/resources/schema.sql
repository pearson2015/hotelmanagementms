CREATE TABLE IF NOT EXISTS room (
    id int NOT NULL AUTO_INCREMENT,
    room_number varchar(50) NOT NULL,
    room_type varchar(50) NOT NULL,
    price decimal(10, 2) NOT NULL,
    status varchar(50) NOT NULL,
    email varchar(50) DEFAULT NULL,
    payment_id varchar(100) DEFAULT NULL,
    reservation_id varchar(100) DEFAULT NULL,
    PRIMARY KEY (id)
);