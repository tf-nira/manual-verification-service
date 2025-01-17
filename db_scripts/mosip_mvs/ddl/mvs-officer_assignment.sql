-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_mvs
-- Table Name 	: mvs.officer_assignment
-- Purpose    	: Officer Assignment Detail : Details of assignment of applications to officers for each roles
-- ------------------------------------------------------------------------------------------

-- ------------------------------------------------------------------------------------------
-- object: mvs.officer_assignment | type: TABLE --
-- DROP TABLE IF EXISTS mvs.officer_assignment CASCADE;
CREATE TABLE mvs.officer_assignment (
    id character varying(255) PRIMARY KEY,
    user_id character varying(255),
    user_role character varying(255),
    cr_by character varying(255) NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
	upd_by character varying(255),
    upd_dtimes TIMESTAMP
);