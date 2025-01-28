-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_mvs
-- Table Name : mvs.mvs_application_h
-- Purpose    : MVS Applications History: This to track changes to Application details.
-- ------------------------------------------------------------------------------------------

-- object: mvs.mvs_application_h | type: TABLE --
-- DROP TABLE IF EXISTS mvs.mvs_application_h CASCADE;
CREATE TABLE mvs.mvs_application_h (
    reg_id character varying(255) NOT NULL,
    service character varying(255),
    service_type character varying(255),
    verified_officer_id character varying(255),
    verified_officer_name character varying(255),
    verified_officer_role character varying(255) NOT NULL,
    stage character varying(255),
    comments character varying,
    rejection_category character varying(255),
	escalation_details character varying,
    cr_by character varying(255) NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
    status_comment character varying(255),
    PRIMARY KEY (reg_id, verified_officer_role)
);