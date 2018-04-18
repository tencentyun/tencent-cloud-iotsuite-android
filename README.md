
## 介绍

IotSDKDevice是腾讯云iotsuite的设备端SDK，DeviceSample是设备端的Demo。

## SDK嵌入说明

#### 下载SDK及Demo

腾讯云iotsuite Android版SDK下载：

    git clone https://github.com/tencentyun/tencent-cloud-iotsuite-android

#### 两种引入方式

1、直接依赖jcenter上的库（建议）

    implementation 'com.tencent.qcloud:iot-android-sdk-device:2.0.0'

2、下载SDK，然后本地依赖

    implementation project(path: ':IotSDKDevice')

#### 注意事项

用户在自己的工程中嵌入IotSDKDevice，DeviceSample中com.tencent.qcloud.iot.sample.qcloud包里面的文件都需要移植过去。

DeviceSample中Connection.java封装了对IotSDKDevice的调用，以及对com.tencent.qcloud.iot.sample.qcloud包内类的调用，直接基于Connection.java做二次开发会更方便。

## 根据设备信息修改Demo

### 一、python脚本生成java类

为了方便用户接入，减少用户二次开发工作量，iotsuite抽象出数据点概念，需要用户按照以下方式生成对应的java类。

开发脚本时使用的python版本是2.7.10.

#### 下载与product对应的数据模板文件(.json)

到 [iotsuite控制台](https://console.cloud.tencent.com/iotsuite/product)，进入设备所属产品的管理页面，配置数据点，然后导出，得到json文件。
把得到的json文件。

#### 生成JsonFileData.java

假设文件名是test.json，放置到 DeviceSample/tools/iot_code_generator/test.json，然后运行：

    cd DeviceSample/tools/iot_code_generator
    python ./tc_iot_code_generator.py -c ./test.json

即可在当前目录下生成JsonFileData.java。

JsonFileData.java中包含了产品id、key等信息，可以通过调用get方法获取。还包括与所配置数据点对应的数据模板类DataTemplate，可以方便地监听数据点的变化。

### 二、修改、编译运行Demo

使用 Android Studio 打开工程 tencent-cloud-iotsuite-android。

#### 用户修改项

根据不同的数据点配置，需要用户做以下修改，才能编译运行。

1、用生成的JsonFileData.java替换demo中的JsonFileData.java（com.tencent.qcloud.iot.sample.qcloud.JsonFileData）。
如果是放到用户自己的项目工程目录下，需要根据用户工程的包名结构修改JsonFileData的包名。

2、JsonFileData包含的是产品维度的信息，不包含设备维度的信息，这里需要在ConnectionFragment中配置deviceName和deviceSecret（从iotsuite控制台上获取）。

    private String mDeviceName = "token_test_1";
    private String mDeviceSecret = "1e3acdf1242b17b11f353505d75cbcfa";

3、ConnectionFragment中，重新实现用于监听服务端控制消息的接口类JsonFileData.IDataControlListener，示例如下：

```
        //监听来自服务端的控制消息,每个接口，处理完成后需要返回true，才能够正确修改上报设备数据。
        mConnection.setDataControlListener(new JsonFileData.IDataControlListener() {
            @Override
            public boolean onControlDeviceSwitch(boolean deviceSwitch) {
                Log.d(TAG, "onControlDeviceSwitch: " + deviceSwitch);
                activity.showToast("onControlDeviceSwitch: " + deviceSwitch);
                return true;
            }

            @Override
            public boolean onControlColor(JsonFileData.Color color) {
                Log.d(TAG, "onControlColor: " + color);
                activity.showToast("onControlColor: " + color);
                return true;
            }

            @Override
            public boolean onControlBrightness(int brightness) {
                Log.d(TAG, "onControlBrightness: " + brightness);
                activity.showToast("onControlBrightness: " + brightness);
                return true;
            }
        });
```

4、DataFragment中，根据JsonFileData.DataTemplate提供的set接口，在测试方法testSetDataTemplate中设置数据点的值，示例如下：

```
    public void testSetDataTemplate() {
        JsonFileData.DataTemplate dataTemplate = mConnection.getJsonFileData().getDataTemplate();
        //当本地控制设备状态变化时，需要调用接口设置数据点的值。
        //第二个参数表示是否立即上报服务端。如果需要设置多个数据点的值，建议只在修改最后一个数据点值时设为true，这样就只会上报一次，避免频繁上报。
        dataTemplate.setBrightnessByUser(15, false);
        dataTemplate.setColorByUser(JsonFileData.Color.BLUE, true);
    }
```

#### 运行Demo

接下来编译运行Demo。打开connect开关并connect成功后，就可以：

1、订阅和发布topic。

2、监听来自服务端的控制消息，并做处理。（处理完后，SDK会自动上报到服务器）

3、用户主动修改数据点值，触发上报到服务端。

## SDK说明

腾讯云IotSDKDevice提供mqtt connect、disconnec、subscribe、unsubscribe、publish 能力，另外提供失败重连的参数配置，相应的调用示例可以参见Demo中的Connection.java。

### mqtt部分

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


#### 其他

- 设置log等级

        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);

- 自动重连逻辑

    SDK可配置minRetryTime、maxRetryTime、maxRetryTimes。

    两次重连之间的时间间隔retryInterval按2的幂次方增长，并且满足 minRetryTime<=retryInterval<=maxRetryTime，当重连次数达到maxRetryTimes后，停止重连。