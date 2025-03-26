-- Database Name: mosip_mvs
-- Table Name : mvs.country_region_mapping
-- Purpose    : Country Region Mapping: Maps countries to their respective regions for international officer assignment

-- object: mvs.country_region_mapping | type: TABLE --
--DROP TABLE IF EXISTS mvs.country_region_mapping CASCADE;
CREATE TABLE mvs.country_region_mapping (
    country_name character varying(128) PRIMARY KEY,
    region character varying(64) NOT NULL,
    cr_by character varying(255) NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
    upd_by character varying(255),
    upd_dtimes TIMESTAMP
);