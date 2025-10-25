package dev.challenge.common.configuration;

import dev.challenge.common.replication.ReplicationFlagFilter;
import dev.challenge.common.security.AuthProperties;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;


public class ReplicationFeignConfig {

    private final String token;

    public ReplicationFeignConfig(AuthProperties props) {
        this.token = props.staticToken();
    }

    @Bean
    public RequestInterceptor staticBearerInterceptor() {
        return template -> template.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    @Bean
    public RequestInterceptor replicationFlagInterceptor() {
        return template -> template.header(ReplicationFlagFilter.HEADER, "true");
    }

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(200, 2000, 3);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            if (status >= 400 && status < 500) {
                return new RuntimeException("Erro de cliente " + status + " em " + methodKey);
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
