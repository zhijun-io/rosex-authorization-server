# rosex-authorization-server

本地 / 集成测试用的 **OIDC Authorization Server**（**勿用于生产**）。

基于 Spring Boot 4.1 与 Spring Security OAuth2 Authorization Server，提供可运行的 IdP、Testcontainers 封装，以及可联调的 OAuth2 Client sample。适合在开发机或 CI 中验证登录、授权码、Refresh Token、自定义 claims（如 `roles`）等流程。

| | |
|---|---|
| 仓库 | [zhijun-io/rosex-authorization-server](https://github.com/zhijun-io/rosex-authorization-server) |
| 镜像 | `ghcr.io/zhijun-io/rosex-authorization-server` |
| 默认端口 | `9000` |
| 默认 Client | `default-client-id` / `default-client-secret` |
| 默认用户 | `user` / `password`（可用 YAML 覆盖） |
| 配置前缀 | `rosex.authorization-server` |

## 特性

- **开箱即用的 OIDC IdP**：Authorization Code、Refresh Token、Client Credentials、Token Exchange
- **YAML 配置用户与 Client**：自定义 attributes / claims（如 `roles`、`email`）
- **登录 UI**：Thymeleaf 表单登录，便于浏览器联调
- **启动时打印 Client 配置片段**：可直接粘贴到 Boot OAuth2 Client
- **健康检查**：`/actuator/health`，方便 Testcontainers / Docker wait
- **Testcontainers 模块**：固定镜像、端口、issuer、默认 client 常量
- **Sample 应用**：端到端演示 OAuth2 Login
- **TLS**：沿用 Spring Boot SSL Bundle 配置

## 模块

| 模块 | 坐标 | 说明 |
|---|---|---|
| `rosex-authorization-server-parent` | 父 POM | 版本与依赖管理 |
| `rosex-authorization-server-app` | 可运行 AS | 打 jar / 构建 Docker 镜像 |
| `rosex-authorization-server-testcontainers` | TC 封装 | `RosexAuthorizationServerContainer` |
| `rosex-authorization-server-sample` | 示例 Client | 浏览器登录演示 + OIDC IT |

## 快速开始

### 从源码运行

```bash
./mvnw -ntp clean package -DskipTests
java -jar rosex-authorization-server-app/target/rosex-authorization-server.jar --config=samples/config.yml
```

常用参数：

| 参数 | 说明 |
|---|---|
| `--config=<path>` | 指定 YAML 配置文件 |
| `--print-sample-config` | 打印完整示例配置 |
| `--help` / `-h` | 查看帮助 |

启动成功后访问：

- Issuer / 登录页：http://localhost:9000
- OIDC 元数据：http://localhost:9000/.well-known/openid-configuration
- 健康检查：http://localhost:9000/actuator/health

控制台会打印可用用户，以及可粘贴到客户端的 `spring.security.oauth2.client.*` 片段。

### 与 Sample 联调

终端 1 启动 Authorization Server（如上）。终端 2：

```bash
./mvnw -ntp -pl rosex-authorization-server-sample spring-boot:run
```

浏览器打开 http://localhost:8080 ，点击 **Sign in**。使用 `samples/config.yml` 时可用 `alice` / `alice`（或 `bob` / `bob`）；未挂载自定义用户时默认 `user` / `password`。

## 配置

配置前缀：`rosex.authorization-server`。示例见 [`samples/config.yml`](samples/config.yml)：

```yaml
rosex:
  authorization-server:
    users:
      - username: alice
        password: alice
        attributes:
          email: alice@example.com
          roles:
            - viewer
            - editor
            - admin
      - username: bob
        password: bob
        attributes:
          email: bob@example.com
          roles:
            - viewer
            - editor
```

完整字段（JWK、自定义 clients、token TTL 等）可通过 `--print-sample-config` 查看。

## 在客户端应用中使用

### 配置 OAuth2 Client

1. 启动 Authorization Server，从控制台复制打印出的 client 配置（或使用下方默认片段）。
2. 确保 scope 包含 `openid`。
3. `issuer-uri` 指向 Authorization Server（本地一般为 `http://127.0.0.1:9000`）。

默认 Client 对应配置：

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          rosex-authorization-server:
            provider: rosex-authorization-server
            client-id: default-client-id
            client-secret: default-client-secret
            client-name: Rosex Authorization Server
            scope:
              - openid
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          rosex-authorization-server:
            issuer-uri: http://127.0.0.1:9000
```

Client 的 `client-id` / `secret` / `redirect-uri` / `scope` 须与 Authorization Server 侧 `rosex.authorization-server.clients`（或默认 Client）一致。默认 redirect 已包含：

- `http://localhost:8080`
- `http://localhost:8080/login/oauth2/code/rosex-authorization-server`

且默认关闭严格 redirect 校验（`validateRedirectUri=false`），便于本地联调。

### 从 `roles` claim 映射权限

Authorization Server 可将用户 `attributes.roles` 写入 ID Token。客户端可提供 `OidcUserService`，把 `roles` 映射为 Spring Security 角色：

```java
@Bean
OidcUserService oidcUserService() {
    var oidcUserService = new OidcUserService();
    oidcUserService.setOidcUserMapper((oidcUserRequest, oidcUserInfo) -> {
        var roles = oidcUserRequest.getIdToken().getClaimAsStringList("roles");
        var authorities = AuthorityUtils.createAuthorityList();
        if (roles != null) {
            roles.stream()
                    .map(r -> "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return new DefaultOidcUser(authorities, oidcUserRequest.getIdToken(), oidcUserInfo);
    });
    return oidcUserService;
}
```

随后可在请求或方法安全中校验：

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/public/**").permitAll()
                    .requestMatchers("/document/**").hasAnyRole("viewer", "editor", "admin")
                    .requestMatchers("/admin/**").hasRole("admin")
                    .anyRequest().authenticated())
            .oauth2Login(Customizer.withDefaults())
            .build();
}
```

更完整的可运行示例见模块 `rosex-authorization-server-sample`。

## 在测试中使用（Testcontainers）

推荐依赖本仓库提供的封装模块（已钉死镜像、健康检查与默认 client 常量）：

```xml
<dependency>
  <groupId>io.zhijun.rosex</groupId>
  <artifactId>rosex-authorization-server-testcontainers</artifactId>
  <version>${rosex-authorization-server.version}</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>testcontainers-junit-jupiter</artifactId>
  <scope>test</scope>
</dependency>
```

```java
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
class OAuth2ClientIT {

    @Container
    static RosexAuthorizationServerContainer authServer = new RosexAuthorizationServerContainer();

    @DynamicPropertySource
    static void oauth2Properties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.security.oauth2.client.provider.rosex-authorization-server.issuer-uri",
                authServer::getIssuerUri);
    }

    @Test
    void openIdConfigurationIsAvailable() {
        RestClient.create()
                .get()
                .uri(authServer.getOpenIdConfigurationUri())
                .retrieve()
                .toBodilessEntity();
    }
}
```

常用 API / 常量：

| API | 说明 |
|---|---|
| `getIssuerUri()` | 动态映射后的 issuer |
| `getOpenIdConfigurationUri()` | OIDC discovery URL |
| `withConfig(Path)` / `withConfig(MountableFile)` | 挂载自定义 YAML |
| `DEFAULT_IMAGE_NAME` | `ghcr.io/zhijun-io/rosex-authorization-server:latest` |
| `DEFAULT_CLIENT_ID` / `DEFAULT_CLIENT_SECRET` | 与默认 Client 一致 |
| `DEFAULT_PROVIDER_ID` | `rosex-authorization-server` |
| `PORT` | 容器内端口 `9000` |

> 拉取镜像前请确认 GHCR 包对你的环境可见（org 包需设为 Public，或已 `docker login ghcr.io`）。

## Docker

构建并运行：

```bash
./mvnw -ntp -B -pl rosex-authorization-server-app -am package -DskipTests
docker build -t ghcr.io/zhijun-io/rosex-authorization-server:local .
docker run --rm -p 9000:9000 ghcr.io/zhijun-io/rosex-authorization-server:local
```

使用已发布镜像：

```bash
docker run --rm -p 9000:9000 ghcr.io/zhijun-io/rosex-authorization-server:latest
```

挂载自定义配置：

```bash
docker run --rm -p 9000:9000 \
  -v "$PWD/samples/config.yml:/config/config.yml:ro" \
  ghcr.io/zhijun-io/rosex-authorization-server:latest \
  --config=/config/config.yml
```

CI 会在 `main` 推送时构建并发布 `linux/amd64` 与 `linux/arm64` 镜像。

## TLS support

本项目基于 Spring Boot，可直接使用 Boot 的 SSL Bundle 对外提供 HTTPS。在配置中增加：

```yaml
spring:
  ssl:
    bundle:
      pem:
        server:
          keystore:
            certificate: /path/to/certificate/localhost.pem
            private-key: /path/to/private/key/localhost-key.pem

server:
  ssl:
    bundle: server
    client-auth: NONE
```

启用 TLS 后，客户端的 `issuer-uri` 需改为 `https://...`，并确保证书链可被客户端信任（本地可用自签证书 + 信任库，或仅在受控环境关闭校验）。

参考：

- [Securing Spring Boot Applications With SSL](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl)
- [用 OpenSSL 生成自签证书](https://stackoverflow.com/questions/10175812/how-can-i-generate-a-self-signed-ssl-certificate-using-openssl)

## 健康检查与发现

| 端点 | 说明 |
|---|---|
| `GET /actuator/health` | 存活 / 就绪 |
| `GET /.well-known/openid-configuration` | OIDC Discovery |
| `GET /oauth2/jwks` | JWK Set |

## 与 Rosex 的关系

本仓库是**独立可运行 IdP**，供 sample / IT / 本地联调使用。Rosex 主仓可通过 DevService 或 Compose 按需拉起该镜像；**不会**把 IdP 源码并入 `rosex` monorepo（见 Rosex ADR 0001：不做身份权威库产品）。
