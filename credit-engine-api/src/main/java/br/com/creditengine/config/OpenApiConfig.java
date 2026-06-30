package br.com.creditengine.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI creditEngineOpenApi() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Credit Engine API")
                        .version("1.0")
                        .description("API para cessão de crédito multimoedas")
                        .license(new License().name("Credit Engine License").url("https://github.com/JuniorGDev")));
    }
}
