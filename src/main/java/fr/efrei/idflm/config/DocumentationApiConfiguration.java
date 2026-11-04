package fr.efrei.idflm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationApiConfiguration {

    private static final String AUTHENTIFICATION_BASIC = "basic";

    @Bean
    public OpenAPI documentationApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API IDFLM")
                        .version("1.0.0")
                        .description("Consultation des incidents et modification de leur état. "
                                + "Authentification HTTP Basic avec un compte de l'application."))
                .components(new Components().addSecuritySchemes(AUTHENTIFICATION_BASIC,
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme(AUTHENTIFICATION_BASIC)))
                .addSecurityItem(new SecurityRequirement().addList(AUTHENTIFICATION_BASIC));
    }
}
