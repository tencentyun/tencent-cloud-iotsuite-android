#!/usr/bin/python
# coding=utf-8

import os
import argparse
import json
import shutil
import sys
from tempfile import mkstemp
import re

reload(sys)
sys.setdefaultencoding("utf-8")


class Const:
    PRODUCT_ID = "product_id"
    PRODUCT_KEY = "product_key"
    PRODUCT_SECRET = "product_secret"
    APP_ID = "app_id"
    REGION = "region"
    PRODUCT_NAME = "name"
    DESCRIPTION = "description"
    DOMAIN = "domain"
    STANDARD = "standard"
    AUTH_TYPE = "auth_type"
    DATA_TEMPLATE = "data_template"
    DELETED = "deleted"
    MESSAGE = "message"
    CREATE_TIME = "create_time"
    UPDATE_TIME = "update_time"
    USERNAME = "username"
    PASSWORD = "password"

    DT_NAME = "name"
    DT_MODE = "mode"
    DT_TYPE = "type"
    DT_RANGE = "range"

    DT_TYPE_ENUM = "enum"
    DT_TYPE_NUMBER = "number"
    DT_TYPE_BOOL = "bool"

    DT_DICT_KEY_CONST_STRING = "key_const_string"
    DT_DICT_KEY_FIELD = "key_field"
    DT_DICT_KEY_GET_METHOD = "key_get_method"
    DT_DICT_KEY_SET_METHOD = "key_set_method"
    DT_DICT_KEY_USER_SET_METHOD = "key_user_set_method"
    DT_DICT_KEY_TO_JSON_METHOD = "key_to_json_method"
    DT_DICT_KEY_ON_CONTROL_METHOD = "key_on_control_method"
    DT_DICT_KEY_ENUM = "key_enum"
    DT_DICT_KEY_ENUM_INDEX_METHOD = "key_enum_index_method"
    DT_DICT_KEY_ENUM_CLASS = "key_enum_class"
    DT_DICT_KEY_DATA_CONTROL_LISTENER = "key_data_control_listener"

class Util:
    @staticmethod
    def convertToStubName(name):
        return "${" + name + "}"

    @staticmethod
    def wrapQuote(str):
        return "\"" + str + "\""

    @staticmethod
    def genSpaceStr(spaceCount):
        return "".join(" " for i in range(spaceCount))

    @staticmethod
    def genNewLineAndSpaceStr(spaceCount):
        return "\n" + Util.genSpaceStr(spaceCount)

    @staticmethod
    def getValueByKey(object, key):
        if key not in object:
            print("错误：{} 属性字段不存在，请检查文件格式是否合法".format(key))
        return object[key]

    #根据传入的python dict，替换字符串
    @staticmethod
    def replaceByObject(str, replaceObject):
        replaceDict = dict((re.escape(k), v) for k, v in replaceObject.iteritems())
        pattern = re.compile("|".join(replaceDict.keys()))
        str = pattern.sub(lambda m: replaceDict[re.escape(m.group(0))], str)
        return str


class AndroidUtil:
    #修复命名，例如：不能以数字开头
    @staticmethod
    def repairName(name):
        if str(name[0]).isdigit():
            name = "repaired_" + name
        return name

    #常量命名风格
    @staticmethod
    def convertToConstStringStyle(name):
        name = AndroidUtil.repairName(name)
        temp = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
        return temp.upper()

    #成员变量驼峰式命名风格，例如mColor
    @staticmethod
    def convertToCamelFieldStyle(name):
        name = AndroidUtil.repairName(name)
        return "m" + AndroidUtil.convertToClassNameStyle(name)

    #临时变量命名风格，例如color
    @staticmethod
    def convertToVariableStyle(name):
        name = AndroidUtil.repairName(name)
        temp = AndroidUtil.convertToClassNameStyle(name)
        return temp[0].lower() + temp[1:]

    #类命名风格，例如Color
    @staticmethod
    def convertToClassNameStyle(name):
        name = AndroidUtil.repairName(name)
        return ''.join(x.capitalize() or '_' for x in name.split('_'))

    #生成get方法的方法名，例如getColor
    @staticmethod
    def genGetMethodName(name, javaType):
        action = "is" if (javaType == "boolean") else "get"
        return action + AndroidUtil.convertToClassNameStyle(name)

    #生成set方法的方法名，例如setColor
    @staticmethod
    def genSetMethodName(name):
        return "set{}".format(AndroidUtil.convertToClassNameStyle(name))

    #生成枚举类索引方法的方法名，例如getColorByIndex
    @staticmethod
    def genEnumIndexMethodName(name):
        return "get{}ByIndex".format(AndroidUtil.convertToClassNameStyle(name))

    #生成成员变量的onControl方法名，例如onControlColor
    @staticmethod
    def genOnControlMethodName(name):
        return "onControl{}".format(AndroidUtil.convertToClassNameStyle(name))

class DataTemplate:
    def convertToJavaType(self, type, name, isEnum, range):
        if isEnum:
            return AndroidUtil.convertToClassNameStyle(name)
        else:
            if type == Const.DT_TYPE_BOOL:
                return "boolean"
            elif type == Const.DT_TYPE_NUMBER:
                for index, val in enumerate(range):
                    if isinstance(val, float):
                        return "double"
                return "int"

    #生成get方法，当成员变量不可读时，用private
    def genGetMethod(self, methodName, javaType, fieldName, readable):
        access = "public" if readable else "private"
        methodString = "{} {} {}() {{".format(access, javaType, methodName)
        methodString += Util.genNewLineAndSpaceStr(12) + "return {};".format(fieldName)
        methodString += Util.genNewLineAndSpaceStr(8) + "}"
        return methodString

    #生成set方法
    def genSetMethod(self, methodName, javaType, fieldName, variableName, range, isNumber):
        methodString = "private void {}({} {}) {{".format(methodName, javaType, variableName)
        if isNumber:
            methodString += Util.genNewLineAndSpaceStr(12) + "final {} min = {};".format(javaType, range[0])
            methodString += Util.genNewLineAndSpaceStr(12) + "final {} max = {};".format(javaType, range[1])
            methodString += Util.genNewLineAndSpaceStr(12) + "if ({} < min || {} > max) {{".format(variableName, variableName)
            methodString += Util.genNewLineAndSpaceStr(16) + "throw new IllegalArgumentException(\"out of range [\" + min + \", \" + max + \"]\");"
            methodString += Util.genNewLineAndSpaceStr(12) + "}"
        methodString += Util.genNewLineAndSpaceStr(12) + "{} = {};".format(fieldName, variableName)
        methodString += Util.genNewLineAndSpaceStr(12) + "onLocalDataChange();"
        methodString += Util.genNewLineAndSpaceStr(8) + "}"
        return methodString

    #生成暴露给用户的set方法，当成员变量不可写时，用private
    def genUserSetMethod(self, methodName, javaType, setMethodName, constStringName, fieldName, variableName, isEnum, writeable):
        access = "public" if writeable else "private"
        value = (fieldName + ".getIndex()") if isEnum else fieldName
        methodString = "{} void {}({} {}, boolean commit) {{".format(access, methodName, javaType, variableName)
        methodString += Util.genNewLineAndSpaceStr(12) + "{}({});".format(setMethodName, variableName)
        methodString += Util.genNewLineAndSpaceStr(12) + "onUserChangeData(genJsonObject({}, {}), commit);".format(constStringName, value)
        methodString += Util.genNewLineAndSpaceStr(8) + "}"
        return methodString

    #生成toJson方法中的片段
    def genToJsonFragment(self, constStringName, fieldName, isEnum):
        value = (fieldName + ".getIndex()") if isEnum else fieldName
        return "object.put({}, {});".format(constStringName, value)

    #生成onControl方法中的片段
    def genOnControlFragment(self, constStringName, variableName, setMethodName, enumIndexMethodName, onControlMethodName, javaType, isEnum, isNumber):
        methodFragment = "case {}:".format(constStringName)
        if isEnum:
            methodFragment += Util.genNewLineAndSpaceStr(20) + "{} {} = {}((int) obj);".format(javaType, variableName, enumIndexMethodName)
            methodFragment += Util.genNewLineAndSpaceStr(20) + "if ({} == null) {{".format(variableName)
            methodFragment += Util.genNewLineAndSpaceStr(24) + "break;"
            methodFragment += Util.genNewLineAndSpaceStr(20) + "}"
        elif isNumber:
            methodFragment += Util.genNewLineAndSpaceStr(20) + "{} {} = ((Number) obj).{}Value();".format(javaType, variableName, javaType)
        else:
            methodFragment += Util.genNewLineAndSpaceStr(20) + "{} {} = ({}) obj;".format(javaType, variableName, javaType)

        methodFragment += Util.genNewLineAndSpaceStr(20) + "result = mDataControlListener.{}({});".format(onControlMethodName, variableName)
        methodFragment += Util.genNewLineAndSpaceStr(20) + "if (result) {"
        methodFragment += Util.genNewLineAndSpaceStr(25) + "{}({});".format(setMethodName, variableName)
        methodFragment += Util.genNewLineAndSpaceStr(20) + "}"
        methodFragment += Util.genNewLineAndSpaceStr(20) + "break;"
        return methodFragment

    #生成枚举类的索引方法，例如getColorByIndex
    def genEnumIndexMethod(self, className, enumIndexMethodName, variableName, isEnum):
        if not isEnum:
            return ""
        methodString = "private {} {}(int index) {{".format(className, enumIndexMethodName)
        methodString += Util.genNewLineAndSpaceStr(12) + "for ({} {} : {}.values()) {{".format(className, variableName, className)
        methodString += Util.genNewLineAndSpaceStr(16) + "if ({}.getIndex() == index) {{".format(variableName)
        methodString += Util.genNewLineAndSpaceStr(20) + "return {};".format(variableName)
        methodString += Util.genNewLineAndSpaceStr(16) + "}"
        methodString += Util.genNewLineAndSpaceStr(12) + "}"
        methodString += Util.genNewLineAndSpaceStr(12) + "Log.e(TAG, \"not value map to index = \" + index);"
        methodString += Util.genNewLineAndSpaceStr(12) + "return null;"
        methodString += Util.genNewLineAndSpaceStr(8) + "}"
        return methodString

    #生成枚举类
    def genEnumClass(self, className, range, isEnum):
        if not isEnum:
            return ""
        classString = "public enum {} {{".format(className)
        for index, name in enumerate(range):
            classString += Util.genNewLineAndSpaceStr(8) + "{}({}, \"{}\")".format(AndroidUtil.convertToConstStringStyle(name), index, name)
            classString += ";\n" if (index == len(range) - 1) else ","
        classString += Util.genNewLineAndSpaceStr(8) + "private int mIndex;"
        classString += Util.genNewLineAndSpaceStr(8) + "private String mName;\n"
        classString += Util.genNewLineAndSpaceStr(8) + "{}(int index, String name) {{".format(className)
        classString += Util.genNewLineAndSpaceStr(12) + "mIndex = index;"
        classString += Util.genNewLineAndSpaceStr(12) + "mName = name;"
        classString += Util.genNewLineAndSpaceStr(8) + "}\n"
        classString += Util.genNewLineAndSpaceStr(8) + "public int getIndex() {"
        classString += Util.genNewLineAndSpaceStr(12) + "return mIndex;"
        classString += Util.genNewLineAndSpaceStr(8) + "}\n"
        classString += Util.genNewLineAndSpaceStr(8) + "public String getName() {"
        classString += Util.genNewLineAndSpaceStr(12) + "return mName;"
        classString += Util.genNewLineAndSpaceStr(8) + "}"
        classString += Util.genNewLineAndSpaceStr(4) + "}"
        return classString

    #生成成员变量的onControl接口
    def genDataControlListener(self, onControlMethodName, variableName, javaType):
        return "boolean {}({} {});".format(onControlMethodName, javaType, variableName)

    #解析json文档中的DataTemplate，根据各个属性生成所需要的代码，放入list中
    def parseDataTemplate(self, dataTemplateObject):
        dataTemplateItems = []
        for item in dataTemplateObject:
            itemDict = {}
            itemName = item[Const.DT_NAME]
            itemType = item[Const.DT_TYPE]
            itemRange = item[Const.DT_RANGE]
            isEnum = (itemType == Const.DT_TYPE_ENUM)
            isNumber = (itemType == Const.DT_TYPE_NUMBER)
            isBool = (itemType == Const.DT_TYPE_BOOL)
            javaType = self.convertToJavaType(itemType, itemName, isEnum, itemRange)
            className = AndroidUtil.convertToClassNameStyle(itemName)
            fieldName = AndroidUtil.convertToCamelFieldStyle(itemName)
            variableName = AndroidUtil.convertToVariableStyle(itemName)
            constStringName = AndroidUtil.convertToConstStringStyle(itemName)
            getMethodName = AndroidUtil.genGetMethodName(itemName, javaType)
            setMethodName = AndroidUtil.genSetMethodName(itemName)
            userSetMethodName = setMethodName + "ByUser"
            enumIndexMethodName = AndroidUtil.genEnumIndexMethodName(itemName)
            onControlMethodName = AndroidUtil.genOnControlMethodName(itemName)
            readable = "r" in str(item[Const.DT_MODE])
            writeable = "w" in str(item[Const.DT_MODE])
            initValue = ""
            if isBool:
                initValue = "false"
            elif isEnum:
                initValue = "{}.values()[0]".format(className)
            else:
                initValue = itemRange[0]

            itemDict[Const.DT_DICT_KEY_CONST_STRING] = "private static final String {} = {};".format(constStringName, Util.wrapQuote(itemName))
            itemDict[Const.DT_DICT_KEY_FIELD] = "private {} {} = {};".format(javaType, fieldName, initValue)
            itemDict[Const.DT_DICT_KEY_GET_METHOD] = self.genGetMethod(getMethodName, javaType, fieldName, readable)
            itemDict[Const.DT_DICT_KEY_SET_METHOD] = self.genSetMethod(setMethodName, javaType, fieldName, variableName, itemRange, isNumber)
            itemDict[Const.DT_DICT_KEY_USER_SET_METHOD] = self.genUserSetMethod(userSetMethodName, javaType, setMethodName, constStringName, fieldName, variableName, isEnum, writeable)
            itemDict[Const.DT_DICT_KEY_TO_JSON_METHOD] = self.genToJsonFragment(constStringName, fieldName, isEnum)
            itemDict[Const.DT_DICT_KEY_ON_CONTROL_METHOD] = self.genOnControlFragment(constStringName, variableName, setMethodName, enumIndexMethodName, onControlMethodName,javaType, isEnum, isNumber)
            itemDict[Const.DT_DICT_KEY_ENUM_INDEX_METHOD] = self.genEnumIndexMethod(className, enumIndexMethodName, variableName, isEnum)
            itemDict[Const.DT_DICT_KEY_ENUM_CLASS] = self.genEnumClass(className, itemRange, isEnum)
            itemDict[Const.DT_DICT_KEY_DATA_CONTROL_LISTENER] = self.genDataControlListener(onControlMethodName, variableName, javaType)

            dataTemplateItems.append(itemDict)
        return dataTemplateItems

def generateFile(outFilePath, configObject):
    tempFd, tempOutFilePath = mkstemp()
    outFile = open(outFilePath, "r")
    tempOutFile = open(tempOutFilePath, "w")

    generateCode(outFile, tempOutFile, configObject)

    outFile.close()
    tempOutFile.close()
    os.close(tempFd)
    os.remove(outFilePath)
    shutil.move(tempOutFilePath, outFilePath)

def generateCode(outFile, tempOutFile, configObject):
    #JsonFileData成员变量代码
    configFieldObject = {Util.convertToStubName(Const.PRODUCT_ID): Util.wrapQuote(configObject[Const.PRODUCT_ID]),
                     Util.convertToStubName(Const.PRODUCT_KEY): Util.wrapQuote(configObject[Const.PRODUCT_KEY]),
                     Util.convertToStubName(Const.PRODUCT_SECRET): Util.wrapQuote(configObject[Const.PRODUCT_SECRET]),
                     Util.convertToStubName(Const.APP_ID): str(configObject[Const.APP_ID]),
                     Util.convertToStubName(Const.REGION): Util.wrapQuote(configObject[Const.REGION]),
                     Util.convertToStubName(Const.PRODUCT_NAME): Util.wrapQuote(configObject[Const.PRODUCT_NAME]),
                     Util.convertToStubName(Const.DESCRIPTION): Util.wrapQuote(configObject[Const.DESCRIPTION]),
                     Util.convertToStubName(Const.DOMAIN): Util.wrapQuote(configObject[Const.DOMAIN]),
                     Util.convertToStubName(Const.STANDARD): str(configObject[Const.STANDARD]),
                     Util.convertToStubName(Const.AUTH_TYPE): str(configObject[Const.AUTH_TYPE]),
                     Util.convertToStubName(Const.DELETED): str(configObject[Const.DELETED]),
                     Util.convertToStubName(Const.MESSAGE): Util.wrapQuote(configObject[Const.MESSAGE]),
                     Util.convertToStubName(Const.CREATE_TIME): Util.wrapQuote(configObject[Const.CREATE_TIME]),
                     Util.convertToStubName(Const.UPDATE_TIME): Util.wrapQuote(configObject[Const.UPDATE_TIME]),
                     Util.convertToStubName(Const.USERNAME): Util.wrapQuote(configObject[Const.USERNAME]),
                     Util.convertToStubName(Const.PASSWORD): Util.wrapQuote(configObject[Const.PASSWORD]),
                     }

    dataTemplateInstance = DataTemplate()
    dataTemplateObject = configObject[Const.DATA_TEMPLATE]
    #basestring for python 2.x, str for python 3.x
    if isinstance(dataTemplateObject, basestring) or isinstance(dataTemplateObject, str):
        dataTemplateObject = json.loads(dataTemplateObject)
    dataTemplateItems = dataTemplateInstance.parseDataTemplate(dataTemplateObject)

    constStringCode = ""
    fieldCode = ""
    accessMethodCode = ""
    toJsonMethodCode = ""
    onControlMethodCode = ""
    enumIndexMethodCode = ""
    enumClassCode = ""
    onControlListenerCode = ""

    for item in dataTemplateItems:
        #DataTemplate常量代码
        constStringCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_CONST_STRING]
        #DataTemplate成员变量代码
        fieldCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_FIELD]
        #DataTemplate get和set方法代码
        if item[Const.DT_DICT_KEY_GET_METHOD]:
            accessMethodCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_GET_METHOD] + "\n"
        if item[Const.DT_DICT_KEY_SET_METHOD]:
            accessMethodCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_SET_METHOD] + "\n"
        if item[Const.DT_DICT_KEY_USER_SET_METHOD]:
            accessMethodCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_USER_SET_METHOD] + "\n"
        #DataTemplate toJson方法片段代码
        toJsonMethodCode += Util.genNewLineAndSpaceStr(16) + item[Const.DT_DICT_KEY_TO_JSON_METHOD]
        #DataTemplate onControl方法片段代码
        onControlMethodCode += Util.genNewLineAndSpaceStr(16) + item[Const.DT_DICT_KEY_ON_CONTROL_METHOD]

        if item[Const.DT_DICT_KEY_ENUM_INDEX_METHOD]:
            #枚举类索引方法代码
            enumIndexMethodCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_ENUM_INDEX_METHOD] + "\n"
            #枚举类代码
            enumClassCode += Util.genNewLineAndSpaceStr(4) + item[Const.DT_DICT_KEY_ENUM_CLASS] + "\n"
        #DataTemplate IDataControlListener接口代码
        onControlListenerCode += Util.genNewLineAndSpaceStr(8) + item[Const.DT_DICT_KEY_DATA_CONTROL_LISTENER]

    dataTemplateCodeObject = {
        Util.convertToStubName("JAVA_CODE_CONST_FIELD_STRING"): constStringCode,
        Util.convertToStubName("JAVA_CODE_FIELD"): fieldCode,
        Util.convertToStubName("JAVA_CODE_ACCESS_METHOD"): accessMethodCode,
        Util.convertToStubName("JAVA_CODE_TO_JSON_METHOD"): toJsonMethodCode,
        Util.convertToStubName("JAVA_CODE_ON_CONTROL_METHOD"): onControlMethodCode,
        Util.convertToStubName("JAVA_CODE_ENUM_INDEX_METHOD"): enumIndexMethodCode,
        Util.convertToStubName("JAVA_CODE_ENUM_CLASS"): enumClassCode,
        Util.convertToStubName("JAVA_CODE_ON_CONTROL_LISTENER"): onControlListenerCode,
    }

    replaceObject = dict(configFieldObject.items() + dataTemplateCodeObject.items())
    #将JAVA文件中的标记字段替换为python生成的代码字符串
    for line in outFile.readlines():
        tempOutFile.write(Util.replaceByObject(line, replaceObject))


def getConfigObject(configFilePath):
    configFile = open(configFilePath, "r")
    try:
        configObject = json.load(configFile)
        print("加载 {} 文件成功".format(configFilePath))
    except ValueError as e:
        print("错误：文件格式非法，请检查 {} 文件是否是 JSON 格式。".format(configFilePath))
        return -1
    configFile.close()
    return configObject


def main():
    parser = argparse.ArgumentParser(description='Iotsuite device data code generator.')
    parser.add_argument('-c', dest='config', metavar='iot_product.json', required=True,
                        help='配置文件本地路径，该文件可从控制台导出到本地： https://console.qcloud.com/iotsuite/product')

    args = parser.parse_args()
    configFilePath = args.config
    templateFilePath = os.path.join(os.getcwd(), "template", "JsonFileData.java")
    outFilePath = os.path.join(os.getcwd(), "JsonFileData.java")

    if not os.path.isfile(configFilePath):
        print("错误：{} 文件不存在，请重新指定 iot_product.json 文件路径".format(configFilePath))
        return -1

    if not os.path.isfile(templateFilePath):
        print("错误：{} 文件不存在".format(templateFilePath))
        return -1

    shutil.copy(templateFilePath, outFilePath)

    configObject = getConfigObject(configFilePath)
    generateFile(outFilePath, configObject)


if __name__ == "__main__":
    main()
