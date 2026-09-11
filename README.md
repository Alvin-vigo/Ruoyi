# 若依医疗管理系统

基于 RuoYi（若依）前后端分离框架开发的医疗管理系统，使用 SpringBoot + MyBatis + MySQL 搭建后端服务。

## 技术栈

- **后端框架**: Spring Boot 2.7.x
- **ORM 框架**: MyBatis
- **数据库**: MySQL 8.0
- **连接池**: Druid
- **分页插件**: PageHelper
- **构建工具**: Maven
- **JDK 版本**: Java 8+

## 项目结构

```
ruoyi-medical
├── ruoyi-admin                # 启动模块（启动类、Controller）
│   └── src/main
│       ├── java/com/ruoyi
│       │   ├── RuoYiMedicalApplication.java    # 启动类
│       │   └── controller                      # 控制层
│       │       ├── PatientController.java      # 患者管理接口
│       │       └── DiagnosisController.java    # 诊疗信息接口
│       └── resources
│           └── application.yml                # 配置文件
│
├── ruoyi-common               # 通用模块（工具类、基类）
│   └── src/main/java/com/ruoyi/common
│       ├── core
│       │   ├── domain
│       │   │   ├── AjaxResult.java            # 统一返回结果
│       │   │   └── BaseEntity.java            # 实体基类
│       │   └── page
│       │       ├── PageDomain.java            # 分页参数
│       │       ├── TableDataInfo.java         # 分页结果
│       │       └── TableSupport.java          # 分页工具
│       ├── exception
│       │   └── ServiceException.java          # 业务异常
│       └── utils
│           ├── StringUtils.java              # 字符串工具
│           └── DateUtils.java                # 时间工具
│
├── ruoyi-framework            # 框架模块（配置、异常处理）
│   └── src/main/java/com/ruoyi/framework
│       ├── config
│       │   ├── MyBatisConfig.java            # MyBatis配置
│       │   └── CorsConfig.java                # 跨域配置
│       └── web/exception
│           └── GlobalExceptionHandler.java    # 全局异常处理
│
├── ruoyi-system               # 业务模块（医疗业务）
│   └── src/main
│       ├── java/com/ruoyi/system
│       │   ├── domain
│       │   │   ├── MedicalPatient.java        # 患者实体
│       │   │   └── MedicalDiagnosis.java      # 诊疗实体
│       │   ├── mapper
│       │   │   ├── MedicalPatientMapper.java  # 患者Mapper
│       │   │   └── MedicalDiagnosisMapper.java # 诊疗Mapper
│       │   └── service
│       │       ├── IMedicalPatientService.java
│       │       ├── IMedicalDiagnosisService.java
│       │       └── impl
│       │           ├── MedicalPatientServiceImpl.java
│       │           └── MedicalDiagnosisServiceImpl.java
│       └── resources/mapper
│           ├── MedicalPatientMapper.xml
│           └── MedicalDiagnosisMapper.xml
│
├── sql
│   └── medical_schema.sql      # 数据库建表脚本
│
└── doc
    ├── 接口文档.md              # API接口文档
    └── 测试记录.md              # 测试记录文档
```

## 快速开始

### 1. 环境准备

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

```sql
-- 执行 SQL 脚本
source sql/medical_schema.sql;
```

### 3. 修改配置

编辑 `ruoyi-admin/src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ruoyi_medical?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: your_password
```

### 4. 启动项目

```bash
# 在项目根目录执行
mvn clean install
cd ruoyi-admin
mvn spring-boot:run
```

或直接运行启动类：`com.ruoyi.RuoYiMedicalApplication`

### 5. 访问验证

启动成功后访问：
- 服务地址: http://localhost:8080
- 患者列表接口: http://localhost:8080/medical/patient/list

## 核心功能

### 患者管理模块
- 患者信息增删改查
- 按姓名、性别、电话、状态多条件查询
- 分页查询
- 数据校验（身份证、手机号格式）

### 诊疗信息模块
- 诊疗记录增删改查
- 关联患者信息查询
- 按科室、医生、就诊日期筛选
- 患者诊疗历史查询

## 接口列表

| 模块 | 接口 | 方法 | 路径 |
|------|------|------|------|
| 患者管理 | 分页查询列表 | GET | /medical/patient/list |
| 患者管理 | 查询详情 | GET | /medical/patient/{id} |
| 患者管理 | 新增 | POST | /medical/patient |
| 患者管理 | 修改 | PUT | /medical/patient |
| 患者管理 | 批量删除 | DELETE | /medical/patient/{ids} |
| 诊疗信息 | 分页查询列表 | GET | /medical/diagnosis/list |
| 诊疗信息 | 查询详情 | GET | /medical/diagnosis/{id} |
| 诊疗信息 | 按患者查询 | GET | /medical/diagnosis/patient/{patientId} |
| 诊疗信息 | 新增 | POST | /medical/diagnosis |
| 诊疗信息 | 修改 | PUT | /medical/diagnosis |
| 诊疗信息 | 批量删除 | DELETE | /medical/diagnosis/{ids} |

详细接口说明请查看 [doc/接口文档.md](doc/接口文档.md)

## 开发说明

- 遵循若依框架开发规范
- 分层架构：Controller → Service → Mapper → Database
- 统一返回结果格式 AjaxResult
- 分页查询统一使用 PageHelper
- 全局异常处理 GlobalExceptionHandler
- 参数校验使用 JSR-303 注解

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0.0 | 2024-01 | 初始版本，完成患者和诊疗模块 |
