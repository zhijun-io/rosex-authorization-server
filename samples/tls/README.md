# TLS / mTLS sample materials (local / test only)

Generate self-signed PEMs:

```bash
./scripts/generate-tls-certs.sh
```

Outputs under `samples/tls/certs/`:

| File | Purpose |
|---|---|
| `server.crt` / `server.key` | Authorization Server HTTPS identity |
| `client-ca.crt` | CA used to verify client certificates (mTLS) |
| `client.crt` / `client.key` | Sample client certificate for mTLS callers |

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

> Do not commit real private keys. `samples/tls/certs/*.key` is gitignored; regenerate locally.
