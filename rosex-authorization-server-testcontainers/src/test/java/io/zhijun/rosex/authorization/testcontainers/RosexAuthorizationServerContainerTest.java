package io.zhijun.rosex.authorization.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.testcontainers.utility.MountableFile;

class RosexAuthorizationServerContainerTest {

	@Test
	void defaultsMatchPublishedImageContract() {
		RosexAuthorizationServerContainer container = new RosexAuthorizationServerContainer();
		assertThat(RosexAuthorizationServerContainer.DEFAULT_IMAGE_NAME)
				.isEqualTo("ghcr.io/zhijun-io/rosex-authorization-server:latest");
		assertThat(RosexAuthorizationServerContainer.PORT).isEqualTo(9000);
		assertThat(RosexAuthorizationServerContainer.DEFAULT_CLIENT_ID).isEqualTo("default-client-id");
		assertThat(RosexAuthorizationServerContainer.DEFAULT_CLIENT_SECRET).isEqualTo("default-client-secret");
		assertThat(container.getExposedPorts()).contains(9000);
		assertThat(container.isHttps()).isFalse();
	}

	@Test
	void withConfigAcceptsMountableFile() {
		RosexAuthorizationServerContainer container = new RosexAuthorizationServerContainer()
				.withConfig(MountableFile.forClasspathResource("tc-config.yml"));
		assertThat(container.getCommandParts()).contains("--config=/config/config.yml");
	}

	@Test
	void oauth2ClientPropertiesIncludeIssuerAndDefaultClient() {
		Map<String, String> properties = new RosexAuthorizationServerContainer()
				.oauth2ClientProperties("demo", "http://127.0.0.1:9000");
		assertThat(properties).containsEntry(
				"spring.security.oauth2.client.provider.demo.issuer-uri", "http://127.0.0.1:9000");
		assertThat(properties.get("spring.security.oauth2.client.registration.demo.client-id"))
				.isEqualTo(RosexAuthorizationServerContainer.DEFAULT_CLIENT_ID);
		assertThat(properties.get("spring.security.oauth2.client.registration.demo.client-secret"))
				.isEqualTo(RosexAuthorizationServerContainer.DEFAULT_CLIENT_SECRET);
	}
}
