package lu.esante.agence.epione.client.mysecu.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.InvalidCanonicalizerException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.config.TemplateConstantes;
import lu.esante.agence.epione.client.mysecu.exception.MySecuSignatureException;

@Slf4j
public class MySecuUtils {

    private static final String ALGORITHM_STRING = "SHA-1";

    private MySecuUtils() {
    }

    public static String encodeString(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM_STRING);
            return Base64.getEncoder().encodeToString(md.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            log.error(MessageFormat.format("Algorithm {0} not found. Not encrypting the data.", ALGORITHM_STRING));
            return "";
        }
    }

    public static String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String getCertificateB64ByAlias(KeyStore ks, String alias)
            throws CertificateEncodingException, KeyStoreException {
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        return Base64.getEncoder().encodeToString(cert.getEncoded());
    }

    public static String retrieveAssertionId(String assertion) {
        Pattern pattern = Pattern.compile("wsu:Id=\"\"([^\"\"]*)\"\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(assertion);
        return matcher.matches() ? matcher.group(1) : "";
    }

    public static String signXmlFile(String xml, PrivateKey privateKey, PublicKey publicKey)
            throws MySecuSignatureException {
        log.debug("MySecu - Start signing XML");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document xmlDocument = builder
                    .parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
            XPath xPath = XPathFactory.newInstance().newXPath();

            xPath.setNamespaceContext(new NamespaceContext() {
                @Override
                public String getNamespaceURI(String prefix) {
                    if ("soapenv".equals(prefix)) {
                        return "http://schemas.xmlsoap.org/soap/envelope/";
                    } else if ("saml2".equals(prefix)) {
                        return "urn:oasis:names:tc:SAML:2.0:assertion";
                    } else if ("saml".equals(prefix)) {
                        return "urn:oasis:names:tc:SAML:2.0:assertion";
                    } else if ("saml2p".equals(prefix))
                        return "urn:oasis:names:tc:SAML:2.0:protocol";
                    else if ("wsse".equals(prefix)) {
                        return "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
                    } else if ("wsu".equals(prefix)) {
                        return "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
                    } else if ("ds".equals(prefix)) {
                        return "http://www.w3.org/2000/09/xmldsig#";
                    }

                    return null;
                }

                @Override
                public String getPrefix(String namespaceURI) {
                    return null;
                }

                @Override
                public Iterator<String> getPrefixes(String namespaceURI) {
                    return null;
                }
            });
            XPathExpression expSecurityToken = xPath
                    .compile("/soapenv:Envelope/soapenv:Header/wsse:Security/wsse:BinarySecurityToken");
            XPathExpression expValidityPeriod = xPath
                    .compile("/soapenv:Envelope/soapenv:Header/wsse:Security/wsu:Timestamp");
            XPathExpression expAssertion = xPath
                    .compile("/soapenv:Envelope/soapenv:Header/wsse:Security/saml:Assertion");
            XPathExpression expUserNameToken = xPath
                    .compile("/soapenv:Envelope/soapenv:Header/wsse:Security/wsse:UsernameToken");
            XPathExpression expBody = xPath.compile("/soapenv:Envelope/soapenv:Body");
            XPathExpression expDigestTags = xPath.compile(
                    "/soapenv:Envelope/soapenv:Header/wsse:Security/ds:Signature/ds:SignedInfo/ds:Reference/ds:DigestValue");
            XPathExpression expSignedInfo = xPath
                    .compile("/soapenv:Envelope/soapenv:Header/wsse:Security/ds:Signature/ds:SignedInfo");
            XPathExpression expSignatureValue = xPath
                    .compile("/soapenv:Envelope/soapenv:Header/wsse:Security/ds:Signature/ds:SignatureValue");

            Node securityToken = (Node) expSecurityToken.evaluate(xmlDocument, XPathConstants.NODE);
            Node validityPeriod = (Node) expValidityPeriod.evaluate(xmlDocument, XPathConstants.NODE);
            Node assertion = (Node) expAssertion.evaluate(xmlDocument, XPathConstants.NODE);
            Node body = (Node) expBody.evaluate(xmlDocument, XPathConstants.NODE);
            Node usernameTokem = (Node) expUserNameToken.evaluate(xmlDocument, XPathConstants.NODE);

            NodeList digestTags = (NodeList) expDigestTags.evaluate(xmlDocument, XPathConstants.NODESET);
            String nodeName = null;

            for (int counter = 0; counter < digestTags.getLength(); counter++) {
                nodeName = digestTags.item(counter).getFirstChild().getTextContent();
                if (TemplateConstantes.TEMPLATE_VAR_X509_TIMESTAMP_TAG_DIGEST.equals(nodeName))
                    generateDigest(validityPeriod, digestTags.item(counter), "saml2 soapenv wsse wsu");
                else if (TemplateConstantes.TEMPLATE_VAR_X509_CERTIFICATE_DIGEST.equals(nodeName))
                    generateDigest(securityToken, digestTags.item(counter));
                else if (TemplateConstantes.TEMPLATE_VAR_X509_AUTHNREQUEST_DIGEST.equals(nodeName))
                    generateDigest(body, digestTags.item(counter), "saml2 soapenv wsse wsu");
                else if (TemplateConstantes.TEMPLATE_VAR_X509_ASSERTION_DIGEST.equals(nodeName))
                    generateDigest(assertion, digestTags.item(counter), "ns soapenv wsa wsse wsp wsu");
                else if (TemplateConstantes.TEMPLATE_VAR_X509_AUTHNUSER_DIGEST.equals(nodeName)) {
                    generateDigest(usernameTokem, digestTags.item(counter), "ns soapenv wsse wsu");
                }
            }
            Node signedInfo = (Node) expSignedInfo.evaluate(xmlDocument, XPathConstants.NODE);
            Node signatureValue = (Node) expSignatureValue.evaluate(xmlDocument, XPathConstants.NODE);
            Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
            byte[] contentToSign = canon.canonicalizeSubtree(signedInfo, "saml2 soapenv");

            byte[] signedData = hashAndSignBytes(privateKey, contentToSign);
            if (verifySignedHash(publicKey, contentToSign, signedData)) {
                signatureValue.getFirstChild().setTextContent(Base64.getEncoder().encodeToString(signedData));
            }

            DOMSource domSource = new DOMSource(xmlDocument);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            Properties oFormat = new Properties();
            oFormat.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperties(oFormat);
            transformer.transform(domSource, result);
            log.debug("MySecu - Finish signing XML");
            return writer.getBuffer().toString();
        } catch (ParserConfigurationException | SAXException | XPathExpressionException | InvalidCanonicalizerException
                | CanonicalizationException | NoSuchAlgorithmException | InvalidKeyException | SignatureException
                | TransformerException | IOException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.error("Impossible to sign the xml document");
            throw new MySecuSignatureException(e.getMessage());
        }

    }

    private static byte[] hashAndSignBytes(PrivateKey privateKey, byte[] dataToSign)
            throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(dataToSign);
        return privateSignature.sign();
    }

    private static boolean verifySignedHash(PublicKey publicKey, byte[] dataToVerify, byte[] signedData)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(dataToVerify);
        return publicSignature.verify(signedData);
    }

    private static void generateDigest(Node contentToDigest, Node digestValueLocation, String inclusiveNamespaces)
            throws InvalidCanonicalizerException, CanonicalizationException, NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        org.apache.xml.security.Init.init();
        Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        byte[] canonicalizedNode = canon.canonicalizeSubtree(contentToDigest, inclusiveNamespaces);
        hash.update(canonicalizedNode);
        byte[] digestValue = hash.digest();
        digestValueLocation.getFirstChild().setTextContent(Base64.getEncoder().encodeToString(digestValue));
    }

    private static void generateDigest(Node contentToDigest, Node digestValueLocation)
            throws InvalidCanonicalizerException, NoSuchAlgorithmException, CanonicalizationException {
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        org.apache.xml.security.Init.init();
        Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        byte[] canonicalizedNode = canon.canonicalizeSubtree(contentToDigest);
        hash.update(canonicalizedNode);

        byte[] digestValue = hash.digest();
        digestValueLocation.getFirstChild().setTextContent(Base64.getEncoder().encodeToString(digestValue));
    }

}
