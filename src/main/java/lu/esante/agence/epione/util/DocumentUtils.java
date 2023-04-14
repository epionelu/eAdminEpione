package lu.esante.agence.epione.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lu.esante.agence.epione.exception.BadRequestException;
import lu.esante.agence.epione.model.Document;
import lu.esante.agence.epione.model.DocumentStatus;
import lu.esante.agence.epione.model.DocumentType;
import lu.esante.agence.epione.model.cns.TAnnulationMemoireHonoraires;
import lu.esante.agence.epione.model.cns.TConsentement;
import lu.esante.agence.epione.model.cns.TMemoireHonoraires;

public class DocumentUtils {

    private DocumentUtils() {
        throw new IllegalStateException();
    }

    private static final String CNS_FILE_PATTERN = "F90\\d{6}[2-9]\\d{3}[0-1]\\d_MED_MHM_\\d{3}_[0-3]\\d01_[\\w]{8}-[\\w]{4}-[\\w]{4}-[\\w]{4}-[\\w]{12}.xml";
    private static final String XSD_PATH = "xsd/2022_CNS_MEMHON_DEPOT-REQ-V1.xsd";
    private static Schema validationSchema;

    public static Optional<Document> parseZipFile(byte[] zipFile, String eHealthId, String author)
            throws BadRequestException, IOException, JAXBException, SAXException {
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipFile));
        ZipEntry entry;
        TMemoireHonoraires res = null;
        int docCounter = 0;
        while ((entry = zis.getNextEntry()) != null) {
            docCounter++;
            if (docCounter > 1) {
                throw new BadRequestException("More than one file in ZIP.");
            }
            if (!validateFileName(entry.getName())) {
                throw new BadRequestException("The file name doesn't match the expected standards.");
            }
            ByteArrayInputStream is = null;

            try (ByteArrayOutputStream os = new ByteArrayOutputStream(2048)) {
                byte[] buffer = new byte[2048];
                int len = 0;
                while ((len = zis.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }

                byte[] file = os.toByteArray();
                is = new ByteArrayInputStream(file);
                res = getMemoireHonoraireFromByteArray(is);
                return buildDocumentFromMemoire(res, eHealthId, author, file);
            } finally {
                if (is != null)
                    is.close();
            }

        }

        return Optional.empty();

    }

    public static boolean validateFileName(String filename) {
        return Pattern.compile(CNS_FILE_PATTERN).matcher(filename).matches();
    }

    private static Schema getXSDSchema() throws SAXException {
        if (validationSchema != null) {
            return validationSchema;
        }

        InputStream is = DocumentUtils.class.getClassLoader().getResourceAsStream(XSD_PATH);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source schemaFile = new StreamSource(is);
        return factory.newSchema(schemaFile);
    }

    public static byte[] streamToByteArray(InputStream stream) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = stream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();

    }

    private static Optional<Document> buildDocumentFromMemoire(TMemoireHonoraires mem, String eHealthId, String author,
            byte[] file) {
        Document res = new Document();

        if (eHealthId == null || author == null || mem == null || file == null) {
            return Optional.empty();
        }

        res.setFileId(UUID.fromString(mem.getEntete().getUUID()));
        res.setAuthor(author);
        res.setMhNumber(mem.getEntete().getNumeroMemoireHonoraires());
        res.setEHealthId(eHealthId);
        res.setCreatedAt(OffsetDateTime.now());
        res.setSsn(mem.getEntete().getPatient().getMatricule().toString());
        res.setPaid(mem.getPaiement().isAcquitte());
        res.setDocumentStatus(DocumentStatus.RECEIVED);
        res.setDocumentType(DocumentType.MH);
        res.setGpCode(String.valueOf(mem.getEntete().getFacturier().getCodePrestataire()));
        res.setFile(Base64.getEncoder().encodeToString(file).getBytes());
        res.setPractitionerFirstname(mem.getEntete().getFacturier().getPrenom());
        res.setPractitionerLastname(mem.getEntete().getFacturier().getNom());
        res.setPrice(mem.getPaiement().getSousTotal().floatValue());
        res.setMemoireDate(Utils.offsetDateTimeFromXmlGregorianCalendar(mem.getEntete().getDateMemoireHonoraires()));

        return Optional.of(res);
    }

    public static Optional<Byte[]> getPdpFromDocument(Document doc)
            throws JAXBException, SAXException, UnsupportedEncodingException {

        if (doc == null) {
            throw new BadRequestException("Empty document");
        }
        byte[] file = doc.getFile();
        if (file == null) {
            throw new BadRequestException("Empty file in document");
        }
        ByteArrayInputStream input = new ByteArrayInputStream(convertFromB64(file));
        TMemoireHonoraires memoire = getMemoireHonoraireFromByteArray(input);
        byte[] pdf = memoire.getDocument();
        return Optional.of(Utils.getBytes(pdf));
    }

    private static TMemoireHonoraires getMemoireHonoraireFromByteArray(ByteArrayInputStream memoire)
            throws JAXBException, SAXException {
        TMemoireHonoraires res = null;
        JAXBContext context = JAXBContext.newInstance(TMemoireHonoraires.class);
        ByteArrayInputStream input = memoire;
        Source source = new StreamSource(input);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        // unmarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        unmarshaller.setSchema(getXSDSchema());
        JAXBElement<TMemoireHonoraires> root = unmarshaller.unmarshal(source, TMemoireHonoraires.class);
        res = root.getValue();
        return res;
    }

    public static byte[] convertFromB64(byte[] source) throws UnsupportedEncodingException {
        return Base64.getDecoder().decode(new String(source).getBytes(StandardCharsets.UTF_8.name()));
    }

    public static String getMhFragment(byte[] file) throws JAXBException, SAXException {
        ByteArrayInputStream input = new ByteArrayInputStream(file);
        TMemoireHonoraires memoire = getMemoireHonoraireFromByteArray(input);
        return toFragment(memoire, "memoireHonoraire");
    }

    public static String getConsentFragment(String mySecuId, String ssn) throws JAXBException, SAXException {
        TConsentement data = new TConsentement();
        data.setIdenitifiantUnique(ssn);
        data.setUUIDSecu(mySecuId);
        return toFragment(data, "consentement");
    }

    public static String getAnnulationFragment(String mySecuId, Optional<String> replacerId, String uuidPrestataire)
            throws JAXBException, SAXException {
        TAnnulationMemoireHonoraires data = new TAnnulationMemoireHonoraires();
        data.setUUIDDocumentAnnule(mySecuId);
        data.setUUIDPrestataire(uuidPrestataire);
        data.setMotif("Not Implemented");
        if (replacerId.isEmpty()) {
            data.setTypeAnnulation(1);
        } else {
            data.setTypeAnnulation(2);
            data.setUUIDDocumentRemplacant(replacerId.get());
        }

        return toFragment(data, "annulationMemoireHonoraire");
    }

    public static <T1> String toFragment(T1 xmlObject, String qname) throws JAXBException, SAXException {
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(xmlObject.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        JAXBElement<T1> jaxbElement = new JAXBElement<>(
                new QName("http://www.secu.lu/ciss/cns", qname, "ms2"),
                (Class<T1>) xmlObject.getClass(),
                xmlObject);
        jaxbMarshaller.marshal(jaxbElement, sw);
        return sw.toString();
    }

    public static String extractXmlBody(String xml)
            throws SAXException, IOException, XPathExpressionException, ParserConfigurationException,
            IllegalArgumentException, TransformerFactoryConfigurationError, TransformerException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new InputSource(new StringReader(xml)));
        removeTextNodes(doc);
        Node body = doc.getLastChild().getLastChild().getFirstChild().getLastChild();
        return nodeToString(body);
    }

    private static void removeTextNodes(org.w3c.dom.Document doc) throws XPathExpressionException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        // XPath to find empty text nodes.
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
                "//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);
        // Remove each empty text node from document.
        for (int i = 0; i < emptyTextNodes.getLength(); i++) {
            Node emptyTextNode = emptyTextNodes.item(i);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
    }

    private static String nodeToString(Node node)
            throws TransformerFactoryConfigurationError, IllegalArgumentException, TransformerException {
        StringWriter sw = new StringWriter();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));

        return sw.toString();
    }

}
