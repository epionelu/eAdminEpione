package lu.esante.agence.epione.client.mpi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mpi.config.MpiConfig;
import lu.esante.agence.epione.client.mpi.exception.MpiCriticalException;
import lu.esante.agence.epione.client.mpi.exception.MpiErrorException;
import lu.esante.agence.epione.client.mpi.model.MpiPerson;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class MpiClient {
    @Autowired
    MpiConfig config;

    private static final String RESPONSE_IS_NULL_ERROR = "Mpi response is null";

    public Patient getPatientById(String identifier) throws MpiCriticalException, MpiErrorException {
        Parameters p = buildPatientSearch(identifier);
        UUID uuid = UUID.randomUUID();
        ResponseEntity<String> response = getWebClient().post()
                .uri(config.getEndpoint() + "$match")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders -> {
                    httpHeaders.set("location", config.getLocation());
                    httpHeaders.set("reqid", uuid.toString());
                    httpHeaders.set("secret", config.getSecret());
                    httpHeaders.set("user", identifier);
                })
                .body(Mono.just(serialiseDomainResource(p)), String.class)
                .retrieve()
                .onStatus(
                        status -> status.value() == 500,
                        clientResponse -> Mono.empty())
                .toEntity(String.class)
                .block();
        if (response == null) {
            log.error(RESPONSE_IS_NULL_ERROR);
            throw new MpiCriticalException(RESPONSE_IS_NULL_ERROR);
        }
        if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new MpiErrorException("500 from MPI received");
        }
        String strPatient = response.getBody();
        Bundle patientBundle = deserialiseDomainResource(strPatient, Bundle.class);
        return (Patient) patientBundle.getEntryFirstRep().getResource();
    }

    public MpiPerson getPatientBySsn(String ssn, String userIdentifier) throws MpiCriticalException, MpiErrorException {
        UUID uuid = UUID.randomUUID();
        ResponseEntity<MpiPerson> response = getWebClient().post()
                .uri(config.getEndpoint())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders -> {
                    httpHeaders.set("matricule", ssn);
                    httpHeaders.set("user", userIdentifier);
                    httpHeaders.set("location", config.getLocation());
                    httpHeaders.set("reqID", uuid.toString());
                    httpHeaders.set("secret", config.getSecret());

                })
                .retrieve()
                .toEntity(MpiPerson.class)
                .block();

        if (response == null) {
            log.error(RESPONSE_IS_NULL_ERROR);
            throw new MpiCriticalException(RESPONSE_IS_NULL_ERROR);
        }

        if (response.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
            throw new MpiErrorException("500 from MPI received");
        }

        return response.getBody();

    }

    public boolean isPatient(String identifier) {
        String[] content = identifier.split("\\^");
        return content != null && content.length == 4
                && content[3].equals("&" + config.getPatientIdentifierDomain() + "&ISO");
    }

    public String getSsn(String patientId) throws MpiCriticalException, MpiErrorException {
        Patient patient = getPatientById(patientId);
        if (patient == null) {
            return "";
        }
        Optional<Identifier> identifier = patient.getIdentifier().stream()
                .filter(i -> i.getSystem().equals(config.getSsnDomain()))
                .findFirst();

        if (identifier.isEmpty()) {
            log.error("Patient doesn't have ssn in identifier");
            throw new MpiCriticalException("Patient doesn't have ssn in identifier");
        }

        return identifier.get().getValue();
    }

    private WebClient getWebClient() {
        WebClient.Builder webClientBuilder = WebClient.builder();
        return webClientBuilder.baseUrl(config.getUrl()).build();
    }

    private Parameters buildPatientSearch(String identifierValue) {
        Parameters parameters = new Parameters();
        Parameters.ParametersParameterComponent parameter = new Parameters.ParametersParameterComponent();
        parameter.setName("resource");
        Patient patient = new Patient();
        Identifier identifier = patient.addIdentifier();
        identifier.setSystem("urn:oid:" + config.getPatientIdentifierDomain());
        identifier.setValue(identifierValue);
        parameter.setResource(patient);
        parameters.addParameter(parameter);
        return parameters;
    }

    private <T extends IBaseResource> String serialiseDomainResource(T domainResource) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();// not thread safe hence recreated each time
        return parser.setPrettyPrint(true).encodeResourceToString(domainResource);
    }

    private <T extends IBaseResource> T deserialiseDomainResource(String resource, Class<T> domainResource) {
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser(); // not thread safe hence recreated each time
        return parser.parseResource(domainResource, resource);
    }

}
