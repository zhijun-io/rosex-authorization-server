package io.zhijun.rosex.authorization.oauth2.token;

import io.zhijun.rosex.authorization.authentication.CustomUser;
import io.zhijun.rosex.authorization.authentication.UserAttributesClaimAccessor;
import java.util.Map;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class IdTokenAddressCustomizer extends TokenCustomizer {
    public IdTokenAddressCustomizer() {
    }

    boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && this.hasScope(context, OidcScopes.ADDRESS);
    }

    void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getOidcAddressClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}