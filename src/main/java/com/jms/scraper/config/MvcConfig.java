package com.jms.scraper.config;

/**
 * Created by anon on 30/01/2016.
 */

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.jms.scraper"})
public class MvcConfig {
}
