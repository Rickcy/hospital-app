package nl.gerimedica.assignment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig
{
    @Bean
    public OpenAPI assignmentOpenAPI()
    {
        return new OpenAPI().info(
                new Info().title("Hospital Appointments API").description("API for management patients and appoitment")
                        .version("v1").contact(new Contact().name("Evgenii Kudenko").email("kuden.and.ko@gmail.com"))
                        .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")));
    }
}
