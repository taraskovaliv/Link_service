package dev.kovaliv.config;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfig {
    @Value("${dev.kovaliv.jdbc.url}")
    private String jdbcUrl;
    @Value("${dev.kovaliv.jdbc.user}")
    private String user;
    @Value("${dev.kovaliv.jdbc.password}")
    private String password;
    @Value("${dev.kovaliv.packages}")
    private String packages;
}
