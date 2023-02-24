package lu.esante.agence.epione.client.mpi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "mpi")
@Data
@Validated
public class MpiConfig {

    private String url;
    private String endpoint;
    private String location;
    private String secret;
    private String ssnDomain;
    private String patientIdentifierDomain;
    private int cacheDelay;
}
