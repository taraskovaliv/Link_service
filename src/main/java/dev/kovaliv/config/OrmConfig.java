package dev.kovaliv.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static org.springframework.orm.jpa.vendor.Database.POSTGRESQL;

@Configuration
@EnableTransactionManagement
@Import(DatabaseConfig.class)
public class OrmConfig {
    @Bean("dataSource")
    DataSource dataSource(DatabaseConfig databaseConfig) {
        HikariConfig dataSourceProperties = new HikariConfig();
        dataSourceProperties.setJdbcUrl(databaseConfig.getJdbcUrl());
        dataSourceProperties.setUsername(databaseConfig.getUser());
        dataSourceProperties.setPassword(databaseConfig.getPassword());
        dataSourceProperties.setMaximumPoolSize(30);
        dataSourceProperties.setMinimumIdle(5);
        dataSourceProperties.setDriverClassName("org.postgresql.Driver");
        return new HikariDataSource(dataSourceProperties);
    }

    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource,
                                                                       DatabaseConfig databaseConfig) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(POSTGRESQL);
        vendorAdapter.setShowSql(false);
        vendorAdapter.setGenerateDdl(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan(databaseConfig.getPackages());
        factory.setDataSource(dataSource);

        return factory;
    }
    @Bean("transactionManager")
    protected PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
