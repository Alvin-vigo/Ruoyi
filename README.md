# 若依医疗管理系统

基于 RuoYi（若依）前后端分离框架开发的医疗管理系统，使用 SpringBoot + MyBatis + MySQL 搭建后端服务，前端兼容 RuoYi-Vue。

## 技术栈

- **后端框架**: Spring Boot 2.7.x
- **ORM 框架**: MyBatis
- **数据库**: MySQL 8.0
- **连接池**: Druid
- **分页插件**: PageHelper
- **Excel 导出**: EasyExcel
- **代码生成**: Velocity 模板引擎
- **构建工具**: Maven
- **JDK 版本**: Java 8+
- **前端框架**: Vue 2 + Element UI（兼容 RuoYi-Vue）

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
├── ruoyi-common               # 通用模块（工具类、基类、Excel、注解）
│   └── src/main/java/com/ruoyi/common
│       ├── annotation
│       │   └── Excel.java                     # Excel 导出注解
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
│           ├── DateUtils.java                # 时间工具
│           └── poi
│               └── ExcelUtil.java            # Excel 导出工具
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
├── ruoyi-generator            # 代码生成器模块
│   └── src/main
│       ├── java/com/ruoyi/generator
│       │   ├── domain
│       │   │   ├── GenTable.java              # 生成业务表
│       │   │   └── GenTableColumn.java        # 生成业务表字段
│       │   ├── mapper
│       │   │   ├── GenTableMapper.java
│       │   │   └── GenTableColumnMapper.java
│       │   ├── service
│       │   │   ├── IGenTableService.java
│       │   │   └── impl/GenTableServiceImpl.java
│       │   ├── controller/GenTableController.java
│       │   ├── config/GenConfig.java
│       │   └── util
│       │       ├── GenUtils.java
│       │       ├── VelocityInitializer.java
│       │       └── VelocityUtils.java
│       └── resources
│           ├── mapper/generator
│           │   ├── GenTableMapper.xml
│           │   └── GenTableColumnMapper.xml
│           └── vm
│               ├── java                       # Java 后端模板
│               ├── xml                        # MyBatis XML 模板
│               ├── vue                        # Vue 前端模板
│               └── sql                        # 菜单权限 SQL 模板
│
├── ruoyi-ui                   # RuoYi-Vue 兼容前端页面
│   └── src
│       ├── api
│       │   └── medical
│       │       ├── patient.js
│       │       └── diagnosis.js
│       └── views
│           └── medical
│               ├── patient/index.vue
│               └── diagnosis/index.vue
│
├── sql
│   ├── medical_schema.sql     # 数据库建表脚本
│   └── medical_menu.sql       # 菜单权限脚本
│
└── doc
    ├── 接口文档.md             # API接口文档
    ├── 测试记录.md             # 测试记录文档
    └── 部署说明.md             # 部署与使用说明
```

## 快速开始

### 1. 环境准备

- JDK 1.8+
- **Maven：无需单独安装** —— 项目 `tools/apache-maven-3.9.6/` 已内置 Maven 3.9.6，换设备直接可用（见下方「项目内置 Maven」）
- MySQL 8.0+
- Node.js 14+（前端）

### 2. 数据库初始化

```sql
-- 执行 SQL 脚本
source sql/medical_schema.sql;
source sql/medical_menu.sql;
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

### 4. 启动后端

```bash
# 方式一（推荐）：使用项目内置 Maven，无需本机安装
#   Windows CMD
mvnw.cmd clean install -DskipTests
#   Windows PowerShell
.\mvnw.ps1 clean install -DskipTests
#   Git Bash / Linux / macOS
./mvnw clean install -DskipTests

# 方式二：已安装 Maven 时可直接用 mvn
mvn clean install -DskipTests
```

启动应用（任选其一）：

```bash
# 使用内置 Maven 启动
mvnw.cmd -pl ruoyi-admin spring-boot:run

# 或直接运行打好的 jar
java -jar ruoyi-admin/target/ruoyi-admin-1.0.0.jar
```

或直接运行启动类：`com.ruoyi.RuoYiMedicalApplication`

### 项目内置 Maven（换设备无需重新下载）

项目 `tools/apache-maven-3.9.6/` 目录随代码一起携带了 Maven 3.9.6（约 10MB），
拷贝/克隆整个项目即可在任何设备上构建，**无需再单独安装或下载 Maven**。

| 环境 | 启动命令 |
|------|----------|
| Windows CMD | `mvnw.cmd clean install -DskipTests` |
| Windows PowerShell | `.\mvnw.ps1 clean install -DskipTests` |
| Git Bash / Linux / macOS | `./mvnw clean install -DskipTests` |

要点说明：

- 三个 `mvnw*` 脚本都会**自动定位项目根目录**，从 `tools/` 下取 Maven，与设备无关；
- 仅需本机已装 **JDK 1.8+**（`JAVA_HOME` 或 PATH 中的 `java`），Maven 本身不必安装；
- 依赖包默认缓存到用户目录 `~/.m2/repository`（首次构建需联网下载依赖；如需完全离线，可把 `~/.m2` 也一并拷贝）；
- 如需升级/更换 Maven 版本，替换 `tools/` 下目录并同步修改 `mvnw*` 脚本中的 `apache-maven-3.9.6` 路径；
- `mvnw.cmd` 仅含 ASCII 字符——批处理文件按控制台代码页（中文 Windows 为 GBK）解析，中文注释会导致命令错乱，请勿在 `.cmd` 中加中文；
- Git Bash 环境如缺少 `uname`/`dirname` 等命令，标准 `bin/mvn` 脚本会失败，`./mvnw` 已内置兼容处理（直接以 `java` + classworlds 启动器调用 Maven）。

> 已实测：`./mvnw -v` 输出 `Maven home: <项目路径>\tools\apache-maven-3.9.6`，`mvnw.cmd -v` 同样指向项目内置 Maven。

### 5. 启动前端

将 `ruoyi-ui` 中的页面复制到 RuoYi-Vue 项目对应位置，执行：

```bash
cd ruoyi-ui  # 或你的 RuoYi-Vue 项目目录
npm install
npm run dev
```

### 6. 访问验证

- 后端服务: http://localhost:8080
- 患者列表接口: http://localhost:8080/medical/patient/list
- 代码生成器: http://localhost:8080/tool/gen/list

## 核心功能

### 患者管理模块
- 患者信息增删改查
- 按姓名、性别、电话、状态多条件查询
- 分页查询
- 数据校验（身份证、手机号格式）
- Excel 导出

### 诊疗信息模块
- 诊疗记录增删改查
- 关联患者信息查询
- 按科室、医生、就诊日期筛选
- 患者诊疗历史查询
- Excel 导出

### 代码生成器模块
- 读取数据库表结构
- 导入需要生成的表
- 预览生成代码
- 批量下载前后端代码（Java / XML / Vue / SQL）
- 兼容 RuoYi-Vue 前端规范

## 接口列表

| 模块 | 接口 | 方法 | 路径 |
|------|------|------|------|
| 患者管理 | 分页查询列表 | GET | /medical/patient/list |
| 患者管理 | 查询详情 | GET | /medical/patient/{id} |
| 患者管理 | 新增 | POST | /medical/patient |
| 患者管理 | 修改 | PUT | /medical/patient |
| 患者管理 | 批量删除 | DELETE | /medical/patient/{ids} |
| 患者管理 | 导出 | GET | /medical/patient/export |
| 诊疗信息 | 分页查询列表 | GET | /medical/diagnosis/list |
| 诊疗信息 | 查询详情 | GET | /medical/diagnosis/{id} |
| 诊疗信息 | 按患者查询 | GET | /medical/diagnosis/patient/{patientId} |
| 诊疗信息 | 新增 | POST | /medical/diagnosis |
| 诊疗信息 | 修改 | PUT | /medical/diagnosis |
| 诊疗信息 | 批量删除 | DELETE | /medical/diagnosis/{ids} |
| 诊疗信息 | 导出 | GET | /medical/diagnosis/export |
| 代码生成 | 查询生成列表 | GET | /tool/gen/list |
| 代码生成 | 导入表 | POST | /tool/gen/importTable |
| 代码生成 | 预览代码 | GET | /tool/gen/preview/{tableId} |
| 代码生成 | 下载代码 | GET | /tool/gen/batchGenCode |

详细接口说明请查看 [doc/接口文档.md](doc/接口文档.md)，部署说明请查看 [doc/部署说明.md](doc/部署说明.md)。

## 代码生成器使用示例

```bash
# 1. 导入表结构
curl -X POST "http://localhost:8080/tool/gen/importTable?tables=medical_patient,medical_diagnosis"

# 2. 批量生成并下载代码
curl -X GET "http://localhost:8080/tool/gen/batchGenCode?tables=medical_patient,medical_diagnosis" -o ruoyi_gen.zip
```

## 开发说明

- 遵循若依框架开发规范
- 分层架构：Controller → Service → Mapper → Database
- 统一返回结果格式 AjaxResult
- 分页查询统一使用 PageHelper
- 全局异常处理 GlobalExceptionHandler
- 参数校验使用 JSR-303 注解
- Excel 导出使用 `@Excel` 注解 + `ExcelUtil`

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| V1.0.0 | 2024-01 | 初始版本，完成患者和诊疗模块 |
| V1.1.0 | 2024-09 | 新增代码生成器、前端页面、Excel 导出、部署说明 |
