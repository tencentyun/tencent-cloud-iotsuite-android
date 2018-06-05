
## 介绍

IotSDKDevice是腾讯云iotsuite的设备端SDK，DeviceSample是设备端的Demo。

## SDK嵌入说明

#### 下载SDK及Demo

腾讯云iotsuite Android版SDK及Demo下载：

    git clone https://github.com/tencentyun/tencent-cloud-iotsuite-android

#### 两种引入方式

1、直接依赖jcenter上的库（建议）

    implementation 'com.tencent.qcloud:iot-android-sdk-device:2.2.0'

2、下载SDK，然后本地依赖

    implementation project(path: ':IotSDKDevice')

#### 注意事项

DeviceSample中Connection.java封装了对IotSDKDevice的调用，直接基于Connection.java做二次开发会更方便。

## 根据设备信息修改Demo

### 一、tc_iot/tc_iot_product.json

为了方便用户接入，减少用户二次开发工作量，iotsuite抽象出数据点概念，需要用户生成数据模板文件。

#### 下载与product对应的数据模板文件(.json)

到 [iotsuite控制台](https://console.cloud.tencent.com/iotsuite/product)，进入设备所属产品的管理页面，配置数据点，然后导出，得到json文件。

#### 重命名及放置路径

将json文件重命名为tc_iot_product.json，放置到以下路径：

    assets/tc_iot/tc_iot_product.json

### 二、修改、编译运行Demo

使用 Android Studio 打开工程 tencent-cloud-iotsuite-android。

#### 用户修改项

需要用户做以下修改，才能编译运行。

1、创建自己的Application类（demo里，class MyApplication extends Application），在onCreate中初始化SDK：

```
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化iot SDK
        TCIotDeviceService.init(this);
    }
```

同时注意，需要在AndroidManifest.xml中修改application标签的name：

    android:name=".MyApplication"

2、tc_iot_product.json包含的是产品维度的信息，不包含设备维度的信息，这里需要在ConnectionFragment中配置deviceName和deviceSecret（从iotsuite控制台上获取）。示例如下：

    private String mDeviceName = "token_test_1";
    private String mDeviceSecret = "1e3acdf1242b17b11f353505d75cbcfa";

3、ConnectionFragment中，重新实现用于监听服务端控制消息的接口类 DataTemplate.IDataControlListener，示例如下：

```
    /**
     * 监听来自服务端的控制消息。
     */
    private DataTemplate.IDataControlListener mDataControlListener = new DataTemplate.IDataControlListener() {
        @Override
        public boolean onControlDataPoint(DataPointControlPacket dataPointControlPacket) {
            switch (dataPointControlPacket.getName()) {
                case TCDataConstant.DEVICE_SWITCH:
                    boolean deviceSwitch = (boolean) dataPointControlPacket.getValue();
                    if (dataPointControlPacket.isForInit()) {
                        //isForInit()标识是否是SDK启动后的第一次设备初始化操作，如果不希望初始化操作，可以判断forInit为true时不处理。
                    }
                    if (dataPointControlPacket.isDiff()) {
                        //isDiff()标识value是否和该数据点的当前值不相等，可以做一些逻辑（比如如果相等就不处理）。
                    }
                    break;
                case TCDataConstant.COLOR:
                    String color = (String) dataPointControlPacket.getValue();
                    break;
                case TCDataConstant.BRIGHTNESS:
                    //不能直接强转成double: ((double) dataPointControlPacket.getValue())
                    double brightness = ((Number) dataPointControlPacket.getValue()).doubleValue();
                    break;
                case TCDataConstant.ALIAS_NAME:
                    String aliasName = (String) dataPointControlPacket.getValue();
                    break;
            }
            getMainActivity().showToast("onControlDataPoint: " + dataPointControlPacket.toString());
            //处理完成后需要返回true，才能够修改上报设备数据。返回false不会修改上报设备数据。
            return true;
        }
    };
```

4、DataFragment中，根据 DataTemplate 提供的set接口，在测试方法testSetDataTemplate中设置数据点的值，示例如下：

```
    public void testSetDataTemplate() {
        DataTemplate dataTemplate = mConnection.getDataTemplate();
        if (dataTemplate == null) {
            ((BaseActivity) getActivity()).showToast("please connect first");
            return;
        }
        //当本地控制设备状态变化时，需要调用接口设置数据点的值。
        //第三个参数表示是否立即上报服务端。如果需要设置多个数据点的值，建议只在修改最后一个数据点值时设为true，这样就只会上报一次，避免频繁上报。
        try {
            //数值型
            dataTemplate.setDataPointByUser(TCDataConstant.BRIGHTNESS, 15.5, false);
            //字符串型
            dataTemplate.setDataPointByUser(TCDataConstant.ALIAS_NAME, "first", false);
            //枚举型
            dataTemplate.setDataPointByUser(TCDataConstant.COLOR, TCDataConstant.COLOR_BLUE, true);
        } catch (DeviceException e) {
            Log.e(TAG, "setDataPointByUser error", e);
        }
    }
```


5、以上涉及 TCDataConstant 的常量需要用户根据tc_iot_project.json内的数据点字段自行定义的。我们也提供python脚本可以自动生成相应的定义代码：

    cd DeviceSample/tools/iot_code_generator
    python ./tc_iot_constant_generator.py -c ../../src/main/assets/tc_iot/tc_iot_product.json

会在iot_code_generator目录下生成 TCDataConstant.java 文件。

> 开发脚本时使用的python版本是2.7.10.

#### 运行Demo

接下来编译运行Demo。打开connect开关并connect成功后，就可以：

1、订阅和发布topic。

2、监听来自服务端的控制消息，并做处理。（处理完后，SDK会自动上报到服务器）

3、用户主动修改数据点值，触发上报到服务端。

## 接口说明

### IotSDKDevice部分

腾讯云IotSDKDevice提供mqtt connect、disconnec、subscribe、unsubscribe、publish 能力，另外提供失败重连的参数配置，相应的调用示例可以参见Demo中的Connection.java。

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

    支持二进制消息和字符串消息。

    MqttPublishRequest中可设置回调，监听请求结果。

- 监听已订阅topic发来的消息

        void setMqttMessageListener(IMqttMessageListener listener)

#### 其他

- 设置log等级

        QLog.setLogLevel(QLog.QLOG_LEVEL_DEBUG);

- 自动重连逻辑

    SDK可配置minRetryTime、maxRetryTime、maxRetryTimes。

    两次重连之间的时间间隔retryInterval按2的幂次方增长，并且满足 minRetryTime<=retryInterval<=maxRetryTime，当重连次数达到maxRetryTimes后，停止重连。

    ```
        //设置自动重连参数
        mTCMqttConfig.setAutoReconnect(true)
                .setMinRetryTimeMs(1000)
                .setMaxRetryTimeMs(20000)
                .setMaxRetryTimes(5000);
    ```

- 可选http请求token

    因存在设备可能无法同步网络时钟等问题导致请求https时返回证书过期问题，提供http请求token的方式：

        //请求token时默认是https，可以在此处设为Http
        mTCMqttConfig.setTokenScheme(TCConstants.Scheme.HTTP);

### 产品信息及数据点

产品信息和数据点信息包含在 JsonFileData.java 和 DataTemplate.java 中。

#### 产品信息部分

首先通过 TCIotDeviceService 类中 getJsonFileData 接口获取到 JsonFileData 对象实例，然后就可以调用 JsonFileData 中的get接口得到产品信息。示例如下：

- 获取产品信息

    ```
        JsonFileData jsonFileData = mTCIotDeviceService.getJsonFileData();
        jsonFileData.getProductId();
        jsonFileData.getProductKey();
        ......
    ```

- 根据产品信息，生成配置类实例 TCMqttConfig

        mTCMqttConfig = TCIotDeviceService.genTCMqttConfig();

#### 数据点部分

TCIotDeviceService 提供数据点功能接口。

- 监听服务端对数据点的控制消息

        void setDataControlListener(DataTemplate.IDataControlListener dataControlListener)

- 用户主动修改数据点的值

    假如用户手动控制了设备端导致设备端的状态变化，则需要修改对应数据点的值，以触发上报到服务器。示例如下：

    ```
        DataTemplate dataTemplate = mTCIotDeviceService.getDataTemplate();
        //第二个参数表示是否立即上报服务端。如果需要设置多个数据点的值，建议只在修改最后一个数据点值时设为true，这样就只会上报一次，避免频繁上报。
        dataTemplate.setDataPointByUser(TCDataConstant.BRIGHTNESS, 15.5, false);
        ......
        dataTemplate.setDataPointByUser(TCDataConstant.COLOR, TCDataConstant.COLOR_BLUE, true);
    ```

