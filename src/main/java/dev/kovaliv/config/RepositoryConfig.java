package dev.kovaliv.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan("dev.kovaliv.data")
@EnableJpaRepositories("dev.kovaliv.data")
@Import(OrmConfig.class)
public class RepositoryConfig {

}
