-- 가장 먼저 참조되지 않는 테이블 생성
CREATE TABLE users (
                       user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       uuid VARCHAR(255),
                       role VARCHAR(255)
);

-- 다음으로 users를 참조하는 managers 테이블 생성
CREATE TABLE managers (
                          manager_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT,
                          manager_name VARCHAR(50),
                          profile_image VARCHAR(255),
                          career VARCHAR(255),
                          comment VARCHAR(255),
                          working_region VARCHAR(255),
                          gender VARCHAR(10),
                          is_registered BOOLEAN DEFAULT FALSE,
                          FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- managers를 참조하는 working_hour 테이블 생성
CREATE TABLE working_hour (
                              manager_id BIGINT PRIMARY KEY,
                              mon_start_time VARCHAR(5) DEFAULT '00:00',
                              mon_end_time VARCHAR(5) DEFAULT '00:00',
                              tue_start_time VARCHAR(5) DEFAULT '00:00',
                              tue_end_time VARCHAR(5) DEFAULT '00:00',
                              wed_start_time VARCHAR(5) DEFAULT '00:00',
                              wed_end_time VARCHAR(5) DEFAULT '00:00',
                              thu_start_time VARCHAR(5) DEFAULT '00:00',
                              thu_end_time VARCHAR(5) DEFAULT '00:00',
                              fri_start_time VARCHAR(5) DEFAULT '00:00',
                              fri_end_time VARCHAR(5) DEFAULT '00:00',
                              sat_start_time VARCHAR(5) DEFAULT '00:00',
                              sat_end_time VARCHAR(5) DEFAULT '00:00',
                              sun_start_time VARCHAR(5) DEFAULT '00:00',
                              sun_end_time VARCHAR(5) DEFAULT '00:00',
                              FOREIGN KEY (manager_id) REFERENCES managers(manager_id)
);

-- patient 테이블 생성 (다른 테이블에서 참조하기 전에 생성 필요)
CREATE TABLE patient (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         phone_number VARCHAR(255) NOT NULL,
                         patient_gender VARCHAR(255) NOT NULL,
                         patient_birth DATE NOT NULL,
                         nok_phone VARCHAR(255) NOT NULL,
                         patient_relation VARCHAR(255) NOT NULL
);

-- managers와 users, patient를 참조하는 reservations 테이블 생성
CREATE TABLE reservations (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              manager_id BIGINT NOT NULL,
                              user_id BIGINT NOT NULL,
                              departure_location VARCHAR(255) NOT NULL,
                              arrival_location VARCHAR(255) NOT NULL,
                              reservation_date TIMESTAMP NOT NULL,
                              created_time TIMESTAMP NOT NULL,
                              payment_status BOOLEAN NOT NULL,
                              reservation_status VARCHAR(255) NOT NULL,
                              cancel_reason VARCHAR(255),
                              cancel_detail VARCHAR(255),
                              creation_date TIMESTAMP,
                              service_type VARCHAR(255),
                              transportation VARCHAR(255),
                              price INT,
                              manager_status BOOLEAN,
                              patient_id BIGINT,
                              FOREIGN KEY (manager_id) REFERENCES managers(manager_id),
                              FOREIGN KEY (user_id) REFERENCES users(user_id),
                              FOREIGN KEY (patient_id) REFERENCES patient(id)
);

-- reservations를 참조하는 accompanies 테이블 생성
CREATE TABLE accompanies (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             reservation_id BIGINT,
                             status VARCHAR(255),
                             latitude DOUBLE,
                             longitude DOUBLE,
                             time TIMESTAMP,
                             detail VARCHAR(255),
                             FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- users를 참조하는 billing_keys 테이블 생성
CREATE TABLE billing_keys (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id BIGINT NOT NULL,
                              bid VARCHAR(255),
                              card_code VARCHAR(255),
                              card_name VARCHAR(255),
                              order_id VARCHAR(255),
                              card_alias VARCHAR(255),
                              FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- managers를 참조하는 certificates 테이블 생성
CREATE TABLE certificates (
                              certificate_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              certificate_image VARCHAR(255),
                              manager_id BIGINT NOT NULL,
                              FOREIGN KEY (manager_id) REFERENCES managers(manager_id)
);

-- users와 reservations를 참조하는 payments 테이블 생성
CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          reservation_id BIGINT,
                          status VARCHAR(255),
                          order_id VARCHAR(255),
                          amount INT,
                          balance_amt INT,
                          paid_at VARCHAR(255),
                          cancelled_at VARCHAR(255),
                          goods_name VARCHAR(255),
                          pay_method VARCHAR(255),
                          card_alias VARCHAR(255),
                          tid VARCHAR(255),
                          receipt_url VARCHAR(255),
                          FOREIGN KEY (user_id) REFERENCES users(user_id),
                          FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- 독립적으로 생성할 수 있는 refreshes 테이블 생성
CREATE TABLE refreshes (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           username VARCHAR(255),
                           refresh VARCHAR(255),
                           expiration VARCHAR(255)
);

-- reservations를 참조하는 reports 테이블 생성
CREATE TABLE reports (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         reservation_id BIGINT,
                         doctor_summary VARCHAR(255),
                         frequency INT,
                         meal_time VARCHAR(255),
                         time_of_day VARCHAR(255),
                         FOREIGN KEY (reservation_id) REFERENCES reservations(id)
);

-- reports를 참조하는 time_of_days 테이블 생성
CREATE TABLE time_of_days (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              time_of_day VARCHAR(255),
                              report_id BIGINT NOT NULL,
                              FOREIGN KEY (report_id) REFERENCES reports(id)
);
