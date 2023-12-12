-- DROP FUNCTION public.update_day_remaining_capacity();

CREATE OR REPLACE FUNCTION public.update_day_remaining_capacity()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
DECLARE
    minCapacity         int;
    DECLARE oldCapacity int;

BEGIN
    -- Your custom logic goes here
    RAISE NOTICE 'After Update Trigger: Table calender_hour has been updated.';
    if NEW.remaining_capacity = - 1 or NEW.remaining_capacity > OLD.remaining_capacity THEN
        select cm.remaining_capacity into oldCapacity from public.calender_day cm where cm.id = new.day_id;

        if OLD.remaining_capacity = oldCapacity then
            select ch.remaining_capacity
            into minCapacity
            from public.calender_hour ch
            where day_id = NEW.day_id
              and ch.remaining_capacity >= 0
            order by ch.remaining_capacity asc
            limit 1;
            update public.calender_day set remaining_capacity = minCapacity where id = new.day_id;
        end if;

        return new;

    END IF;
    IF NEW.remaining_capacity < OLD.remaining_capacity THEN


        select cm.remaining_capacity into oldCapacity from public.calender_day cm where cm.id = new.day_id;
        if NEW.remaining_capacity < oldCapacity then
            update public.calender_day set remaining_capacity = NEW.remaining_capacity where id = new.day_id;
        end if;


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
DECLARE
    minCapacity         int;
    DECLARE oldCapacity int;
BEGIN
    -- Your custom logic goes here
    RAISE NOTICE 'After Update Trigger: Table calender_hour has been updated.';
    IF NEW.remaining_capacity = - 1 or NEW.remaining_capacity > OLD.remaining_capacity THEN
        select cm.remaining_capacity into oldCapacity from public.calender_month cm where cm.id = NEW.month_id;
        if OLD.remaining_capacity = oldCapacity then

            select ch.remaining_capacity
            into minCapacity
            from public.calender_day ch
            where month_id = NEW.month_id
              and ch.remaining_capacity >= 0
            order by ch.remaining_capacity asc
            limit 1;
            update public.calender_month set remaining_capacity = minCapacity where id = new.month_id;
        end if;
        return new;
    END IF;
    IF NEW.remaining_capacity < OLD.remaining_capacity THEN

        select cm.remaining_capacity into oldCapacity from public.calender_month cm where cm.id = NEW.month_id;
        if NEW.remaining_capacity < oldCapacity then
            update public.calender_month set remaining_capacity = NEW.remaining_capacity where id = new.month_id;
        end if;

    END IF;
    return new;
END;
$function$
;
-- DROP FUNCTION public.update_year_remaining_capacity();

CREATE OR REPLACE FUNCTION public.update_year_remaining_capacity()
    RETURNS trigger
    LANGUAGE plpgsql
AS
$function$
DECLARE
    minCapacity         int;
    DECLARE oldCapacity int;
BEGIN
    -- Your custom logic goes here
    RAISE NOTICE 'After Update Trigger: Table calender_hour has been updated.';
    IF NEW.remaining_capacity = - 1 or NEW.remaining_capacity > OLD.remaining_capacity THEN
        select cm.remaining_capacity into oldCapacity from public.calender_year cm where cm.id = NEW.year_id;
        if OLD.remaining_capacity = oldCapacity then

            select ch.remaining_capacity
            into minCapacity
            from public.calender_month ch
            where year_id = NEW.year_id
              and ch.remaining_capacity >= 0
            order by ch.remaining_capacity asc
            limit 1;
            update public.calender_year set remaining_capacity = minCapacity where id = new.year_id;
        END IF;
        return new;
    END IF;
    IF NEW.remaining_capacity < OLD.remaining_capacity THEN

        select cm.remaining_capacity into oldCapacity from public.calender_year cm where cm.id = NEW.year_id;
        if NEW.remaining_capacity < oldCapacity then
            update public.calender_year set remaining_capacity = NEW.remaining_capacity where id = NEW.year_id;
        end if;

    END IF;
    return new;

END;
$function$
;
