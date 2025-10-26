package dev.challenge.common.configuration;

import dev.challenge.common.replication.ReplicationFlagFilter;
import dev.challenge.common.security.AuthProperties;
import feign.Request;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@Configuration
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
    public Request.Options feignRequestOptions() {
        return new Request.Options(
                10_000,
                60_000
        );
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
                return new RuntimeException("Client error " + status + " at " + methodKey);
            }
            return new ErrorDecoder.Default().decode(methodKey, response);
        };
    }
}
