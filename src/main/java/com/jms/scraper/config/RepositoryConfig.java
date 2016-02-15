package com.jms.scraper.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Created by anon on 30/01/2016.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.jms.scraper.repository")
@EnableAutoConfiguration
@EntityScan(basePackages = {"com.jms.scraper.model"})
public class RepositoryConfig {
}