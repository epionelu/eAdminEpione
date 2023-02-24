ALTER TABLE document
  ADD memoire_date timestamptz NOT NULL DEFAULT now();

ALTER TABLE document_aud
  ADD memoire_date timestamptz NOT NULL DEFAULT now();