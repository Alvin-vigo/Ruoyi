# ⚠️ 本目录是「代码生成器样例产物」，不是项目源码

`codegen/medical_drug/` 由若依代码生成器（ruoyi-generator）针对 `medical_drug` 表一键生成，
是**与 `ruoyi-system` 中真实药品模块完全重复**的演示副本（Controller/Domain/Mapper/Service/XML/SQL/Vue 一应俱全）。

## 重要约定
- **不要在本目录修改业务逻辑** —— 任何改动都不会进入运行的应用，真实代码在 `ruoyi-system/` 与 `ruoyi-ui/src/views/medical/drug/`。
- **不要把它当项目模块导入/编译** —— 它与真实模块类名、表名完全冲突，误导入会造成重复 Bean / 编译歧义。
- 保留它仅用于演示「若依代码生成器能产出什么」，或作为二次生成的基线参考。

如需重新生成，请用后端【系统工具 → 代码生成】对 `medical_drug` 表执行，生成结果导出到独立目录后自行比对，不要覆盖本目录。
