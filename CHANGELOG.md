# Changelog

本项目遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Changed

- 可运行模块由 `rosex-authorization-server-app` 重命名为 `rosex-authorization-server`

## [0.1.0-SNAPSHOT] - 2026-07-17

### Added

- 从 `spring-authorization-server` 迁移为 `rosex-authorization-server`（`zhijun-io` org）
- 升级至 Spring Boot 4.1 / Java 21，适配 Spring Security 7 Authorization Server API
- 多模块结构：`rosex-authorization-server`、`rosex-authorization-server-testcontainers`、`rosex-authorization-server-sample`
- Testcontainers 封装 `RosexAuthorizationServerContainer`（默认镜像、健康检查、`withConfig`）
- Sample OAuth2 Client 应用与 OIDC IT
- Actuator `/actuator/health`、配置前缀 `rosex.authorization-server`
- GHCR 多架构镜像发布（`linux/amd64`、`linux/arm64`）
- README：项目介绍、特性、客户端接入、Testcontainers、Docker、TLS

### Changed

- Docker layer 提取使用 Boot 4 `extract --layers --launcher`
- 默认安全日志级别由 `trace` 调整为 `info`

[Unreleased]: https://github.com/zhijun-io/rosex-authorization-server/compare/main...HEAD
[0.1.0-SNAPSHOT]: https://github.com/zhijun-io/rosex-authorization-server/commits/main
