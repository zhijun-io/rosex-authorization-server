package io.zhijun.rosex.authorization.oauth2.token;

import io.zhijun.rosex.authorization.authentication.UserAttributesClaimAccessor;
import io.zhijun.rosex.authorization.authentication.CustomUser;
import java.util.Map;
import static org.springframework.security.oauth2.core.oidc.OidcScopes.PHONE;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public class IdTokenPhoneCustomizer extends TokenCustomizer {
    public IdTokenPhoneCustomizer() {
    }

    boolean shouldCustomize(JwtEncodingContext context) {
        return this.isIdToken(context) && this.hasScope(context, PHONE);
    }

    void customizeInternal(JwtEncodingContext context) {
        this.getPrincipal(context)
                .map(CustomUser::getTokenClaims)
                .map(UserAttributesClaimAccessor::getOidcPhoneClaims)
                .orElse(Map.of())
                .forEach((key, value) -> context.getClaims().claim(key, value));
    }
}
