set schema 'fleetforward';

create type user_role as enum('admin', 'dispatcher', 'driver');
create type driver_status as enum('available', 'busy', 'off_duty');
create type trailer_type as enum('dry_van', 'flatbed', 'reefer');
create type driver_company_role as enum('driver', 'owner_operator');
create type job_status as enum ('completed','available','expired','assigned','ongoing');

create table if not exists app_user
(
    id serial primary key,
    email varchar(50) unique not null,
    phone_number varchar(20) not null,
    hashed_password varchar(255) not null,
    first_name varchar(20) not null,
    last_name varchar(20) not null,
    photo_base64 text,
    role user_role not null
);

insert into app_user(id, email, phone_number, hashed_password, first_name, last_name, role)
values (0, 'defaultdriver@default.default', 'default_number','no_password_here', 'Default', 'Default', 'driver'),
       (1,'defaultdispatcher@default.default', 'default_number', 'no_password_here', 'Default', 'Default', 'dispatcher');

create table if not exists dispatcher
(
    dispatcher_id int not null references app_user(id) on delete cascade primary key,
    commission_rate decimal(5,2) default 0.00
);

insert into dispatcher(dispatcher_id)
values (1);

create table if not exists company
(
    mc_number varchar(10) not null unique primary key,
    company_name varchar(50) not null
);

insert into company(mc_number, company_name)
values ('mcnumbrdef', 'Default Company LLC');

create table if not exists driver
(
    driver_id int not null references app_user(id) on delete cascade primary key,
    company_mc_number varchar(10) not null references company(mc_number),
    status driver_status not null,
    current_trailer_type trailer_type not null,
    current_location_state varchar(2),
    current_location_zip_code int4 CHECK (current_location_zip_code>0 and current_location_zip_code<100000),
    role_in_company driver_company_role not null,
    foreign key (current_location_state, current_location_zip_code) references addresses(state_abbr,zip_code)
);

insert into driver(driver_id, company_mc_number, status, current_trailer_type, current_location_state,current_location_zip_code, role_in_company)
values (0, 'mcnumbrdef', 'available', 'dry_van' , 'AL',35010, 'owner_operator');

create table if not exists job
(
    id serial primary key,
    dispatcher_id int references dispatcher(dispatcher_id),
    driver_id int references driver(driver_id) default 0,
    title varchar(20) not null,
    description varchar(300) not null,
    loaded_miles int not null check(loaded_miles > 0),
    weight_of_cargo int not null check (weight_of_cargo > 0),
    type_of_trailer_needed trailer_type not null,
    total_price int not null check ( total_price > 0 ),
    cargo_info varchar(30) not null,
    pickup_time timestamp not null,
    delivery_time timestamp not null,
    pickup_location_state varchar(2),
    pickup_location_zip_code int4 CHECK (pickup_location_zip_code>0 and pickup_location_zip_code<100000),
    drop_location_state varchar(2),
    drop_location_zip_code int4 CHECK (pickup_location_zip_code>0 and pickup_location_zip_code<100000),
    foreign key (pickup_location_state,pickup_location_zip_code) references addresses(state_abbr, zip_code),
    foreign key (drop_location_state,drop_location_zip_code) references addresses(state_abbr, zip_code),
    current_job_status job_status not null
);

create table if not exists message
(
    sender_id int references app_user(id),
    reciever_id int references app_user(id),
    message varchar(500) not null
);

create table drivers_managed_by_dispatcher
(
    dispatcher_id int references dispatcher(dispatcher_id),
    driver_id int references driver(driver_id) unique
);