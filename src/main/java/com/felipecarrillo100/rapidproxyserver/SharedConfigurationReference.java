package com.felipecarrillo100.rapidproxyserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.felipecarrillo100.rapidproxyserver")
class SharedConfigurationReference {
    private static final Logger logger = LoggerFactory.getLogger(SharedConfigurationReference.class);

    @Value("${ogc.proxy.enabled:false}")
    private Boolean enabled;

    @Value("${ogc.proxy.baseurl:/proxy}")
    private String utl;

    @Bean(name="initializeService")
    public void helloWorld() {
        logger.info("** Super proxy has been initialized **");
        if (!enabled) {
            logger.info(" * Proxy currently disabled, to enable set ogc.proxy.enabled to true in your properties file");
        }
    }
}