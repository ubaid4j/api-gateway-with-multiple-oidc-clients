package dev.ubaid.es.resource;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/")
public class IndexResource {

    @GetMapping
    public Mono<OidcUser> userInfo(@AuthenticationPrincipal OidcUser oidcUser) {
        return Mono.just(oidcUser);
    }
}
