package lu.esante.agence.epione.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "epione")
@Data
@Validated
public class EpioneSettings {
    @Data
    public static class OAuthConfig {
        @NotBlank
        private String audience;

        @NotBlank
        private String issuer;
    }

    @Data
    public static class Security {
        @NotBlank
        private String certificateHeaderName;
        @NotEmpty
        private String[] cnsEHealthIds;
        @NotBlank
        private String certificateKey;
        @NotEmpty
        private String[] adminEHealthIds;
    }

    @NotNull
    private OAuthConfig oauth;

    @NotNull
    private Security security;

    private boolean isMySecuActive = true;

    @NotBlank
    private String cnsFileDirectory;

}
