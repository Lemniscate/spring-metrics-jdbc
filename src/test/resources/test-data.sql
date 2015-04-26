drop table if exists public.metrics;
create table public.metrics(
  name VARCHAR(255) primary key,
  value DOUBLE not null
);

insert into public.metrics (name, value)
    values ('foo', 12.50), ('bar', 45)