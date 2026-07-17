package io.zhijun.rosex.authorization.testcontainers;

import java.nio.file.Path;
import java.util.Objects;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * Testcontainers wrapper for {@code ghcr.io/zhijun-io/rosex-authorization-server}.
 *
 * <p>Defaults match the bundled Authorization Server: port {@code 9000}, health at
 * {@code /actuator/health}, and the default client credentials.
 */
public class RosexAuthorizationServerContainer extends GenericContainer<RosexAuthorizationServerContainer> {

	public static final String DEFAULT_IMAGE_NAME = "ghcr.io/zhijun-io/rosex-authorization-server:latest";

	public static final int PORT = 9000;

	public static final String DEFAULT_CLIENT_ID = "default-client-id";

	public static final String DEFAULT_CLIENT_SECRET = "default-client-secret";

	public static final String DEFAULT_PROVIDER_ID = "rosex-authorization-server";

	private static final String CONFIG_IN_CONTAINER = "/config/config.yml";

	public RosexAuthorizationServerContainer() {
		this(DockerImageName.parse(DEFAULT_IMAGE_NAME));
	}

	public RosexAuthorizationServerContainer(String dockerImageName) {
		this(DockerImageName.parse(dockerImageName));
	}

	public RosexAuthorizationServerContainer(DockerImageName dockerImageName) {
		super(dockerImageName);
		withExposedPorts(PORT);
		waitingFor(Wait.forHttp("/actuator/health").forPort(PORT));
	}

	/**
	 * Mount a YAML config and pass {@code --config} so users/clients override defaults.
	 */
	public RosexAuthorizationServerContainer withConfig(Path configFile) {
		Objects.requireNonNull(configFile, "configFile");
		return withConfig(MountableFile.forHostPath(configFile));
	}

	public RosexAuthorizationServerContainer withConfig(MountableFile configFile) {
		Objects.requireNonNull(configFile, "configFile");
		withCopyFileToContainer(configFile, CONFIG_IN_CONTAINER);
		withCommand("--config=" + CONFIG_IN_CONTAINER);
		return this;
	}

	public String getIssuerUri() {
		return "http://" + getHost() + ":" + getMappedPort(PORT);
	}

	public String getOpenIdConfigurationUri() {
		return getIssuerUri() + "/.well-known/openid-configuration";
	}
}
