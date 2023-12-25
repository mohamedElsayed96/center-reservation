-- public.calender_day definition

-- Drop table

-- DROP TABLE calender_day;

CREATE TABLE calender_day (
                              center_id int4 NULL,
                              day_of_the_month int4 NOT NULL,
                              remaining_capacity int4 NOT NULL,
                              remaining_evening_capacity int4 NOT NULL,
                              id int8 NOT NULL,
                              month_id int8 NULL,
                              "name" varchar(255) NULL,
                              CONSTRAINT calender_day_name_check CHECK (((name)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[]))),
                              CONSTRAINT calender_day_pkey PRIMARY KEY (id)
);
CREATE INDEX calender_day_center_id_idx ON public.calender_day USING btree (center_id);
CREATE INDEX calender_day_month_id_idx ON public.calender_day USING btree (month_id);
CREATE INDEX calender_day_name_idx ON public.calender_day USING btree (name);

-- Table Triggers

create trigger update_month_remaining_capacity_trigger after
    update
        of remaining_capacity on
    public.calender_day for each row execute function update_month_remaining_capacity();
create trigger update_month_remaining_evening_capacity_trigger after
    update
        of remaining_evening_capacity on
    public.calender_day for each row execute function update_month_remaining_capacity();
create trigger delete_hour_of_day_trigger after
    delete
    on
        public.calender_day for each row execute function delete_hour_of_the_day();


-- public.calender_hour definition

-- Drop table

-- DROP TABLE calender_hour;

CREATE TABLE calender_hour (
                               center_id int4 NULL,
                               remaining_capacity int4 NOT NULL,
                               remaining_evening_capacity int4 NOT NULL,
                               "time" time(6) NULL,
                               day_id int8 NULL,
                               id int8 NOT NULL,
                               CONSTRAINT calender_hour_pkey PRIMARY KEY (id)
);
CREATE INDEX calender_hour_center_id_idx ON public.calender_hour USING btree (center_id);
CREATE INDEX calender_hour_day_id_idx ON public.calender_hour USING btree (day_id);

-- Table Triggers

create trigger update_day_remaining_capacity_trigger after
    update
        of remaining_capacity on
    public.calender_hour for each row execute function update_day_remaining_capacity();
create trigger update_day_remaining_evening_capacity_trigger after
    update
        of remaining_evening_capacity on
    public.calender_hour for each row execute function update_day_remaining_capacity();


-- public.calender_month definition

-- Drop table

-- DROP TABLE calender_month;

CREATE TABLE calender_month (
                                center_id int4 NULL,
                                remaining_capacity int4 NOT NULL,
                                remaining_evening_capacity int4 NOT NULL,
                                id int8 NOT NULL,
                                "name" varchar(255) NULL,
                                CONSTRAINT calender_month_pkey PRIMARY KEY (id)
);
CREATE INDEX calender_month_center_id_idx ON public.calender_month USING btree (center_id);


-- public.center definition

-- Drop table

-- DROP TABLE center;

CREATE TABLE center (
                        end_license_date date NULL,
                        end_working_hour time(6) NULL,
                        evening_end_working_hour time(6) NULL,
                        evening_max_capacity int4 NOT NULL,
                        evening_start_working_hour time(6) NULL,
                        id serial4 NOT NULL,
                        max_capacity int4 NOT NULL,
                        start_working_hour time(6) NULL,
                        created_date timestamp(6) NULL,
                        "name" varchar(255) NULL,
                        CONSTRAINT center_pkey PRIMARY KEY (id)
);


-- public.package definition

-- Drop table

-- DROP TABLE package;

CREATE TABLE package (
                         center_id int4 NULL,
                         id int4 NOT NULL DEFAULT nextval('package_entity_id_seq'::regclass),
                         "type" varchar(255) NULL,
                         CONSTRAINT package_entity_pkey PRIMARY KEY (id),
                         CONSTRAINT package_entity_type_check CHECK (((type)::text = ANY ((ARRAY['HOURS'::character varying, 'DAYS'::character varying, 'MONTHS'::character varying])::text[]))),
                         CONSTRAINT fksso6nkepovf9lq2tsyyab0qo5 FOREIGN KEY (center_id) REFERENCES center(id)
);


-- public.working_day definition

-- Drop table

-- DROP TABLE working_day;

CREATE TABLE working_day (
                             center_id int4 NULL,
                             id serial4 NOT NULL,
                             "name" varchar(255) NULL,
                             CONSTRAINT working_day_name_check CHECK (((name)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[]))),
                             CONSTRAINT working_day_pkey PRIMARY KEY (id),
                             CONSTRAINT fk82jk4dohe8xklx2uyhwt09g55 FOREIGN KEY (center_id) REFERENCES center(id)
);