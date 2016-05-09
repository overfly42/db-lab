-- Parse::SQL::Dia      version 0.26                              
-- Documentation        http://search.cpan.org/dist/Parse-Dia-SQL/
-- Environment          Perl 5.020002, /usr/bin/perl              
-- Architecture         x86_64-linux                              
-- Target Database      postgres                                  
-- Input file           uml_db.dia                                
-- Generated at         Thu May  5 14:07:12 2016                  
-- Typemap for postgres not found in input file                   

-- get_constraints_drop 

-- get_permissions_drop 

-- get_view_drop

-- get_schema_drop


CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;


-- get_smallpackage_pre_sql 

-- get_schema_create
create table Haus (
   id          bigserial           not null,
   street      varchar(50)                 ,--  Haus befindet sich an Weg
   name        varchar(50)                 ,
   housenumber varchar(10)                 ,
   postcode    varchar(5)                  ,
   city        varchar(50)                 ,
   geb_nr      int                         ,
   levels      int                         ,
   height      decimal                     ,
   roof_shape  varchar(50)                 ,
   amnity      varchar(50)                 ,
   shop        varchar(50)                 ,
   tourism     varchar(50)                 ,
   operator    varchar(50)                 ,
   umriss      geometry('polygon')         ,
   pos         geometry('point')           ,
   constraint pk_Haus primary key (id)
)   ;
create table Strasse (
   id       bigserial              not null,
   name     varchar(50)                    ,
   oneway   boolean                        ,
   maxspeed int                            ,
   surface  varchar(50)                    ,
   lanes    int                            ,
   path     geometry('linestring')         ,
   constraint pk_Strasse primary key (id)
)   ;
create table Strassenbahn (
   id   bigserial              not null,
   path geometry('linestring')         ,
   constraint pk_Strassenbahn primary key (id)
)   ;
create table Eisenbahn (
   id   bigserial              not null,
   path geometry('linestring')         ,
   constraint pk_Eisenbahn primary key (id)
)   ;
create table Parkplatz (
   id       bigserial           not null,
   access   boolean                     ,
   fee      boolean                     ,
   capicity int                         ,
   operator varchar (50)                ,
   umriss   geometry('polygon')         ,
   name     varchar(50)                 ,
   constraint pk_Parkplatz primary key (id)
)   ;
create table Ampel (
   id        bigserial         not null,
   pos       geometry('point')         ,
   sound     boolean                   ,
   vibration boolean                   ,
   constraint pk_Ampel primary key (id)
)   ;
create table Haltestelle (
   id         bigserial         not null,
   pos        geometry('point')         ,
   shelter    boolean                   ,
   bus_routes varchar(50)               ,
   name       varchar(50)               ,
   constraint pk_Haltestelle primary key (id)
)   ;
create table See (
   id     bigserial           not null,
   umriss geometry('polygon')         ,
   name   varchar(50)                 ,
   constraint pk_See primary key (id)
)   ;
create table Landnutzung (
   id      bigserial           not null,
   umriss  geometry('polygon')         ,
   landuse varchar(50)                 ,
   constraint pk_Landnutzung primary key (id)
)   ;
create table Park (
   id     bigserial           not null,
   umriss geometry('polygon')         ,
   name   varchar(50)                 ,
   constraint pk_Park primary key (id)
)   ;
create table Spielplatz (
   id     bigserial           not null,
   umriss geometry('polygon')         ,
   name   varchar(50)                 ,
   constraint pk_Spielplatz primary key (id)
)   ;
create table Fluss (
   id   bigserial              not null,
   name varchar(50)                    ,
   path geometry('linestring')         ,
   constraint pk_Fluss primary key (id)
)   ;
create table Tunnel (
   id       bigserial              not null,
   name     varchar(50)                    ,
   oneway   boolean                        ,
   maxspeed int                            ,
   surface  varchar(50)                    ,
   lanes    int                            ,
   road     boolean                        ,
   river    boolean                        ,
   rail     boolean                        ,
   path     geometry('linestring')         ,
   constraint pk_Tunnel primary key (id)
)   ;
create table Bruecke (
   id       bigserial              not null,
   name     varchar(50)                    ,
   oneway   boolean                        ,
   maxspeed int                            ,
   surface  varchar(50)                    ,
   lanes    int                            ,
   road     boolean                        ,
   rail     boolean                        ,
   path     geometry('linestring')         ,
   constraint pk_Bruecke primary key (id)
)   ;

-- get_view_create

-- get_permissions_create

-- get_inserts

-- get_smallpackage_post_sql

-- get_associations_create
