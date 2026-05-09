package com.techchallenge.biblioteca.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca API")
                        .description("API REST para gestao de livros, usuarios, emprestimos e relatorios da biblioteca.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Tech Challenge Biblioteca API")));
    }
}
