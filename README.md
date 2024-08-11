# S-RPC
### 项目介绍
S-RPC 是一款基于 Java + Netty + Etcd 实现的高性能 RPC 框架。该框架实现了服务注册、发现，自定义协议，负载均衡等功能，并且使用了 Java SPI 机制提供了标准服务接口，开发者能够根据需求对框架进行扩展。

通过学习该项目，可以了解到 RPC 框架的底层实现原理，下面是 RPC 框架调用流程图：
![image.png](https://git.acwing.com/Mendicus/image/-/raw/main/Java/20240316170702.png)

### 功能设计

#### 目录结构
```text
|-- common                  -- 示例代码的一些依赖，包括接口、model
|-- rpc-core                -- 框架核心实现
|-- rpc-spring-boot-starter -- 框架注解驱动，可在 Spring Boot 项目中使用 
|-- service-consumer        -- 服务消费者示例
|-- service-provider        -- 服务提供者示例
|-- springboot-service-consumer     -- 使用注解的服务消费者示例
`-- springboot-service-provider     -- 使用注解的服务提供在示例
```

#### 具体功能

- 支持 `application.properties` 和 `application.yml` 配置文件
- 使用 Netty 作为底层网络框架进行通信
- 增强 Java SPI 机制，支持开发者进行插件化扩展
- 提供接口的 Mock 服务
- 使用 Etcd 实现高可用的注册中心。
- 使用 Caffeine 缓存服务信息，提升框架的响应速度
- 自定义传输协议
- 实现轮询、随机、一致性哈希等负载均衡策略
- 注解驱动设计

```text
/**  传输协议
 *   0        1        2        3        4        5         ...       13                17
 *   +--------+--------+--------+--------+--------+--------------------+----------------+
 *   | magic  |version | codec  |  type  | status |    requestId       |   bodyLength   |
 *   +--------+--------+--------+--------+--------+--------------------+----------------+
 *   |                                                                                  |
 *   |                                   body                                           |
 *   |                                                                                  |
 *   +----------------------------------------------------------------------------------+
 * 1B magic（魔数）
 * 1B version（版本）
 * 1B codec（序列化类型）
 * 1B type（消息类型）
 * 1B status（状态）
 * 8B requestId（请求ID）
 * 4B bodyLenght（消息长度）
 * body（数据）
 */
```