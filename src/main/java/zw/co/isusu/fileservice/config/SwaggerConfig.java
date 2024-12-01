package zw.co.isusu.fileservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "File Upload and Download Service API",
                version = "1.0.0",
                description = "File Upload and Download Service Management System",
                contact = @Contact(
                        name = "Isusu Dev Team",
                        url = "https://isususervices.co.zw",
                        email = "systemsdev@isususervices.co.zw"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://isususervices.co.zw/LICENSE"
                )
        ),
        servers = @Server(url = "http://localhost:5000")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {
}
