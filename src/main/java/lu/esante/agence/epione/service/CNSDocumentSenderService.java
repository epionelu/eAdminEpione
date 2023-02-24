package lu.esante.agence.epione.service;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.MySecuClient;
import lu.esante.agence.epione.client.mysecu.exception.MySecuInvalidTemplateRequestException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuOperationException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuSignatureException;
import lu.esante.agence.epione.config.EpioneSettings;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.service.impl.document.DocumentSenderWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CNSDocumentSenderService {

    @Autowired
    EpioneSettings settings;

    @Autowired
    IDocumentBatchHelper helper;

    @Autowired
    MySecuClient client;

    @Scheduled(fixedDelayString = "${epione.send.document.delay.in.milliseconds}")
    public void sendDocument() {
        if (!settings.isMySecuActive()) {
            log.info("MySecu is not active");
            return;
        }

        List<Document> list = helper.getAll();
        if (list.isEmpty()) {
            log.info("No document to send");
            return;
        }

        try {
            DocumentSenderWrapper sender = new DocumentSenderWrapper(client);
            for (Document document : list) {
                if (sender.send(document)) {
                    helper.acknowledgeSend(document);
                    log.info("document " + document.getId() + " has been send to the CNS");
                }
            }
        } catch (MySecuInvalidTemplateRequestException | MySecuOperationException | MySecuSignatureException e) {
            log.error("Failure during batch job CNS Transfert");
            e.printStackTrace();
        }

    }
}
