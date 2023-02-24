CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS CONSENT_TYPE (
    id UUID DEFAULT uuid_generate_v4 (),
    code VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS DOCUMENT_TYPE (
    id UUID DEFAULT uuid_generate_v4 (),
    code VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS DOCUMENT_STATUS (
    id UUID DEFAULT uuid_generate_v4 (),
    code VARCHAR NOT NULL,
    description VARCHAR NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS CONSENT (
    id UUID DEFAULT uuid_generate_v4 (),
    author VARCHAR NOT NULL,
  	ssn VARCHAR NOT NULL,
  	start_at timestamptz NOT NULL DEFAULT now(),
    end_at timestamptz NOT NULL,
  	consent_type UUID NOT NULL,
    PRIMARY KEY (id),
  	FOREIGN KEY(consent_type) REFERENCES consent_type(id),
  	UNIQUE (ssn, consent_type)
);

CREATE TABLE IF NOT EXISTS DOCUMENT (
    id UUID DEFAULT uuid_generate_v4 (),
    author VARCHAR NOT NULL,
  	gp_code VARCHAR NOT NULL,
  	ehealth_id VARCHAR NOT NULL,
  	replaced_by UUID,
  	ssn VARCHAR NOT NULL,
  	file_id UUID NOT NULL,
  	mh_number VARCHAR NOT NULL,
  	paid boolean NOT NULL DEFAULT false,
  	sent_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
  	document_type UUID NOT NULL,
  	document_status UUID NOT NULL,
  	file BYTEA NOT NULL,
  
    PRIMARY KEY (id),
  	FOREIGN KEY(document_type) REFERENCES document_type(id),
  	FOREIGN KEY(document_status) REFERENCES document_status(id),
  	FOREIGN KEY(replaced_by) REFERENCES document(id),
  	UNIQUE(file_id),
  	UNIQUE(replaced_by)
);

CREATE TABLE IF NOT EXISTS PARTNER (
    id UUID DEFAULT uuid_generate_v4 (),
    name VARCHAR NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS PERSON_PARTNER (
    id UUID DEFAULT uuid_generate_v4 (),
    person_ssn VARCHAR NOT NULL,
  	partner_id UUID NOT NULL,
    PRIMARY KEY (id),
 	FOREIGN KEY(partner_id) REFERENCES partner(id)
);
  
CREATE TABLE IF NOT EXISTS PRACTITIONER_MEMBER (
   id UUID DEFAULT uuid_generate_v4 (),
   ehealth_id VARCHAR NOT NULL,
   member_id VARCHAR NOT NULL,
   PRIMARY KEY (id),
   UNIQUE (ehealth_id, member_id)
 );

CREATE TABLE IF NOT EXISTS TRACE (
    id UUID DEFAULT uuid_generate_v4 (),
    action VARCHAR NOT NULL,
  	author VARCHAR NOT NULL,
    entity_type VARCHAR NOT NULL,
    reference_id UUID,
    created_at timestamptz NOT NULL DEFAULT now(),
    author_roles VARCHAR NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS HISTO_STAT (
	id UUID DEFAULT uuid_generate_v4 (),
  	stat_file VARCHAR NOT NULL,
  	created_at timestamptz NOT NULL DEFAULT now(),
    end_at timestamptz NOT NULL DEFAULT now(),
  	export_count INTEGER NOT NULL DEFAULT 0,
  	PRIMARY KEY (id)
 );

CREATE INDEX idx_histo_stat_created_at ON histo_stat(created_at);