1. 背景
BMS（电池管理系统）是用于智能化管理及维护电池单元，防止过充电和过放电，延长电池使用寿命并监控电池状态的系统。本项目旨在设计并实现一个支持规则配置和信号预警的系统，以便在各种突发情况下提升用户体验。
2. 技术栈
- 编程语言：Java
- 框架：Spring Boot
- 数据库：MySQL, Redis
- 测试：JUnit，Mockito，Postman
3. 系统架构设计
项目采用了分层架构设计，主要包括表示层、业务逻辑层和数据访问层。使用Spring Boot作为主要框架，MySQL作为关系型数据库存储车辆和规则信息，Redis作为缓存层用于加速查询。各层职责如下：
1. 接口层（Controller）：提供HTTP接口用于接收车辆信号并返回预警信息。
2. 服务层（Service）：使用Spring Boot框架实现业务逻辑，包含信号处理、规则匹配和预警生成等功能。
3. 数据访问层（model）：包含实体类，表示数据库中的表。
4. 项目总体结构
BatteryWarningSystem/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/batterywarningsystem/
│   │   │       ├── controller/
│   │   │       │   └── WarningController.java
│   │   │       ├── model/
│   │   │       │   └── Warning.java
│   │   │       ├── service/
│   │   │       │   ├── WarningService.java
│   │   │       │   └── RedisConfig.java
│   │   │       └── BatteryWarningSystemApplication.java
│   │   ├── resources/
│   │   │   └── application.properties
│   │   
│   ├── test/
│   │   └── java/
│   │       └── com/example/batterywarningsystem/
│   │           ├── controller/
│   │           │   └── WarningControllerTest.java
│   │           └── service/
│   │               └── WarningServiceTest.java
│   
├── pom.xml
