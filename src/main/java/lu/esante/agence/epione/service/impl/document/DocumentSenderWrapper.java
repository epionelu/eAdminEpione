package lu.esante.agence.epione.service.impl.document;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.MySecuClient;
import lu.esante.agence.epione.client.mysecu.exception.MySecuInvalidTemplateRequestException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuOperationException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuSignatureException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.util.DocumentUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;

@Slf4j
public class DocumentSenderWrapper {

    private MySecuClient client;
    private String assertion;

    public DocumentSenderWrapper(MySecuClient client)
            throws MySecuInvalidTemplateRequestException, MySecuOperationException, MySecuSignatureException {
        this.client = client;
        assertion = client.getAssertion();
    }

    public boolean send(Document document) {
        try {
            String doc = DocumentUtils.getMhFragment(DocumentUtils.convertFromB64(document.getFile()));
            client.sendDocument(doc, assertion);
            return true;
        } catch (MySecuInvalidTemplateRequestException | MySecuOperationException | MySecuSignatureException | UnsupportedEncodingException | JAXBException | SAXException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.info("Failure in MySecuClient, Impossible to send document");
            return false;
        }
    }

}
