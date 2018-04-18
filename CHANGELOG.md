
# 变更记录

## [2.0.0] - 2018/04/17

### 新增

- 新增数据点功能，支持数据点变更回调及数据自动上报逻辑。
- tools 目录集成 tc_iot_code_generator.py 命令行工具，用来为配置的数据点生成代码。

### 变更

- 取消shadow相关接口
- demo中取消token模式和直连模式的配置界面
- 修改Connection.java中connect接口