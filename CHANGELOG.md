
# 变更记录

## [2.0.0] - 2018/04/17

### 新增

- 新增数据点功能，支持数据点变更回调及数据自动上报逻辑。
- tools 目录集成 tc_iot_code_generator.py 命令行工具，用来为配置的数据点生成代码。

### 变更

- 取消shadow相关接口
- demo中取消token模式和直连模式的配置界面
- 修改Connection.java中connect接口

## [2.1.0] - 2018/05/30

### 新增

- 数据点支持String类型
- 上报SDK信息

### 变更

- fix bugs

## [2.2.0] - 2018/06/05

### 新增

- 支持二进制数据流。
- 支持http方式请求token。

### 变更

- 取消python生成JsonFileData的机制，改为SDK内部解析json文件。
- 移除com.tencent.qcloud.iot.sample.qcloud里面的类。
- 允许设置用http方式请求token。

## [2.3.0] - 2018/07/12

### 新增

- ota通道。

### 变更

- fix bugs