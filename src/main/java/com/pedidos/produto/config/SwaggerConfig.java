package com.pedidos.produto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8082}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        Server server = new Server()
                .url("http://localhost:" + serverPort)
                .description("Servidor de Desenvolvimento");

        Contact contact = new Contact()
                .name("Equipe de Desenvolvimento")
                .email("dev@pedidos.com")
                .url("https://github.com/pedidos/produto-service");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Produto Service API")
                .version("1.0.0")
                .description("API para gerenciamento de produtos do sistema de pedidos")
                .termsOfService("https://pedidos.com/termos")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(server));
    }
}