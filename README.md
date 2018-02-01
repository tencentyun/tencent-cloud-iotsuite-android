
### 介绍

IotSDK是腾讯云iotsuite的设备端SDK，IotSample是使用IotSDK的demo。

### 引入SDK

#### 两种引入方式

1、下载SDK，然后本地依赖

    implementation project(path: ':IotSDK')

2、直接依赖jcenter上的库

    implementation 'com.tencent.qcloud:iot-android-sdk:1.1.0'

### 编译运行Demo

#### 下载SDK

腾讯云iotsuite Android版SDK下载：

    git clone https://github.com/tencentyun/tencent-cloud-iotsuite-android

#### 使用Android Studio打开

使用 Android Studio 打开工程 tencent-cloud-iotsuite-android。

#### 配置参数

腾讯云iotsuite有两种连接模式：mqtt直连模式和token模式，一个设备只支持其中一种。

SDK需要一些配置参数，包括mqttHost、productKey、productId、deviceName、deviceSecret，这些参数可以从iot控制台获取。

除以上参数外，还需要userName和password用于mqtt连接。如果是token模式，SDK内部会自动根据以上参数获取userName和password，而如果是mqtt直连模式，则需要联系腾讯云获取对应的userName和password。

Demo中的参数配置示例在ConnectionFragment.java中：

```
    //直连模式参数
    private String mDirectMqttHost = "mqtt-m2i58z3s.ap-guangzhou.mqtt.tencentcloudmq.com";
    private String mDirectProductKey = "mqtt-m2i58z3s";
    private String mDirectProductId = "iot-6xzr8ap8";
    private String mDirectDeviceName = "test_android_1";
    private String mDirectDeviceSecret = "48bf05179b6f1be3b38c89f27c804f11";
    private String mDirectUserName = "AKIDNgssgTw1pW2NahKR4oRt9D6ofNuGgSKG";
    private String mDirectPassword = "085Nmo6yhgR/TMjSPfFWP+TEVrggjVNFtAyvZUCxp0U=";
    //token认证模式参数
    private String mTokenMqttHost = "mqtt-5oo05hhn8.ap-guangzhou.mqtt.tencentcloudmq.com";
    private String mTokenProductKey = "mqtt-5oo05hhn8";
    private String mTokenProductId = "iot-kaqvlhxc";
    private String mTokenDeviceName = "test_android_2";
    private String mTokenDeviceSecret = "4a3a3b49c5103f8d4cfea154169f6b25";
```
#### 运行Demo

在ConnectionFragment中填好配置参数后，编译运行Demo。选择连接模式并连接成功后，就可以订阅和发布topic。

### SDK说明

腾讯云iotsuite Android SDK提供mqtt connect、disconnec、subscribe、unsubscribe、publish 能力，另外提供失败重连的参数配置，相应的调用示例可以参见Demo中的Connection.java。

#### mqtt部分

- 建立mqtt连接

        void connect(final IMqttConnectStateCallback connectStateCallback)
    
    IMqttConnectStateCallback用于监听连接状态变化。

- 断开mqtt连接

        void disconnect()
    
- 订阅topic

        void subscribe(final MqttSubscribeRequest request)

    MqttSubscribeRequest中可设置回调，监听请求结果。

- 取消订阅topic

        void unSubscribe(final MqttUnSubscribeRequest request)

    MqttUnSubscribeRequest中可设置回调，监听请求结果。

- 向topic发布消息

        void publish(final MqttPublishRequest request)

    MqttPublishRequest中可设置回调，监听请求结果。

- 监听已订阅topic发来的消息

        void setMqttMessageListener(IMqttMessageListener listener)

#### 影子操作

- 获取影子

        void getShadow()

- 设备端更新影子

        void reportShadow(JSONObject report)

- 删除影子部分属性或全部属性

        void deleteShadow(JSONObject delete)

- 设置监听影子消息，必须在connect调用之后

        void setShadowMessageListener(IShadowListener shadowListener)

#### 其他

- 设置log等级

        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);

- 自动重连逻辑

    SDK可配置minRetryTime、maxRetryTime、maxRetryTimes。

    两次重连之间的时间间隔retryInterval按2的幂次方增长，并且满足 minRetryTime<=retryInterval<=maxRetryTime，当重连次数达到maxRetryTimes后，停止重连。