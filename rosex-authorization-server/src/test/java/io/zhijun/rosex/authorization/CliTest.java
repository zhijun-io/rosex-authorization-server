package io.zhijun.rosex.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CliTest {

	@Test
	void allowsSpringBootPropertyFlags() {
		assertThat(Cli.isPassthroughBootProperty("--spring.ssl.bundle.pem.server.keystore.certificate"))
				.isTrue();
		assertThat(Cli.isPassthroughBootProperty("--server.ssl.client-auth")).isTrue();
		assertThat(Cli.isPassthroughBootProperty("--config")).isFalse();
		assertThat(Cli.checkUnknownFlagsAndPrintError(new String[] {
				"--config=samples/config.yml",
				"--spring.config.additional-location=optional:file:samples/tls/application-tls.yml",
				"--server.ssl.bundle=server"
		})).isFalse();
	}
}
