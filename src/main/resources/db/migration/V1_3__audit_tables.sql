CREATE TABLE
  public.revinfo (rev integer NOT NULL, revtstmp bigint NULL, primary key(rev));

CREATE TABLE
  public.consent_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint NULL,
    author character varying(255) NULL,
    end_at timestamp without time zone NULL,
    ssn character varying(255) NULL,
    start_at timestamp without time zone NULL,
    consent_type uuid NULL
  );

CREATE TABLE
  public.consent_type_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint NULL,
    code character varying(255) NULL,
    description character varying(255) NULL
  );


CREATE TABLE
  public.document_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint NULL,
    author character varying(255) NULL,
    created_at timestamp without time zone NULL,
    ehealth_id character varying(255) NULL,
    file bytea NULL,
    file_id uuid NULL,
    gp_code character varying(255) NULL,
    mh_number character varying(255) NULL,
    paid boolean NULL,
    replaced_by uuid NULL,
    sent_at timestamp without time zone NULL,
    ssn character varying(255) NULL,
    document_status uuid NULL,
    document_type uuid NULL
  );


CREATE TABLE
  public.document_status_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint NULL,
    code character varying(255) NULL,
    description character varying(255) NULL
  );


CREATE TABLE
  public.document_type_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint NULL,
    code character varying(255) NULL,
    description character varying(255) NULL
  );



CREATE TABLE
  public.practitioner_member_aud (
    id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint NULL,
    ehealth_id character varying(255) NULL,
    member_id character varying(255) NULL
  );