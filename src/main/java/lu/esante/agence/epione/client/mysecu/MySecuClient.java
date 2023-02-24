package lu.esante.agence.epione.client.mysecu;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.config.MySecuConfig;
import lu.esante.agence.epione.client.mysecu.config.MySecuTemplateEnum;
import lu.esante.agence.epione.client.mysecu.config.TemplateConstantes;
import lu.esante.agence.epione.client.mysecu.exception.MySecuClientInitializationException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuInvalidTemplateRequestException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuOperationException;
import lu.esante.agence.epione.client.mysecu.exception.MySecuSignatureException;
import lu.esante.agence.epione.client.mysecu.service.MySecuTemplateService;
import lu.esante.agence.epione.client.mysecu.util.MySecuUtils;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MySecuClient {

    @Autowired
    MySecuConfig config;

    @Autowired
    MySecuTemplateService helper;

    private SSLContext sslContext;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private String certificateB64;

    HostnameVerifier allHostsValid = (String hostname, SSLSession session) -> true;

    @PostConstruct
    void init() throws Exception {
        log.info("MySecu - Initializing Client");

        try {
            log.info("MySecu - Loading keystore");
            KeyStore keystore = loadKeystore(config.getKeystore().getLocation(), config.getKeystore().getPassword());
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keystore,
                    config.getKeystore().getPassword().toCharArray());
            KeyManager[] kms = kmf.getKeyManagers();
            log.info("MySecu - Keystore loaded");

            log.info("MySecu - Loading truststore");
            KeyStore truststore = loadKeystore(config.getTruststore().getLocation(),
                    config.getTruststore().getPassword());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(truststore);
            TrustManager[] tms = tmf.getTrustManagers();
            log.info("MySecu - Truststore loaded");

            log.info("MySecu - Initiate SSL Context");
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kms, tms, new SecureRandom());
            log.info("MySecu - SSL Context initiatied");

            log.info("MySecu - Loading keys");
            privateKey = ((KeyStore.PrivateKeyEntry) keystore
                    .getEntry(
                            config.getKeystore().getAlias(),
                            new KeyStore.PasswordProtection(config.getKeystore().getPassword().toCharArray())))
                    .getPrivateKey();

            publicKey = keystore.getCertificate(config.getKeystore().getAlias()).getPublicKey();
            log.info("MySecu - Loading keys completed");

            log.info("MySecu - Loading certificate");
            certificateB64 = MySecuUtils.getCertificateB64ByAlias(keystore, config.getKeystore().getAlias());
            log.info("MySecu - Certificate loaded");

        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException
                | IOException | KeyManagementException e) {
            e.printStackTrace();
            throw new MySecuClientInitializationException(e.getMessage());
        }

    }

    public String sendDocument(String doc, String assertion)
            throws MySecuInvalidTemplateRequestException, MySecuOperationException, MySecuSignatureException {
        String envelope;
        envelope = generateBusinessXmlRequest(doc, assertion);
        return callWebService(envelope, config.getService().getBusinessEndpoint());

    }

    public String getAssertion()
            throws MySecuInvalidTemplateRequestException, MySecuOperationException, MySecuSignatureException {
        String envelope;
        envelope = generateAuthorizationXmlRequest();
        String res = callWebService(envelope, config.getService().getAuthorizationEndpoint());
        Pattern pattern = Pattern.compile("(<wsse:BinarySecurityToken[.\\S\\s]*</wsse:BinarySecurityToken>)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(res);
        return matcher.find() ? matcher.group(1) : "";

    }

    private static KeyStore loadKeystore(String location, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException {
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream(location), password.toCharArray());
        return clientStore;
    }

    private static String extractOperationResult(BufferedInputStream bis) throws MySecuOperationException {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            int result2 = bis.read();
            while (result2 != -1) {
                buf.write((byte) result2);
                result2 = bis.read();
            }
            return buf.toString();
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.error("MySecu - Impossible to read response");
            throw new MySecuOperationException(e.getMessage());
        }
    }

    private String generateAuthorizationXmlRequest()
            throws MySecuInvalidTemplateRequestException, MySecuSignatureException {
        Optional<String> optTemplate = helper.getTemplate(MySecuTemplateEnum.AUTORISATION);
        if (optTemplate.isEmpty()) {
            throw new MySecuInvalidTemplateRequestException(
                    MessageFormatter.format("Template for {0} not found in MySecuTemplateService",
                            MySecuTemplateEnum.AUTORISATION.toString()).toString());
        }
        String template = optTemplate.get()
                .replace(TemplateConstantes.TEMPLATE_VAR_Autorisation_EndpointReferenceAddress,
                        config.getService().getUrl() + config.getService().getBusinessEndpoint())
                .replace(TemplateConstantes.TEMPLATE_VAR_Autorisation_User, config.getUserid())
                .replace(TemplateConstantes.TEMPLATE_VAR_Autorisation_Password,
                        MySecuUtils.encodeString(config.getPassword()))
                .replace(TemplateConstantes.TEMPLATE_VAR_Autorisation_Nonce, MySecuUtils.generateNonce())
                .replace(TemplateConstantes.TEMPLATE_VAR_Autorisation_CreatedAt, Instant.now().toString())
                .replace(TemplateConstantes.TEMPLATE_VAR_X509_CERT_AS_BASE64, certificateB64);

        return MySecuUtils.signXmlFile(template, privateKey, publicKey);
    }

    private String generateBusinessXmlRequest(String doc, String assertion)
            throws MySecuInvalidTemplateRequestException, MySecuSignatureException {
        Optional<String> optTemplate = helper.getTemplate(MySecuTemplateEnum.BUSINESS);
        if (optTemplate.isEmpty()) {
            throw new MySecuInvalidTemplateRequestException(
                    MessageFormatter.format("Template for {0} not found in MySecuTemplateService",
                            MySecuTemplateEnum.BUSINESS.toString()).toString());
        }
        String template = optTemplate.get()
                .replace(TemplateConstantes.TEMPLATE_VAR_ServiceMetier_BinarySecurityToken, assertion)
                .replace(TemplateConstantes.TEMPLATE_VAR_ServiceMetier_MH, doc)
                .replace(TemplateConstantes.TEMPLATE_VAR_ServiceMetier_IdBinarySecurityToken,
                        MySecuUtils.retrieveAssertionId(assertion))
                .replace(TemplateConstantes.TEMPLATE_VAR_X509_CERT_AS_BASE64, certificateB64);
        return MySecuUtils.signXmlFile(template, privateKey, publicKey);
    }

    private String callWebService(String soapEnvelope, String operationPath) throws MySecuOperationException {
        log.debug(soapEnvelope);
        Proxy proxy = null;
        if (config.getWebproxy() != null && config.getWebproxy().getPort() != 0
                && !config.getWebproxy().getHost().isEmpty()) {
            InetSocketAddress proxyInet = new InetSocketAddress(config.getWebproxy().getHost(),
                    config.getWebproxy().getPort());
            proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
        }

        log.debug("MySecu - Start sending data");
        HttpsURLConnection.setFollowRedirects(false);

        HttpsURLConnection con;
        try {
            URL url = new URL(config.getService().getUrl() + operationPath);
            if (proxy != null) {
                con = (HttpsURLConnection) url.openConnection(proxy);
            } else {
                con = (HttpsURLConnection) url.openConnection();
            }

            con.setSSLSocketFactory(sslContext.getSocketFactory());
            con.setHostnameVerifier(allHostsValid);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "text/xml;charset=\"UTF-8\"");
            con.setRequestProperty("Accept", "text/xml");

            con.setRequestProperty("SOAPAction", "");
            con.setFixedLengthStreamingMode(soapEnvelope.getBytes(StandardCharsets.UTF_8).length);
            con.setDoInput(true);
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
            osw.write(soapEnvelope);
            osw.flush();
            osw.close();
            os.close();
            con.connect();
            log.debug("MySecu - Data transfert completed");

            log.debug("MySecu - Start checking the response");
            BufferedInputStream errorStream = null;
            BufferedInputStream responseStream = null;
            try {

                int rcClassifier = con.getResponseCode() / 100;
                if (rcClassifier < 2 || rcClassifier > 3) {
                    errorStream = new BufferedInputStream(con.getErrorStream());
                    log.error(
                            MessageFormatter.format("MySecu respond with code {0}", con.getResponseCode()).toString());
                    throw new MySecuOperationException(extractOperationResult(errorStream));
                }
                responseStream = new BufferedInputStream(con.getInputStream());
                log.debug(MessageFormatter.format("MySecu respond with code {0}", con.getResponseCode()).toString());
                return extractOperationResult(responseStream);
            } finally {
                if (responseStream != null) {
                    responseStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
                con.disconnect();
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            log.error("MySecu - Impossible to open connection");
            throw new MySecuOperationException(e.getMessage());
        }

    }

}
