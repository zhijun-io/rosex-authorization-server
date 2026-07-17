package io.zhijun.rosex.authorization.oauth2.token;

import io.zhijun.rosex.authorization.authentication.UserAttributesClaimAccessor;
import io.zhijun.rosex.authorization.authentication.CustomUser;
import java.util.Map;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class IdTokenEmailCustomizer extends TokenCustomizer {
    public IdTokenEmailCustomizer() {
    }

    boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && this.hasScope(context, OidcScopes.EMAIL);
    }

    void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getOidcEmailClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}