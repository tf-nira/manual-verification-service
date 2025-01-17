CREATE DATABASE mosip_mvs 
	ENCODING = 'UTF8' 
	TABLESPACE = pg_default 
	OWNER = postgres;

COMMENT ON DATABASE mosip_mvs IS 'MVS related entities and its data is stored in this database';

\c mosip_mvs 

DROP SCHEMA IF EXISTS mvs CASCADE;
CREATE SCHEMA mvs;
ALTER SCHEMA mvs OWNER TO postgres;
ALTER DATABASE mosip_mvs SET search_path TO mvs,pg_catalog,public;
