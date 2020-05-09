BEGIN;

DELETE
FROM ticket_active
WHERE train_id IN (
    SELECT train_id
    FROM train_active
    WHERE depart_date <= CURRENT_DATE
);

DELETE
FROM train_active
WHERE depart_date <= CURRENT_DATE+'13d'::interval;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE
FROM train_static;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE+'1d'::interval
FROM train_static;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE+'2d'::interval
FROM train_static;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE+'3d'::interval
FROM train_static;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE+'4d'::interval
FROM train_static;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE+'5d'::interval
FROM train_static;

INSERT INTO train_active (train_id, train_static, depart_date)
SELECT nextval('train_sequence'), train_static_id, CURRENT_DATE+'6d'::interval
FROM train_static;


COMMIT;