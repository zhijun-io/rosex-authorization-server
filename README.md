# rosex-authorization-server

本地 / 测试用的 OIDC Authorization Server（**勿用于生产**）。基于 Spring Boot 4.1 + Spring Security OAuth2 Authorization Server。

坐标：`io.zhijun.rosex:rosex-authorization-server`  
镜像：`ghcr.io/zhijun-io/rosex-authorization-server`

## 快速开始

```bash
./mvnw -ntp clean package
java -jar target/rosex-authorization-server-0.1.0-SNAPSHOT.jar --config=samples/config.yml
```

默认端口 `9000`。默认 client：`default-client-id` / `default-client-secret`。

## 配置

配置前缀：`rosex.authorization-server`

```yaml
rosex:
  authorization-server:
    users:
      - username: alice
        password: alice
        attributes:
          email: alice@example.com
          roles: [viewer, editor, admin]
```

更多示例：`--print-sample-config` 或 `samples/config.yml`。

## Docker

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

本地构建：

```bash
./mvnw -ntp -B package -DskipTests
docker build -t ghcr.io/zhijun-io/rosex-authorization-server:local .
```

## 客户端（Boot）

启动后控制台会打印可粘贴的 `spring.security.oauth2.client.*` 片段。issuer 一般为 `http://localhost:9000`。

Testcontainers 示例：

```java
@Container
static GenericContainer<?> authServer =
    new GenericContainer<>("ghcr.io/zhijun-io/rosex-authorization-server:latest")
        .withExposedPorts(9000)
        .waitingFor(Wait.forHttp("/actuator/health").forPort(9000));
```

## 健康检查

- `GET /actuator/health`
- `GET /.well-known/openid-configuration`

## 与 Rosex 的关系

本仓库是独立可运行 IdP，供 sample / IT 使用。Rosex 主仓后续可通过 DevService 按需拉起该镜像；**不会**把 IdP 源码并入 `rosex` monorepo。
