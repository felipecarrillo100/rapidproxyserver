package io.github.felipecarrillo100;

import io.github.felipecarrillo100.controllers.ProxyRequest;
import io.github.felipecarrillo100.controllers.ProxyRequestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("io.github.felipecarrillo100")
class SharedConfigurationReference {
    private static final Logger logger = LoggerFactory.getLogger(SharedConfigurationReference.class);

    @Value("${ogc.proxy.enabled:false}")
    private Boolean enabled;

    @Value("${ogc.proxy.baseurl:/proxy}")
    private String utl;

    @Bean(name="initializeService")
    public void helloWorld() {
        logger.info("** Rapidproxy 1.0.1 has been initialized **");
        if (!ProxyRequestProvider.isSet()) {
            ProxyRequestProvider.setProxyRequest(ProxyRequest.class);
        }
        if (!enabled) {
            logger.info(" * Proxy currently disabled, to enable set ogc.proxy.enabled to true in your properties file");
        }
    }
}