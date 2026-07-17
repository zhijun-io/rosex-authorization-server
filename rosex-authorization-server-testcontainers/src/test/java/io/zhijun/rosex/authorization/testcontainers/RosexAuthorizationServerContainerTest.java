package io.zhijun.rosex.authorization.testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
	}
}
