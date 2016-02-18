package com.jms.scraper.config;

/**
 * Created by anon on 30/01/2016.
 */

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@ComponentScan(basePackages = {"com.jms.scraper"})
public class MvcConfig {
}
