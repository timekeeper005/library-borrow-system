# Library Borrow Management System

这是一个基于 Spring Boot + Spring MVC + Spring Data JPA 的图书借阅管理系统项目，覆盖三条完整业务故事线：

1. 图书信息管理（管理员视角）
2. 读者借阅图书（读者视角）
3. 借阅记录统计（管理员视角，异步实现）

## 项目技术栈
- Java 17
- Spring Boot 3.3.4
- Spring MVC
- Spring Data JPA
- H2 Database（内存数据库）
- Maven

## 业务要求覆盖
### 故事线1：图书信息管理
- 新增图书
- 单本查询
- 列表查询 / 模糊查询
- 更新图书信息
- 删除图书
- ISBN 唯一校验
- 删除前检查是否有未归还借阅记录

### 故事线2：读者借阅图书
- 查询可借阅图书
- 读者借阅图书
- 查询个人借阅记录
- 归还图书
- 库存校验：库存 > 0
- 读者存在超期未归还时禁止新借阅
- 单本图书每人只能借阅一件未归还的书
- 借阅期限 30 天，超期标记为 OVERDUE

### 故事线3：借阅记录统计
- 统计指定时间段内每本图书借阅次数
- 采用异步任务执行，返回 taskId
- 通过 taskId 查询统计结果
- 统计结果包含图书 ID、书名、借阅次数

## Spring MVC 核心工作原理说明
Spring MVC 是 Spring 用来处理 Web 请求的核心模块，典型请求链路可以概括为：

1. 请求进入 DispatcherServlet
2. DispatcherServlet 通过 HandlerMapping 找到对应 Controller 方法
3. HandlerAdapter 调用处理器方法
4. HandlerMethodArgumentResolver 负责绑定请求参数到方法参数
5. 业务逻辑在 Service 层执行
6. Controller 返回对象或 ResponseEntity
7. ReturnValueHandler + HttpMessageConverter 把结果转换成 JSON
8. 响应返回给客户端

其中最关键的组件包括：
- Controller：接收 HTTP 请求并编排流程
- Service：处理业务逻辑和事务
- Repository：访问数据库
- DispatcherServlet：统一请求分发入口
- HandlerMapping：维护 URL 到 Controller 的映射
- HandlerAdapter：适配具体处理方法的调用
- HttpMessageConverter：负责 JSON / XML 等数据格式转换

### 参数绑定示例
- @PathVariable：从 URL 路径变量中获取参数
- @RequestParam：从查询参数中获取参数
- @RequestBody：从请求体中解析 JSON
- @Validated：启用参数校验

### 返回值处理示例
- 返回普通对象：Spring 自动转换为 JSON
- 返回 ResponseEntity：可控制状态码和响应头
- 返回 void：可用于无内容响应，如 204 No Content

## 项目启动
```bash
mvn clean package
java -jar target/library-borrow-system-0.0.1-SNAPSHOT.jar
```

或直接开发模式启动：
```bash
mvn spring-boot:run
```

## H2 数据库控制台
访问：
```text
http://localhost:8080/h2-console
```

连接信息：
- JDBC URL: jdbc:h2:mem:librarydb
- 用户名: sa
- 密码: 空

## API 说明
### 1）管理员图书管理
- POST /api/admin/books
- GET /api/admin/books/{id}
- GET /api/admin/books?keyword=xxx&page=0&size=10
- PUT /api/admin/books/{id}
- DELETE /api/admin/books/{id}

### 2）读者借阅
- GET /api/books/available
- POST /api/readers/{readerId}/borrow
- GET /api/readers/{readerId}/records
- POST /api/readers/{readerId}/return/{recordId}

### 3）异步借阅统计
- POST /api/admin/stats/borrow-counts?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
- GET /api/admin/stats/{taskId}

## 示例请求
### 新增图书
```bash
curl -X POST http://localhost:8080/api/admin/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Spring in Action",
    "author": "Craig Walls",
    "isbn": "9781617294945",
    "category": "Java",
    "stock": 10,
    "publisher": "Manning",
    "publishDate": "2024-01-01"
  }'
```

### 借阅图书
```bash
curl -X POST http://localhost:8080/api/readers/1/borrow \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1
  }'
```

### 异步统计
```bash
curl -X POST "http://localhost:8080/api/admin/stats/borrow-counts?from=2024-01-01T00:00:00&to=2024-12-31T23:59:59"
```

## 说明
本项目适合作为课程作业或教学案例，用于深入理解 Spring Boot、RESTful 接口设计、JPA 数据持久化，以及 Spring MVC 的请求处理工作原理。
