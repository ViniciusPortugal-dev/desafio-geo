package dev.challenge.common.feign;

import dev.challenge.common.replication.ReplicationFlagFilter;
import feign.RequestInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class CommonFeignAutoConfiguration {
    @Bean
    public RequestInterceptor replicationHeader() {
        return template -> template.header(ReplicationFlagFilter.HEADER, "true");
    }
}