ALTER TABLE document
  ADD my_secu_id VARCHAR(100);

ALTER TABLE document_aud
  ADD my_secu_id VARCHAR(100);

INSERT INTO document_status(code, description) VALUES ('REIMBURSEMENT_ASKED', 'La demande de remboursement à été demandée');
INSERT INTO document_status(code, description) VALUES ('REIMBURSEMENT_SENT', 'La demande de remboursement à été envoyée');
INSERT INTO document_status(code, description) VALUES ('CANCEL_ASKED', 'La demande d annulation à été demandée');

UPDATE document_status SET code = 'CANCEL_REPLACED' WHERE code ='CANCEL_REPLACE'; 