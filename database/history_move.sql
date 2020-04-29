BEGIN;

INSERT INTO train_history (train_id, train_static, depart_date)
SELECT train_id, train_static, depart_date
FROM train_active
         JOIN train_static ON train_active.train_static = train_static.train_static_id
WHERE CURRENT_DATE > train_active.depart_date + train_static.arrive_time;

DELETE
FROM train_active
WHERE train_id IN (
    SELECT train_id
    FROM train_history
);

INSERT INTO ticket_history(ticket_id, train_id, depart_station, arrive_station,
                           seat_id, seat_num, passenger_id, username)
SELECT ticket_id,
       train_id,
       depart_station,
       arrive_station,
       seat_id,
       seat_num,
       passenger_id,
       username
FROM ticket_active
WHERE train_id IN (
    SELECT train_id
    FROM train_history
);

DELETE
FROM ticket_active
WHERE train_id IN (
    SELECT train_id
    FROM train_history
);

COMMIT;

