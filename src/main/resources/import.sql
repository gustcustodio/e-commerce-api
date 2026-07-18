INSERT INTO tb_category (name) VALUES ('Books');
INSERT INTO tb_category (name) VALUES ('Electronics');
INSERT INTO tb_category (name) VALUES ('Home Office');

INSERT INTO tb_product (name, description, price, quantity) VALUES ('1984', 'George Orwells chilling prophecy about the future.', 29.90, 25);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('The Great Gatsby', 'F. Scott Fitzgeralds masterpiece of the Jazz Age.', 35.50, 20);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('Frankenstein', 'Mary Shelleys legendary tale of ambition.', 28.00, 15);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('Crime and Punishment', 'Fyodor Dostoevskys psychological analysis.', 48.00, 10);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('Moby Dick', 'Herman Melvilles complex narrative of an obsessed captain.', 42.90, 12);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('The Little Prince', 'Antoines poetic and timeless story about life.', 25.00, 40);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('Pride and Prejudice', 'Jane Austens sharp-witted classic about love.', 32.00, 18);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('To the Lighthouse', 'Virginia Woolfs innovative exploration of family life.', 38.00, 8);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('Animal Farm', 'Orwells allegorical novella about power.', 24.50, 30);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('The Catcher in the Rye', 'J.D. Salingers portrayal of teenage rebellion.', 34.00, 22);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('The Stranger', 'Albert Camuss famous novel on the absurdity of life.', 27.50, 15);
INSERT INTO tb_product (name, description, price, quantity) VALUES ('Logitech MX Master 3S', 'High-performance wireless mouse.', 550.00, 8);

INSERT INTO tb_product_category (product_id, category_id) VALUES (1, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (2, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (3, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (4, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (5, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (6, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (7, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (8, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (9, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (10, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (11, 1);
INSERT INTO tb_product_category (product_id, category_id) VALUES (12, 2);
INSERT INTO tb_product_category (product_id, category_id) VALUES (12, 3);

INSERT INTO tb_user(name, email, password, active) VALUES ('Liev Tolstói', 'tolstoi@email.com', '$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe', true);
INSERT INTO tb_user(name, email, password, active) VALUES ('Charles Dickens', 'dickens@email.com', '$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe', true);
INSERT INTO tb_user(name, email, password, active) VALUES ('Oscar Wilde', 'wilde@email.com', '$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe', true);
INSERT INTO tb_user(name, email, password, active) VALUES ('Emily Bronte', 'bronte@email.com', '$2a$10$VDNktmwwHMKRm8lUbdaHw.oYJbkU6hX3laW1pkyx7m7fb69QMdeJe', true);

INSERT INTO tb_role (authority) VALUES ('ROLE_ADMIN');
INSERT INTO tb_role (authority) VALUES ('ROLE_CLIENT');

INSERT INTO tb_user_role (user_id, role_id) VALUES (1, 2);
INSERT INTO tb_user_role (user_id, role_id) VALUES (2, 1);
INSERT INTO tb_user_role (user_id, role_id) VALUES (2, 2);
INSERT INTO tb_user_role (user_id, role_id) VALUES (3, 2);
INSERT INTO tb_user_role (user_id, role_id) VALUES (4, 2);

INSERT INTO tb_order (order_date, order_status, client_id) VALUES (TIMESTAMP WITH TIME ZONE '2026-03-25T13:00:00Z', 0, 1);
INSERT INTO tb_order (order_date, order_status, client_id) VALUES (TIMESTAMP WITH TIME ZONE '2026-04-29T15:50:00Z', 0, 2);
INSERT INTO tb_order (order_date, order_status, client_id) VALUES (TIMESTAMP WITH TIME ZONE '2026-05-03T14:20:00Z', 1, 1);

INSERT INTO tb_order_item (order_id, product_id, quantity, price) VALUES (1, 1, 1, 29.90);
INSERT INTO tb_order_item (order_id, product_id, quantity, price) VALUES (1, 5, 1, 42.90);
INSERT INTO tb_order_item (order_id, product_id, quantity, price) VALUES (2, 4, 1, 48.00);
INSERT INTO tb_order_item (order_id, product_id, quantity, price) VALUES (3, 2, 2, 35.50);