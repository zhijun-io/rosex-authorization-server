# TLS / mTLS sample materials (local / test only)

Generate self-signed PEMs (required before running TLS / Compose TLS examples):

```bash
./scripts/generate-tls-certs.sh
```

Outputs under `samples/tls/certs/` (gitignored except `.gitkeep`):

| File | Purpose |
|---|---|
| `server.crt` / `server.key` | Authorization Server HTTPS identity |
| `client-ca.crt` / `client-ca.key` | CA used to verify client certificates (mTLS) |
| `client.crt` / `client.key` | Sample client certificate for mTLS callers |

## Host YAML vs Compose YAML

| File | Paths | Use with |
|---|---|---|
| `application-tls.yml` / `application-mtls.yml` | `samples/tls/certs/...` (host-relative) | `java -jar` from repo root |
| `application-tls.compose.yml` / `application-mtls.compose.yml` | `/certs/...` (container paths) | `compose/docker-compose.*.yml` |

## HTTPS only

```bash
./mvnw -ntp -pl rosex-authorization-server -am package -DskipTests
java -jar rosex-authorization-server/target/rosex-authorization-server.jar \
  --config=samples/config.yml \
  --spring.config.additional-location=optional:file:samples/tls/application-tls.yml
```

Issuer becomes `https://localhost:9000`. Clients must trust `server.crt` (or disable verification only in local tests).

## mTLS

```bash
java -jar rosex-authorization-server/target/rosex-authorization-server.jar \
  --config=samples/config.yml \
  --spring.config.additional-location=optional:file:samples/tls/application-mtls.yml
```

Callers must present `client.crt` / `client.key` signed by `client-ca.crt`.

## Testcontainers

```java
@Container
static RosexAuthorizationServerContainer authServer = new RosexAuthorizationServerContainer()
        .withConfig(Path.of("samples/config.yml"))
        .withTls(Path.of("samples/tls/certs/server.crt"), Path.of("samples/tls/certs/server.key"));
```

For mTLS: `.withMutualTls(serverCrt, serverKey, Path.of("samples/tls/certs/client-ca.crt"))`.

mTLS readiness uses a startup-log probe (not HTTPS `/actuator/health`), because health checks without a client certificate fail when `client-auth=NEED`.

> Do not commit generated materials under `samples/tls/certs/` (ignored by git). Regenerate with `./scripts/generate-tls-certs.sh`.
