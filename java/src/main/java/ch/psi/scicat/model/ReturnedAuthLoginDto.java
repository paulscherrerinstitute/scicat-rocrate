package ch.psi.scicat.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReturnedAuthLoginDto(
        @JsonProperty("access_token") String accessToken,
        String id,
        @JsonProperty("expires_in") int expiresIn,
        int ttl,
        String created,
        String userId,
        User user) {

    public record User(
            String id,
            String username,
            String email,
            boolean emailVerified,
            String realm,
            String authStrategy) {
    }
}
