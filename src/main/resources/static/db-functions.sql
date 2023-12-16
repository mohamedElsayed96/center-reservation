-- DROP FUNCTION public.update_day_remaining_capacity();

CREATE OR REPLACE FUNCTION public.update_day_remaining_capacity()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    RAISE NOTICE 'After Update Trigger: Table calender_hour has been updated.';
    if NEW.remaining_capacity > OLD.remaining_capacity THEN
        call update_hour_tree_state_with_minimum(new.day_id, OLD.remaining_capacity, false);
        return new;
    END IF;
    IF NEW.remaining_capacity < OLD.remaining_capacity THEN
        call reserve_hour(new.day_id, new.remaining_capacity, false);
        return new;
    END IF;
    if NEW.remaining_evening_capacity > OLD.remaining_evening_capacity THEN
        call update_hour_tree_state_with_minimum(new.day_id, OLD.remaining_evening_capacity, true);
        return new;
    END IF;
    IF NEW.remaining_evening_capacity < OLD.remaining_evening_capacity THEN
        call reserve_hour(new.day_id, new.remaining_evening_capacity, true);
        return new;
    END IF;
    return new;
END;
$function$
;
-- DROP FUNCTION public.update_month_remaining_capacity();

CREATE OR REPLACE FUNCTION public.update_month_remaining_capacity()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    -- Your custom logic goes here
    RAISE NOTICE 'After Update Trigger: Table calender_hour has been updated.';
    IF NEW.remaining_capacity > OLD.remaining_capacity THEN
        call update_day_tree_state_with_minimum(NEW.month_id, OLD.remaining_capacity, false);
        return new;
    END IF;
    IF NEW.remaining_capacity < OLD.remaining_capacity THEN
        call reserve_day(new.month_id, new.remaining_capacity, false);
        return new;
    END IF;
    IF NEW.remaining_evening_capacity > OLD.remaining_evening_capacity THEN
        call update_day_tree_state_with_minimum(NEW.month_id, OLD.remaining_evening_capacity, true);
        return new;
    END IF;
    IF NEW.remaining_evening_capacity < OLD.remaining_evening_capacity THEN
        call reserve_day(new.month_id, new.remaining_evening_capacity, true);
        return new;
    END IF;
    return new;

END;
$function$
;

CREATE OR REPLACE FUNCTION public.delete_hour_of_the_day()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
BEGIN
    delete from calender_hour where day_id = old.id;
    return OLD;

END;
$function$
;

CREATE OR REPLACE PROCEDURE public.update_day_tree_state_with_minimum(_month_id int8, old_capacity int, evening boolean)
    LANGUAGE plpgsql
AS
$function$
DECLARE
    minCapacity         int;
    DECLARE oldCapacity int;
BEGIN
    if evening = true then
        select cm.remaining_evening_capacity into oldCapacity from public.calender_month cm where cm.id = _month_id;
        if old_capacity = oldCapacity then
            select ch.remaining_evening_capacity
            into minCapacity
            from public.calender_day ch
            where month_id = _month_id
            order by ch.remaining_evening_capacity
            limit 1;
            update public.calender_month set remaining_evening_capacity = minCapacity where id = _month_id;
        end if;
    end if;
    if evening = false then
        select cm.remaining_capacity into oldCapacity from public.calender_month cm where cm.id = _month_id;
        if old_capacity = oldCapacity then
            select ch.remaining_capacity
            into minCapacity
            from public.calender_day ch
            where month_id = _month_id
            order by ch.remaining_capacity
            limit 1;
            update public.calender_month set remaining_capacity = minCapacity where id = _month_id;
        end if;
    end if;

END;
$function$
;
CREATE OR REPLACE PROCEDURE public.update_tree_state_from_hour_node(_center_id int, start_id int8, evening boolean)
    LANGUAGE plpgsql
AS
$function$
DECLARE
    current_day_id int;
BEGIN
    if evening = true then
        FOR current_day_id IN (SELECT id FROM public.calender_day where center_id = _center_id and id >= start_id)
            LOOP
                update public.calender_day
                set remaining_evening_capacity = (select ch.remaining_evening_capacity
                                                  from public.calender_hour ch
                                                  where day_id = current_day_id
                                                    and ch.remaining_evening_capacity >= 0
                                                  order by ch.remaining_evening_capacity
                                                  limit 1)
                where id = current_day_id;
            END LOOP;
    end if;
    if evening = false then
        FOR current_day_id IN (SELECT id FROM public.calender_day where center_id = _center_id and id >= start_id)
            LOOP
                update public.calender_day
                set remaining_capacity = (select ch.remaining_capacity
                                          from public.calender_hour ch
                                          where day_id = current_day_id
                                            and ch.remaining_capacity >= 0
                                          order by ch.remaining_capacity
                                          limit 1)
                where id = current_day_id;
            END LOOP;
    end if;
end;
$function$
;

CREATE OR REPLACE PROCEDURE public.update_tree_state_from_day_node(_center_id int, start_id int8, default_capacity int, evening boolean)
    LANGUAGE plpgsql
AS
$function$
DECLARE
    minCapacity      int;
    current_month_id int;
BEGIN
    if evening = true then
        FOR current_month_id IN (SELECT id FROM public.calender_month where center_id = _center_id and id >= start_id)
            LOOP
                select ch.remaining_evening_capacity
                into minCapacity
                from public.calender_day ch
                where month_id = current_month_id
                order by ch.remaining_evening_capacity
                limit 1;
                if minCapacity IS NULL then
                    minCapacity = default_capacity;
                end if;
                update public.calender_month set remaining_evening_capacity = minCapacity where id = current_month_id;
            END LOOP;
    end if;
    if evening = false then
        FOR current_month_id IN (SELECT id FROM public.calender_month where center_id = _center_id and id >= start_id)
            LOOP
                select ch.remaining_capacity
                into minCapacity
                from public.calender_day ch
                where month_id = current_month_id
                order by ch.remaining_capacity
                limit 1;
                if minCapacity IS NULL then
                    minCapacity = default_capacity;
                end if;
                update public.calender_month set remaining_capacity = minCapacity where id = current_month_id;
            END LOOP;
    end if;
end;
$function$
;

CREATE OR REPLACE PROCEDURE public.update_hour_tree_state_with_minimum(_day_id int8, old_capacity int, evening boolean)
    LANGUAGE plpgsql
AS
$function$
DECLARE
    minCapacity         int;
    DECLARE oldCapacity int;
BEGIN
    if evening = true then
        select cm.remaining_evening_capacity into oldCapacity from public.calender_day cm where cm.id = _day_id;
        if old_capacity = oldCapacity then
            select ch.remaining_evening_capacity
            into minCapacity
            from public.calender_hour ch
            where day_id = _day_id
              and ch.remaining_evening_capacity >= 0
            order by ch.remaining_evening_capacity
            limit 1;
            update public.calender_day set remaining_evening_capacity = minCapacity where id = _day_id;
        end if;
    end if;
    if evening = false then
        select cm.remaining_capacity into oldCapacity from public.calender_day cm where cm.id = _day_id;
        if old_capacity = oldCapacity then
            select ch.remaining_capacity
            into minCapacity
            from public.calender_hour ch
            where day_id = _day_id
              and ch.remaining_capacity >= 0
            order by ch.remaining_capacity
            limit 1;
            update public.calender_day set remaining_capacity = minCapacity where id = _day_id;
        end if;
    end if;

END;
$function$
;

CREATE OR REPLACE PROCEDURE public.reserve_hour(_day_id int8, new_capacity int, evening boolean)
    LANGUAGE plpgsql
AS
$function$
DECLARE
    DECLARE
    oldCapacity int;
BEGIN
    if evening = true then
        select cm.remaining_evening_capacity into oldCapacity from public.calender_day cm where cm.id = _day_id;
        if new_capacity < oldCapacity then
            update public.calender_day set remaining_evening_capacity = new_capacity where id = _day_id;
        end if;
    end if;
    if evening = false then
        select cm.remaining_capacity into oldCapacity from public.calender_day cm where cm.id = _day_id;
        if new_capacity < oldCapacity then
            update public.calender_day set remaining_capacity = new_capacity where id = _day_id;
        end if;
    end if;

END;
$function$
;
CREATE OR REPLACE PROCEDURE public.reserve_day(_month_id int8, new_capacity int, evening boolean)
    LANGUAGE plpgsql
AS
$function$
DECLARE
    DECLARE
    oldCapacity int;
BEGIN
    if evening = true then
        select cm.remaining_evening_capacity into oldCapacity from public.calender_month cm where cm.id = _month_id;
        if new_capacity < oldCapacity then
            update public.calender_month set remaining_evening_capacity = new_capacity where id = _month_id;
        end if;
    end if;
    if evening = false then
        select cm.remaining_capacity into oldCapacity from public.calender_month cm where cm.id = _month_id;
        if new_capacity < oldCapacity then
            update public.calender_month set remaining_capacity = new_capacity where id = _month_id;
        end if;
    end if;

END;
$function$
;

