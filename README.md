# xiaohashu

`xiaohashu` 是一个基于 Spring Cloud Alibaba 的小红书后端微服务示例项目，用于演示内容社区场景下的服务拆分与协作。项目覆盖了认证、网关、用户、笔记、关注关系、计数、搜索、对象存储、KV、分布式 ID 和数据对齐等核心能力。

## 项目作用

该项目主要用于实现和串联以下业务能力：

- 用户登录认证与令牌管理
- 笔记发布、详情、点赞、收藏
- 用户关注与粉丝关系维护
- 计数聚合与缓存协同
- 文件上传与对象存储访问
- Elasticsearch 搜索
- 基于 MQ 和定时任务的数据对齐

核心服务包括：

- `xiaohashu-gateway`：统一网关入口
- `xiaohashu-auth`：认证服务
- `xiaohashu-user`：用户服务
- `xiaohashu-note`：笔记服务
- `xiaohashu-user-relation`：关注关系服务
- `xiaohashu-count`：计数服务
- `xiaohashu-search`：搜索服务
- `xiaohashu-oss`：对象存储服务
- `xiaohashu-kv`：KV 服务
- `xiaohashu-distributed-id-generator`：分布式 ID 服务
- `xiaohashu-data-align`：数据对齐与任务调度

## 目录说明

```text
xiaohashu/
├─ pom.xml
├─ xiaoha-framework/
├─ xiaohashu-auth/
├─ xiaohashu-gateway/
├─ xiaohashu-user/
├─ xiaohashu-note/
├─ xiaohashu-user-relation/
├─ xiaohashu-count/
├─ xiaohashu-search/
├─ xiaohashu-oss/
├─ xiaohashu-kv/
├─ xiaohashu-distributed-id-generator/
└─ xiaohashu-data-align/
```

- `pom.xml`：根 Maven 聚合工程，统一管理模块与依赖版本
- `xiaoha-framework/`：公共组件与自定义 Spring Boot Starter
- `xiaohashu-auth/`：认证服务
- `xiaohashu-gateway/`：网关服务，作为统一入口
- `xiaohashu-user/`：用户服务，包含用户资料与权限相关能力
- `xiaohashu-note/`：笔记服务，负责发布、详情、点赞、收藏等功能
- `xiaohashu-user-relation/`：用户关注与粉丝关系服务
- `xiaohashu-count/`：用户和笔记计数服务
- `xiaohashu-search/`：搜索服务，对接 Elasticsearch
- `xiaohashu-oss/`：对象存储服务，对接 MinIO
- `xiaohashu-kv/`：KV 服务封装
- `xiaohashu-distributed-id-generator/`：分布式 ID 生成服务
- `xiaohashu-data-align/`：数据对齐、MQ 消费与 XXL-Job 任务

多数业务模块采用 `*-api` 与 `*-biz` 双层结构：

- `*-api`：对外接口与共享模型
- `*-biz`：业务实现与服务启动模块

## 启动前准备

运行该项目前，建议先准备以下基础环境：

- JDK 17
- Maven 3.8+
- MySQL 8
- Redis
- Nacos
- RocketMQ
- MinIO
- Elasticsearch
- XXL-Job

仓库中的开发配置默认使用：

- Nacos 命名空间：`xiaohashu`
- 多个基础服务地址：`192.168.88.101`

如果你的本地环境不同，需要先调整各模块中的 `bootstrap.yaml`、`application.yaml` 或 `application-dev.yaml`。

## 启动方式

### 1. 启动基础依赖

先启动外部中间件：

1. Nacos
2. MySQL
3. Redis
4. RocketMQ
5. MinIO
6. Elasticsearch
7. XXL-Job

### 2. 编译项目

在仓库根目录执行：

```bash
mvn clean install
```

### 3. 启动微服务

推荐启动顺序如下：

1. `xiaohashu-distributed-id-generator`
2. `xiaohashu-auth`
3. `xiaohashu-user`
4. `xiaohashu-note`
5. `xiaohashu-user-relation`
6. `xiaohashu-count`
7. `xiaohashu-oss`
8. `xiaohashu-kv`
9. `xiaohashu-search`
10. `xiaohashu-gateway`
11. `xiaohashu-data-align`

可按模块分别启动，例如：

```bash
mvn -pl xiaohashu-distributed-id-generator/xiaohashu-distributed-id-generator-biz -am spring-boot:run
mvn -pl xiaohashu-auth -am spring-boot:run
mvn -pl xiaohashu-user/xiaohashu-user-biz -am spring-boot:run
mvn -pl xiaohashu-note/xiaohashu-note-biz -am spring-boot:run
mvn -pl xiaohashu-user-relation/xiaohashu-user-relation-biz -am spring-boot:run
mvn -pl xiaohashu-count/xiaohashu-count-biz -am spring-boot:run
mvn -pl xiaohashu-oss/xiaohashu-oss-biz -am spring-boot:run
mvn -pl xiaohashu-kv/xiaohashu-kv-biz -am spring-boot:run
mvn -pl xiaohashu-search -am spring-boot:run
mvn -pl xiaohashu-gateway -am spring-boot:run
mvn -pl xiaohashu-data-align -am spring-boot:run
```

### 4. 访问入口

全部服务启动后，通常通过 `xiaohashu-gateway` 作为统一入口访问系统，其余服务通过注册中心完成服务发现与调用。
