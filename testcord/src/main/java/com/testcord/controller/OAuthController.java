package com.testcord.controller;

import com.testcord.model.OAuthAccessToken;
import com.testcord.model.OAuthClient;
import com.testcord.model.User;
import com.testcord.service.OAuthService;
import com.testcord.service.SnowflakeService;
import com.testcord.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/oauth2")
public class OAuthController {

    private final OAuthService oAuthService;
    private final UserService userService;
    private final SnowflakeService snowflakeService;

    public OAuthController(OAuthService oAuthService, UserService userService,
                            SnowflakeService snowflakeService) {
        this.oAuthService = oAuthService;
        this.userService = userService;
        this.snowflakeService = snowflakeService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<?> authorize(@RequestParam("client_id") String clientId,
                                        @RequestParam("redirect_uri") String redirectUri,
                                        @RequestParam(value = "scope", defaultValue = "identify guilds") String scope,
                                        @RequestParam(value = "response_type", defaultValue = "code") String responseType,
                                        @RequestParam(value = "state", required = false) String state,
                                        @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(302)
                    .location(URI.create("/dashboard/login?redirect=/oauth2/authorize?client_id=" + clientId))
                    .build();
        }

        Optional<OAuthClient> client = oAuthService.findClient(clientId);
        if (client.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_client"));
        }

        String code = oAuthService.generateAuthCode(user.getId(), clientId, scope);
        String location = redirectUri + "?code=" + code + (state != null ? "&state=" + state : "");
        return ResponseEntity.status(302).location(URI.create(location)).build();
    }

    @PostMapping("/token")
    public ResponseEntity<?> exchangeToken(@RequestParam("grant_type") String grantType,
                                            @RequestParam("code") String code,
                                            @RequestParam("client_id") String clientId,
                                            @RequestParam("client_secret") String clientSecret,
                                            @RequestParam(value = "redirect_uri", required = false) String redirectUri) {
        if (!"authorization_code".equals(grantType)) {
            return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
        }

        Optional<OAuthAccessToken> token = oAuthService.exchangeCode(code, clientId, clientSecret);
        if (token.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("error", "invalid_grant"));
        }

        OAuthAccessToken t = token.get();
        return ResponseEntity.ok(Map.of(
                "access_token", t.getAccessToken(),
                "token_type", "Bearer",
                "expires_in", 604800,
                "refresh_token", t.getRefreshToken(),
                "scope", t.getScope()
        ));
    }

    @PostMapping("/applications/register")
    public ResponseEntity<?> registerApp(@RequestBody Map<String, String> body,
                                          @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        String name = body.get("name");
        String redirectUri = body.get("redirect_uri");
        if (name == null || redirectUri == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "name and redirect_uri required"));
        }
        OAuthClient client = oAuthService.registerClient(name, redirectUri, user.getId(), snowflakeService);
        return ResponseEntity.ok(Map.of(
                "client_id", client.getClientId(),
                "client_secret", client.getClientSecret(),
                "name", client.getName(),
                "redirect_uri", client.getRedirectUri()
        ));
    }
}
