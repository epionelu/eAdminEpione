package lu.esante.agence.epione.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.MySecuClient;
import lu.esante.agence.epione.client.mysecu.exception.MySecuInvalidTemplateRequestException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuOperationException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuSignatureException;
import lu.esante.agence.epione.config.EpioneSettings;
import lu.esante.agence.epione.exception.IllegalOperationException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.service.impl.document.DocumentSenderWrapper;

@Service
@Slf4j
public class CNSDocumentSenderService {

    @Autowired
    EpioneSettings settings;

    @Autowired
    IDocumentBatchHelper helper;

    @Autowired
    MySecuClient client;

    DocumentSenderWrapper sender = null;

    @Scheduled(fixedDelayString = "${epione.send.document.delay.in.milliseconds}")
    public void jobWrapper() {
        if (!settings.getIsMySecuActive()) {
            log.info("MySecu is not active - Job is ignored");
            return;
        }

        sendDocumentsToCNS();
        sendReimbursementsAskToCNS();
        cancelDocuments();

    }

    private void sendDocumentsToCNS() {
        List<Document> list = helper.getAllReceived();
        if (list.isEmpty()) {
            log.info("No document to send as RECEIVED");
            return;
        }

        try {
            sender = new DocumentSenderWrapper(client);
        } catch (MySecuInvalidTemplateRequestException | MySecuOperationException | MySecuSignatureException e) {
            log.error("Failure during assertion aquisition");
            e.printStackTrace();
            return;
        }

        log.info("{} document(s) to send", list.size());
        int errorCounter = 0;
        for (Document document : list) {
            if (!sendDocument(document)) {
                errorCounter++;
            }
        }

        log.info("Send all documents batch completed with {} errors", errorCounter);
    }

    private boolean sendDocument(Document doc) {
        try {
            String mySecuId = sender.sendMH(doc);
            doc = helper.acknowledgeSend(doc, mySecuId);
            log.info("document " + doc.getId() + " has been send to the CNS");
            return true;
        } catch (MySecuOperationException | IllegalOperationException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    private void sendReimbursementsAskToCNS() {
        List<Document> list = helper.getAllReimbursementAsked();
        if (list.isEmpty()) {
            log.info("No document to send as REIMBURSEMENT_ASKED");
            return;
        }

        try {
            sender = new DocumentSenderWrapper(client);
        } catch (MySecuInvalidTemplateRequestException | MySecuOperationException | MySecuSignatureException e) {
            log.error("Failure during assertion aquisition");
            e.printStackTrace();
            return;
        }

        int errorCounter = 0;
        log.info("{} document(s) to consent", list.size());
        for (Document document : list) {
            if (!sendReimbursementAskToCNS(document)) {
                errorCounter++;
            }
        }

        log.info("Send all reimbursement inquery batch completed with {} errors", errorCounter);
    }

    private boolean sendReimbursementAskToCNS(Document doc) {
        try {
            sender.sendConsent(doc.getMySecuId(), doc.getSsn());
            doc.setDocumentStatus(DocumentStatus.REIMBURSEMENT_SENT);
            helper.saveHelper(doc);
            return true;
        } catch (MySecuOperationException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

    private void cancelDocuments() {
        List<Document> list = helper.getAllCancelationAsked();
        if (list.isEmpty()) {
            log.info("No document to send as CANCELLED");
            return;
        }

        try {
            sender = new DocumentSenderWrapper(client);
        } catch (MySecuInvalidTemplateRequestException | MySecuOperationException | MySecuSignatureException e) {
            log.error("Failure during assertion aquisition");
            e.printStackTrace();
            return;
        }

        int errorCounter = 0;
        log.info("{} document(s) to cancel", list.size());
        for (Document document : list) {
            if (!cancelDocument(document)) {
                errorCounter++;
            }
        }

        log.info("Cancel documents batch completed with {} errors", errorCounter);
    }

    private boolean cancelDocument(Document doc) {
        log.info("Cancelling document " + doc.getId().toString());
        try {
            if (doc.getMySecuId() != null) {
                String prestataireUUID = UUID.randomUUID().toString();
                if (doc.getReplacedBy() != null) {
                    sender.sendAnnulation(doc.getMySecuId(), Optional.of(doc.getReplacedBy().toString()),
                            prestataireUUID);
                    doc.setDocumentStatus(DocumentStatus.CANCEL_REPLACED);
                } else {
                    sender.sendAnnulation(doc.getMySecuId(), Optional.empty(), prestataireUUID);
                    doc.setDocumentStatus(DocumentStatus.CANCELED);
                }
            } else {
                if (doc.getReplacedBy() != null) {
                    doc.setDocumentStatus(DocumentStatus.CANCEL_REPLACED);
                } else {
                    doc.setDocumentStatus(DocumentStatus.CANCELED);
                }
            }

            helper.saveHelper(doc);
            return true;

        } catch (MySecuOperationException ex) {
            log.error(ex.getMessage(), ex);
            return false;
        }
    }

}
