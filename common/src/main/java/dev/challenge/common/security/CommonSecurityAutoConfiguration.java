package dev.challenge.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@AutoConfiguration
@EnableConfigurationProperties(AuthProperties.class)
public class CommonSecurityAutoConfiguration {
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthProperties props) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
       .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
       .authorizeHttpRequests(reg -> reg
         .requestMatchers("/actuator/**","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html", "/error").permitAll()
         .anyRequest().authenticated()
       )
       .addFilterBefore(new StaticTokenAuthFilter(props.staticToken()), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
