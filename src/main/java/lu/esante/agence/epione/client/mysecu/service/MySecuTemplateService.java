package lu.esante.agence.epione.client.mysecu.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import lu.esante.agence.epione.client.mysecu.config.MySecuTemplateEnum;

@Service
@Slf4j
public class MySecuTemplateService {

    private Map<MySecuTemplateEnum, String> templates;

    // Load XML templates in memory
    @PostConstruct
    void init() throws IOException {
        log.info("Loading templates in memory");
        templates = new EnumMap<>(MySecuTemplateEnum.class);
        try {
            templates.put(MySecuTemplateEnum.AUTHENTICATION,
                    loadTemplate("templates/Authentification.xml"));
            templates.put(MySecuTemplateEnum.AUTORISATION,
                    loadTemplate("templates/Autorisation.xml"));
            templates.put(MySecuTemplateEnum.BUSINESS,
                    loadTemplate("templates/ServiceMetier.xml"));
            log.info("Templates loaded");
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new IOException("Impossible to load templates");
        }
    }

    private String loadTemplate(String location) throws IOException {
        try (InputStream is = new ClassPathResource(location).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public Optional<String> getTemplate(MySecuTemplateEnum template) {
        return Optional.of(templates.get(template));
    }

}
