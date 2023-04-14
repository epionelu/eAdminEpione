package lu.esante.agence.epione.service.impl.document;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.MySecuClient;
import lu.esante.agence.epione.client.mysecu.exception.MySecuInvalidTemplateRequestException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuOperationException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuSignatureException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.cns.TMemoireHonorairesRetour;
import lu.esante.agence.epione.util.DocumentUtils;

@Slf4j
public class DocumentSenderWrapper {

    private MySecuClient client;
    private String assertion;

    public DocumentSenderWrapper(MySecuClient client)
            throws MySecuInvalidTemplateRequestException, MySecuOperationException, MySecuSignatureException {
        this.client = client;
        assertion = client.getAssertion();
    }

    /**
     * Send document to mySecu
     * Returns the MySecuId of the document
     **/
    public String sendMH(Document document) throws MySecuOperationException {
        try {
            String doc = DocumentUtils.getMhFragment(DocumentUtils.convertFromB64(document.getFile()));
            String res = send(doc, "2022_CNS_MEMHON_DEPOT-V1");
            log.debug(res);
            String body = DocumentUtils.extractXmlBody(res);
            TMemoireHonorairesRetour mhRetour = JAXB.unmarshal(new StringReader(body), TMemoireHonorairesRetour.class);
            return mhRetour.getUuidSecu();
        } catch (MySecuOperationException | JAXBException | SAXException | XPathExpressionException
                | IllegalArgumentException | IOException | ParserConfigurationException
                | TransformerFactoryConfigurationError | TransformerException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.error("Failure in MySecuClient, Impossible to send document {}", document.getId());
            throw new MySecuOperationException(e.getMessage());
        }
    }

    public String sendConsent(String mySecuId, String ssn) throws MySecuOperationException {
        try {
            String doc = DocumentUtils.getConsentFragment(mySecuId, ssn);
            return send(doc, "2022_CNS_MEMHON_REMBCONS-V1");
        } catch (MySecuOperationException | JAXBException | SAXException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.error("Failure in MySecuClient, Impossible to send consent");
            throw new MySecuOperationException(e.getMessage());
        }
    }

    public String sendAnnulation(String mySecuId, Optional<String> replacedId, String uuidPrestataire)
            throws MySecuOperationException {
        try {
            String doc = DocumentUtils.getAnnulationFragment(mySecuId, replacedId, uuidPrestataire);
            return send(doc, "2022_CNS_MEMHON_ANNUL-V1");
        } catch (MySecuOperationException | JAXBException | SAXException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.error("Failure in MySecuClient, Impossible to send annulation");
            throw new MySecuOperationException(e.getMessage());
        }
    }

    private String send(String data, String msgId) throws MySecuOperationException {
        try {
            String res = client.sendDocument(data, assertion, msgId);
            log.debug(res);
            return res;
        } catch (MySecuInvalidTemplateRequestException | MySecuOperationException | MySecuSignatureException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new MySecuOperationException(e.getMessage());
        }
    }

}
