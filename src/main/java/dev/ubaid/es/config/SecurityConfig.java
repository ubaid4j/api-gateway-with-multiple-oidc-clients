package dev.ubaid.es.config;

import org.springframework.cloud.gateway.config.HttpClientProperties;
import org.springframework.cloud.gateway.config.HttpClientProperties.Pool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.CsrfSpec;
import org.springframework.security.config.web.server.ServerHttpSecurity.ExceptionHandlingSpec;
import org.springframework.security.oauth2.client.oidc.authentication.ReactiveOidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoderFactory;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Customizer<AuthorizeExchangeSpec> AUTHORIZE_EXCHANGE = (spec) -> spec
        .pathMatchers(HttpMethod.GET, "/login/**", "/static/favicon.ico", "/index.html").permitAll()
        .anyExchange()
        .authenticated();

    private static final Customizer<ExceptionHandlingSpec> EXCEPTION_HANDLING = (spec) -> spec
        .authenticationEntryPoint(new RedirectServerAuthenticationEntryPoint("/index.html"));


    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange(AUTHORIZE_EXCHANGE)
            .oauth2Login(Customizer.withDefaults())
            .exceptionHandling(EXCEPTION_HANDLING)
            .csrf(CsrfSpec::disable)
            .build();
    }

    @Bean
    ReactiveJwtDecoderFactory<ClientRegistration> reactiveJwtDecoderFactory(WebClient webClient) {
        ReactiveOidcIdTokenDecoderFactory reactiveJwtDecoderFactory = new ReactiveOidcIdTokenDecoderFactory();
//        reactiveJwtDecoderFactory.setWebClient(webClient);
        return reactiveJwtDecoderFactory;
    }

    @Bean
    WebClient webClient(HttpClientProperties props) {
        Pool pool = props.getPool();
        ConnectionProvider provider = ConnectionProvider.builder("custom-connection-provider")
            .maxConnections(pool.getMaxConnections())
            .maxIdleTime(pool.getMaxIdleTime())
            .maxLifeTime(pool.getMaxLifeTime())
            .evictInBackground(pool.getEvictionInterval())
            .build();

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
            .build();
    }


}
