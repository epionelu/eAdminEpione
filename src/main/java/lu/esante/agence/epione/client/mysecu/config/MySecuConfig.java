package lu.esante.agence.epione.client.mysecu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "mysecu")
@Data
@Validated
public class MySecuConfig {

    @Data
    public static class WebProxyProperties {
        @NotBlank
        private String host;
        private int port;
    }

    @Data
    public static class KeystoreProperties {
        @NotBlank
        private String location;
        @NotBlank
        private String alias;
        @NotBlank
        private String password;
    }

    @Data
    public static class ServiceProperties {
        @NotBlank
        private String url;
        @NotBlank
        private String authorizationEndpoint;
        @NotBlank
        private String businessEndpoint;
    }

    private WebProxyProperties webproxy;

    @NotBlank
    private String password;
    @NotBlank
    private String userid;

    @NotNull
    private KeystoreProperties keystore;
    @NotNull
    private KeystoreProperties truststore;
    @NotNull
    private ServiceProperties service;

}