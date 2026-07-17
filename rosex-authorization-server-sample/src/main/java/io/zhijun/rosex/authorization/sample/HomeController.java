package io.zhijun.rosex.authorization.sample;

import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home(@AuthenticationPrincipal OidcUser user, Model model) {
		model.addAttribute("authenticated", user != null);
		if (user != null) {
			model.addAttribute("name", user.getFullName() != null ? user.getFullName() : user.getPreferredUsername());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("claims", Map.copyOf(user.getClaims()));
		}
		return "index";
	}
}
