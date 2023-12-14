create index calender_hour_day_id_idx
    on calender_hour (day_id);

create index calender_hour_center_id_idx
    on calender_hour (center_id);

create index calender_day_month_id_idx
    on calender_day (month_id);

create index calender_day_center_id_idx
    on calender_day (center_id);

create index calender_month_center_id_idx
    on calender_month (center_id);

CREATE INDEX calender_day_name_idx ON public.calender_day ("name");
