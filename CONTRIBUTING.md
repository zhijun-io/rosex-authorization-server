# 贡献指南

感谢关注 `rosex-authorization-server`。本仓库提供**本地 / 测试用** OIDC Authorization Server（非生产 IdP）。

## 参与前阅读

- [`README.md`](README.md) — 用法与模块说明
- [`CHANGELOG.md`](CHANGELOG.md) — 变更记录
- [`SECURITY.md`](SECURITY.md) — 安全披露

## 模块约定

| 模块 | 说明 |
|---|---|
| `rosex-authorization-server` | 可运行 Authorization Server（Docker 镜像由此构建） |
| `rosex-authorization-server-testcontainers` | 对外 Testcontainers 库；接口保持精简 |
| `rosex-authorization-server-sample` | 演示用 Client，不作为生产模板；**不发布到 Maven**（`maven.deploy.skip=true`） |

- 破坏性变更（配置前缀、默认镜像、默认 client、公共 TC API）须更新 README / CHANGELOG，并体现在版本号。
- 第三方依赖版本优先由 Spring Boot 父 POM / BOM 管理；子模块不硬编码版本。
- 提交信息使用中文 `type: 描述`（`feat` / `fix` / `docs` / `refactor` / `chore` 等）。

## 本地验证

```bash
./mvnw -ntp -B clean package -DskipTests
./mvnw -ntp -B test -pl rosex-authorization-server-testcontainers
```

需要 Docker 时，可运行 sample IT（依赖已发布或本地可拉取的 GHCR 镜像）：

```bash
./mvnw -ntp -B verify -pl rosex-authorization-server-sample
```

本地构建镜像：

```bash
./mvnw -ntp -B -pl rosex-authorization-server -am package -DskipTests
docker build -t ghcr.io/zhijun-io/rosex-authorization-server:local .
```

## 拉取请求

1. Fork 并基于 `main` 开分支
2. 保持改动聚焦；文档与代码同步更新
3. 确保上述构建 / 测试通过
4. PR 说明「为什么」以及如何验证

## 许可

本仓库以 [Apache License 2.0](LICENSE) 发布；提交即表示您同意在 ALv2 下贡献。
