package dev.challenge.common.configuration;

import dev.challenge.common.replication.ReplicationFlagFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class ReplicationFilterConfig {

    @Bean
    public FilterRegistrationBean<ReplicationFlagFilter> replicationFlagFilter() {
        FilterRegistrationBean<ReplicationFlagFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new ReplicationFlagFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
