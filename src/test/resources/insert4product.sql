delete
from product;
insert into public.product (product_id, name, description, product_type_id, open_date, create_time, create_user,
                            last_modify_time, last_modify_user)
values (1, 'Test Card', 'Test Card Description', 1, '2024-08-02T10:10:10.00Z'::timestamptz,
        '2024-08-02T10:10:10.00Z'::timestamptz, 'omni', '2024-08-02T10:10:10.00Z'::timestamptz, 'omni')
     , (2, 'Test Credit', 'Test Credit Description', 2, '2024-08-02T10:10:10.00Z'::timestamptz,
        '2024-08-02T10:10:10.00Z'::timestamptz, 'omni', '2024-08-02T10:10:10.00Z'::timestamptz, 'omni')
     , (3, 'Test Deposit', 'Test Deposit Description', 3, '2024-08-02T10:10:10.00Z'::timestamptz,
        '2024-08-02T10:10:10.00Z'::timestamptz, 'omni', '2024-08-02T10:10:10.00Z'::timestamptz, 'omni')
     , (4, 'Test Card 2', 'Test Card 2 Description', 1, '2024-08-02T10:10:10.00Z'::timestamptz,
        '2024-08-02T10:10:10.00Z'::timestamptz, 'omni', '2024-08-02T10:10:10.00Z'::timestamptz, 'omni');

ALTER SEQUENCE product_product_id_seq RESTART WITH 5;