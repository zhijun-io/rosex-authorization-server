# rosex-authorization-server

本地 / 测试用的 OIDC Authorization Server（**勿用于生产**）。基于 Spring Boot 4.1 + Spring Security OAuth2 Authorization Server。

## 模块

| 模块 | 说明 |
|---|---|
| `rosex-authorization-server-app` | 可运行 Authorization Server |
| `rosex-authorization-server-testcontainers` | Testcontainers 封装 |
| `rosex-authorization-server-sample` | OAuth2 Client 示例应用 |

镜像：`ghcr.io/zhijun-io/rosex-authorization-server`

## 快速开始

```bash
./mvnw -ntp clean package -DskipTests
java -jar rosex-authorization-server-app/target/rosex-authorization-server.jar --config=samples/config.yml
```

默认端口 `9000`。默认 client：`default-client-id` / `default-client-secret`。

另开终端跑 sample：

```bash
./mvnw -ntp -pl rosex-authorization-server-sample spring-boot:run
```

浏览器打开 http://localhost:8080 ，点击 Sign in。

## 配置

配置前缀：`rosex.authorization-server`（见 `samples/config.yml`）。

## Testcontainers

```xml
<dependency>
  <groupId>io.zhijun.rosex</groupId>
  <artifactId>rosex-authorization-server-testcontainers</artifactId>
  <scope>test</scope>
</dependency>
```

```java
@Container
static RosexAuthorizationServerContainer authServer = new RosexAuthorizationServerContainer();

@DynamicPropertySource
static void oauth2(DynamicPropertyRegistry registry) {
    registry.add("spring.security.oauth2.client.provider.rosex-authorization-server.issuer-uri",
            authServer::getIssuerUri);
}
```

常量：`DEFAULT_CLIENT_ID` / `DEFAULT_CLIENT_SECRET` / `DEFAULT_PROVIDER_ID`。

可选挂载配置：`authServer.withConfig(Path.of("samples/config.yml"))`。

## Docker

```bash
./mvnw -ntp -B -pl rosex-authorization-server-app -am package -DskipTests
docker build -t ghcr.io/zhijun-io/rosex-authorization-server:local .
docker run --rm -p 9000:9000 ghcr.io/zhijun-io/rosex-authorization-server:local
```

## 健康检查

- `GET /actuator/health`
- `GET /.well-known/openid-configuration`
