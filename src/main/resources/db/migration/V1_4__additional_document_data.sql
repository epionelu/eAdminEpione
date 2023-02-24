ALTER TABLE document
  ADD practitioner_firstname VARCHAR DEFAULT '',
  ADD practitioner_lastname VARCHAR DEFAULT '',
  ADD price float8  DEFAULT 0;

ALTER TABLE document_aud
  ADD practitioner_firstname VARCHAR DEFAULT '',
  ADD practitioner_lastname VARCHAR DEFAULT '',
  ADD price float8  DEFAULT 0;

ALTER TABLE document
  ALTER practitioner_firstname DROP DEFAULT,
  ALTER practitioner_lastname DROP DEFAULT,
  ALTER practitioner_firstname DROP DEFAULT;
  