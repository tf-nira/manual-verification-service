-- -------------------------------------------------------------------------------------------------
-- Database Name: mosip_mvs
-- Table Name : mvs.mvs_application
-- Purpose    : MVS Applications: Application details stored in this table
-- ------------------------------------------------------------------------------------------

-- object: mvs.mvs_application | type: TABLE --
-- DROP TABLE IF EXISTS mvs.mvs_application CASCADE;
CREATE TABLE mvs.mvs_application (
    reg_id character varying(255) PRIMARY KEY,
    service character varying(255),
    service_type character varying(255),
    reference_url character varying,
	source character varying(255),
	ref_id character varying(255),
	schema_version character varying(255),
	foundlink character varying(255),
	age_group character varying(255),
    assigned_officer_id character varying(255),
    assigned_officer_name character varying(255),
    assigned_officer_role character varying(255),
    stage character varying(255),
    comments character varying,
    rejection_category character varying(255),
	escalation_details character varying,
    cr_by character varying(255) NOT NULL,
    cr_dtimes TIMESTAMP NOT NULL,
    upd_by character varying(255),
    upd_dtimes TIMESTAMP,
    is_deleted BOOLEAN,
    del_dtimes TIMESTAMP,
    status_comment character varying(255)
);