#!/bin/bash
#drop and recreate database geo
#parsediasql --file uml_db.dia --db postgres > ./create.sql 
psql -U postgres -c "drop database geo"
psql -U postgres -c "create database geo"
psql -U postgres -d geo -c "CREATE EXTENSION postgis;"
psql -U postgres -d geo -c "CREATE EXTENSION postgis_topology;"
psql -U postgres -d geo -f ./create.sql
