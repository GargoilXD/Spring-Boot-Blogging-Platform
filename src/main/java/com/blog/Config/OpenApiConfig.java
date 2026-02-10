package com.blog.Config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Spring Boot Blogging Platform API",
        version = "1.0.0",
        description = "RESTful API for a blogging platform with support for posts, comments, tags, and user authentication. " +
                      "This API provides comprehensive endpoints for managing blog content and user interactions.",
        contact = @Contact(
            name = "API Support",
            email = "dont@contactme.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            description = "Local Development Server",
            url = "http://localhost:8080"
        ),
        @Server(
            description = "Production Server",
            url = "https://api.blogplatform.com"
        )
    }
)
public class OpenApiConfig { }
