-- remaining_capacity
create trigger update_day_remaining_capacity_trigger
    after update
        of remaining_capacity
    on calender_hour
    for each row
execute procedure update_day_remaining_capacity();

create trigger update_month_remaining_capacity_trigger
    after update
        of remaining_capacity
    on calender_day
    for each row
execute procedure update_month_remaining_capacity();

-- remaining_evening_capacity
create trigger update_day_remaining_evening_capacity_trigger
    after update
        of remaining_evening_capacity
    on calender_hour
    for each row
execute procedure update_day_remaining_capacity();

create trigger update_month_remaining_evening_capacity_trigger
    after update
        of remaining_evening_capacity
    on calender_day
    for each row
execute procedure update_month_remaining_capacity();

create trigger delete_hour_of_day_trigger
    after delete
    on calender_day
    for each row
execute procedure delete_hour_of_the_day();

-- delete




-- auto-generated definition
-- create trigger update_year_remaining_capacity_trigger
--     after update
--         of remaining_capacity
--     on calender_month
--     for each row
-- execute procedure update_year_remaining_capacity();