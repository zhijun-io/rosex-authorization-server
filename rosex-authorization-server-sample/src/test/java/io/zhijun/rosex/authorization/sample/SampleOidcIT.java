package io.zhijun.rosex.authorization.sample;

import io.zhijun.rosex.authorization.testcontainers.RosexAuthorizationServerContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SampleOidcIT {

	@Container
	static RosexAuthorizationServerContainer authServer = new RosexAuthorizationServerContainer();

	@DynamicPropertySource
	static void oauth2Properties(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.client.provider.rosex-authorization-server.issuer-uri",
				authServer::getIssuerUri);
	}

	@Value("${spring.security.oauth2.client.provider.rosex-authorization-server.issuer-uri}")
	String issuerUri;

	@Test
	void authorizationServerExposesOpenIdConfiguration() {
		ResponseEntity<String> response = RestClient.create()
				.get()
				.uri(authServer.getOpenIdConfigurationUri())
				.retrieve()
				.toEntity(String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains(issuerUri);
		assertThat(response.getBody()).contains("authorization_endpoint");
	}
}
