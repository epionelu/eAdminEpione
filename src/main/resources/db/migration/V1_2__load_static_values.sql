INSERT INTO consent_type(code, description) VALUES ('MH', 'Mémoire d''honoraire');
INSERT INTO consent_type(code, description) VALUES ('CNS', 'Accord pour l''envoi à la CNS');
INSERT INTO consent_type(code, description) VALUES ('ESANTE', 'Accord d''utilisation de la plateforme eSanté');
INSERT INTO consent_type(code, description) VALUES ('CNS_AUTO', 'Accord pour l''envoi automatique à la CNS');


INSERT INTO document_type(code, description) VALUES ('MH', 'Mémoire d''honoraire');

INSERT INTO document_status(code, description) VALUES ('RECEIVED', 'Le document est dans le système');
INSERT INTO document_status(code, description) VALUES ('TO_SEND', 'Le document sera envoyé à la CNS lors du prochain batch si il remplit toutes les conditions.');
INSERT INTO document_status(code, description) VALUES ('SENT', 'Le document à été envoyé à la CNS');
INSERT INTO document_status(code, description) VALUES ('CANCEL', 'Le document à été annulé');
INSERT INTO document_status(code, description) VALUES ('CANCELED', 'Le document à été annulé');
INSERT INTO document_status(code, description) VALUES ('CANCEL_REPLACE', 'Le document à été annulé et remplacé');

