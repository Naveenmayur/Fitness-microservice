package com.fitness.gateway;

import com.fitness.gateway.user.RegisterRequest;
import com.fitness.gateway.user.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakUserSyncFilter implements WebFilter {
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange, @NonNull WebFilterChain webFilterChain) {
        String userId = serverWebExchange.getRequest().getHeaders().getFirst("X-User-ID");
        String token = serverWebExchange.getRequest().getHeaders().getFirst("Authorization");
        RegisterRequest registerRequest = getUserDetails(token);

        if(userId == null){
            assert registerRequest != null;
            userId = registerRequest.getKeycloakId();
        }

        if (userId != null && token != null) {
            String finalUserId = userId;
            return userService.validateUser(userId)
                    .flatMap(exist -> {
                        if (!exist) {
                            //Register user
                            if (registerRequest != null) {
                                log.info("Registering new user with email: {}", registerRequest.getEmail());
                                return userService.registerUser(registerRequest)
                                        .then(Mono.empty());
                            } else {
                                log.error("Failed to extract user details from token");

                                return Mono.empty();
                            }
                        } else {
                            log.info("User with id {} already exists in the system", finalUserId);
                            return Mono.empty();
                        }
                    }).then(Mono.defer(() -> {
                        ServerHttpRequest mutatedRequest = serverWebExchange.getRequest().mutate()
                                .header("X-User-ID", finalUserId)
                                .build();
                        return webFilterChain.filter(serverWebExchange.mutate().request(mutatedRequest).build());
                    }));
        }
        return webFilterChain.filter(serverWebExchange);
    }

    private RegisterRequest getUserDetails(String token) {
        try {
            String tokenWithoutBearer = token.split("Bearer ")[1];
            SignedJWT signedJWT = SignedJWT.parse(tokenWithoutBearer);
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setEmail(claimsSet.getStringClaim("email"));
            registerRequest.setFirstname(claimsSet.getStringClaim("given_name"));
            registerRequest.setLastname(claimsSet.getStringClaim("family_name"));
            registerRequest.setKeycloakId(claimsSet.getSubject());
            registerRequest.setPassword("dummyPassword"); // Set a dummy password since it's required but not used for authentication
            return registerRequest;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
