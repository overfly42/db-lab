create table Haus (
   id          bigserial           not null,
   street      varchar(50)                 ,
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
   id        bigserial       not null,
   pos       geometry(point)         ,
   sound     boolean                 ,
   vibration boolean                 ,
   constraint pk_Ampel primary key (id)
)   ;
create table Haltestelle (
   id         bigserial       not null,
   pos        geometry(point)         ,
   shelter    boolean                 ,
   bus_routes varchar(50)             ,
   name       varchar(50)             ,
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
