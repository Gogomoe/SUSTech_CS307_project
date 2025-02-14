DROP VIEW IF EXISTS user_all;

DROP TRIGGER user_role_trigger ON user_roles;

DROP TABLE IF EXISTS
    "user",user_roles,roles_perms,user_info,
    station, train_static,
    seat, train_seat, train_station, train_station_price,
    train_history, train_active,
    passenger, ticket_history, ticket_active;

DROP SEQUENCE IF EXISTS train_sequence, ticket_sequence;

CREATE SEQUENCE IF NOT EXISTS train_sequence;
CREATE SEQUENCE IF NOT EXISTS ticket_sequence;

CREATE TABLE IF NOT EXISTS "user"
(
    username      VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    password_salt VARCHAR(255) NOT NULL,

    PRIMARY KEY (username)
);

CREATE TABLE IF NOT EXISTS user_info
(
    username VARCHAR(255) NOT NULL,
    avatar   VARCHAR(255),

    FOREIGN KEY (username) REFERENCES "user" (username)
);

CREATE TABLE IF NOT EXISTS roles_perms
(
    role VARCHAR(255) NOT NULL,
    perm VARCHAR(255) NOT NULL,

    PRIMARY KEY (role, perm)
);

CREATE TABLE IF NOT EXISTS user_roles
(
    username VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL,

    PRIMARY KEY (username, role),
    FOREIGN KEY (username) REFERENCES "user" (username)
);

INSERT INTO "user"
VALUES ('admin',
        'B4A9EFB850B66AE184FCF0CE9228475BA4CFA7F649E974A89AF5066C8177620116933C61DD3256F9684A3FA59AFBCC678423C9E63BD5475CE648EF5A4D57404D',
        'AAB0A5DFDBB983C0354997272AAD8433489AD03EAA8A10D42678F4774007845A');
-- password admin

INSERT INTO roles_perms
VALUES ('admin', 'admin');

INSERT INTO user_roles
VALUES ('admin', 'admin');

INSERT INTO user_info
VALUES ('admin', null);


INSERT INTO "user"
VALUES ('user',
        '793E74D9A09435293CA6B42F582DD72098A8F035D45C6BA90F50D41E0D005AB8F64CBD1D22084188A910866AAB8C148112F7CE6BA413AE24D2F1992A3565972C',
        '2F36E2C98FD5BAF7E5A22CE97BB3C965EB0F028C0E5976ACC3915F1BC9008132');
-- password user

INSERT INTO user_info
VALUES ('user', null);

INSERT INTO roles_perms
VALUES ('conductor', 'buy-ticket-for-other'),
       ('attendant', 'buy-ticket-for-other'),
       ('attendant', 'buy-ticket-late'),
       ('after-sales', 'cancel-ticket-for-other'),
       ('after-sales', 'cancel-ticket-late');

CREATE TABLE IF NOT EXISTS station
(
    station_id SERIAL      NOT NULL PRIMARY KEY,
    name       VARCHAR(20) NOT NULL UNIQUE,
    city       VARCHAR(20) NOT NULL,
    code       VARCHAR(5)  NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS train_static
(
    train_static_id SERIAL      NOT NULL PRIMARY KEY,
    code            VARCHAR(10) NOT NULL UNIQUE,
    type            VARCHAR(20) NOT NULL,
    depart_station  INT         NOT NULL REFERENCES station (station_id),
    arrive_station  INT         NOT NULL REFERENCES station (station_id),
    depart_time     INTERVAL(0) NOT NULL,
    arrive_time     INTERVAL(0) NOT NULL
);

CREATE TABLE IF NOT EXISTS seat
(
    seat_id SERIAL      NOT NULL PRIMARY KEY,
    name    VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS train_seat
(
    train_static_id INT NOT NULL REFERENCES train_static (train_static_id),
    seat_id         INT NOT NULL REFERENCES seat (seat_id),
    count           INT NOT NULL,
    PRIMARY KEY (train_static_id, seat_id)
);

CREATE TABLE IF NOT EXISTS train_station
(
    train_static_id INT         NOT NULL REFERENCES train_static (train_static_id),
    station_id      INT         NOT NULL REFERENCES station (station_id),
    arrive_time     INTERVAL(0) NOT NULL,
    depart_time     INTERVAL(0) NOT NULL,
    PRIMARY KEY (train_static_id, station_id)
);

CREATE TABLE IF NOT EXISTS train_station_price
(
    train_static_id INT NOT NULL REFERENCES train_static (train_static_id),
    station_id      INT NOT NULL REFERENCES station (station_id),
    seat_id         INT NOT NULL REFERENCES seat (seat_id),
    remain_price    INT NOT NULL,
    PRIMARY KEY (train_static_id, station_id, seat_id)
);

CREATE TABLE IF NOT EXISTS train_history
(
    train_id     INT  NOT NULL PRIMARY KEY,
    train_static INT  NOT NULL REFERENCES train_static (train_static_id),
    depart_date  DATE NOT NULL,
    UNIQUE (train_static, depart_date)
);
CREATE TABLE IF NOT EXISTS train_active
(
    train_id     INT  NOT NULL PRIMARY KEY,
    train_static INT  NOT NULL REFERENCES train_static (train_static_id),
    depart_date  DATE NOT NULL,
    UNIQUE (train_static, depart_date)
);

CREATE TABLE IF NOT EXISTS passenger
(
    passenger_id SERIAL       NOT NULL PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    people_id    CHAR(18)     NOT NULL,
    phone        VARCHAR(20)  NOT NULL,
    username     VARCHAR(255) NOT NULL REFERENCES "user" (username),
    UNIQUE (people_id, username)
);

CREATE TABLE IF NOT EXISTS ticket_history
(
    ticket_id      INT          NOT NULL PRIMARY KEY,
    train_id       INT          NOT NULL,
    depart_station INT          NOT NULL REFERENCES station (station_id),
    arrive_station INT          NOT NULL REFERENCES station (station_id),
    seat_id        INT          NOT NULL REFERENCES seat (seat_id),
    seat_num       INT,
    passenger_id   INT          NOT NULL REFERENCES passenger (passenger_id),
    username       VARCHAR(255) NOT NULL REFERENCES "user" (username),
    valid          BOOLEAN      NOT NULL DEFAULT TRUE,
    create_time    TIMESTAMP    NOT NULL DEFAULT NOW(),
    update_time    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS ticket_active
(
    ticket_id      INT          NOT NULL PRIMARY KEY,
    train_id       INT          NOT NULL,
    depart_station INT          NOT NULL REFERENCES station (station_id),
    arrive_station INT          NOT NULL REFERENCES station (station_id),
    seat_id        INT          NOT NULL REFERENCES seat (seat_id),
    seat_num       INT,
    passenger_id   INT          NOT NULL REFERENCES passenger (passenger_id),
    username       VARCHAR(255) NOT NULL REFERENCES "user" (username),
    valid          BOOLEAN      NOT NULL DEFAULT TRUE,
    create_time    TIMESTAMP    NOT NULL DEFAULT NOW(),
    update_time    TIMESTAMP    NOT NULL DEFAULT NOW()
);


CREATE OR REPLACE VIEW user_all AS
SELECT u.*, ui.avatar
FROM "user" u
         JOIN user_info ui on u.username = ui.username;

CREATE OR REPLACE FUNCTION user_role_trigger_fun() RETURNS TRIGGER AS
$$
BEGIN
    IF (SELECT count(*) FROM roles_perms WHERE user_roles.role = NEW.role) = 0 THEN
        raise 'role not exist';
    END IF;

    RETURN NEW;

END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER user_role_trigger
    BEFORE INSERT
    ON user_roles
    FOR EACH ROW
EXECUTE PROCEDURE user_role_trigger_fun();
