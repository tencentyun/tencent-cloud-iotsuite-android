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
    DATA_TEMPLATE = "data_template"

    DT_NAME = "name"
    DT_MODE = "mode"
    DT_TYPE = "type"
    DT_RANGE = "range"

    DT_TYPE_ENUM = "enum"
    DT_TYPE_NUMBER = "number"
    DT_TYPE_BOOL = "bool"
    DT_TYPE_STRING = "string"

    DT_DICT_KEY_CONST_STRING = "key_const_string"
    DT_DICT_KEY_ENUM_CONST = "key_enum_const"


class Util:
    @staticmethod
    def convertToStubName(name):
        return "${" + name + "}"

    @staticmethod
    def wrapQuote(str):
        return "\"" + str + "\""

    @staticmethod
    def jsonOpt(jsonObject, key, default):
        return jsonObject[key] if dict(jsonObject).has_key(key) else default

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

    # 根据传入的python dict，替换字符串
    @staticmethod
    def replaceByObject(str, replaceObject):
        replaceDict = dict((re.escape(k), v) for k, v in replaceObject.iteritems())
        pattern = re.compile("|".join(replaceDict.keys()))
        str = pattern.sub(lambda m: replaceDict[re.escape(m.group(0))], str)
        return str


class AndroidUtil:
    # 修复命名，例如：不能以数字开头
    @staticmethod
    def repairName(name):
        if str(name[0]).isdigit():
            name = "repaired_" + name
        return name

    # 常量命名风格
    @staticmethod
    def convertToConstStringStyle(name):
        name = AndroidUtil.repairName(name)
        temp = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
        return temp.upper()

    # 成员变量驼峰式命名风格，例如mColor
    @staticmethod
    def convertToCamelFieldStyle(name):
        name = AndroidUtil.repairName(name)
        return "m" + AndroidUtil.convertToClassNameStyle(name)

    # 临时变量命名风格，例如color
    @staticmethod
    def convertToVariableStyle(name):
        name = AndroidUtil.repairName(name)
        temp = AndroidUtil.convertToClassNameStyle(name)
        return temp[0].lower() + temp[1:]

    # 类命名风格，例如Color
    @staticmethod
    def convertToClassNameStyle(name):
        name = AndroidUtil.repairName(name)
        return ''.join(x.capitalize() or '_' for x in name.split('_'))

    # 生成get方法的方法名，例如getColor
    @staticmethod
    def genGetMethodName(name, javaType):
        action = "is" if (javaType == "boolean") else "get"
        return action + AndroidUtil.convertToClassNameStyle(name)

    # 生成set方法的方法名，例如setColor
    @staticmethod
    def genSetMethodName(name):
        return "set{}".format(AndroidUtil.convertToClassNameStyle(name))

    # 生成枚举类索引方法的方法名，例如getColorByIndex
    @staticmethod
    def genEnumIndexMethodName(name):
        return "get{}ByIndex".format(AndroidUtil.convertToClassNameStyle(name))

    # 生成成员变量的onControl方法名，例如onControlColor
    @staticmethod
    def genOnControlMethodName(name):
        return "onControl{}".format(AndroidUtil.convertToClassNameStyle(name))


class DataTemplate:
    # 生成枚举类常量
    def genEnumConstant(self, className, range, isEnum):
        if not isEnum:
            return ""
        constantString = ""
        for index, name in enumerate(range):
            constantString += Util.genNewLineAndSpaceStr(4) + "String {} = \"{}\";".format(AndroidUtil.convertToConstStringStyle(className) + "_" + AndroidUtil.convertToConstStringStyle(name), name)
        return constantString

    # 解析json文档中的DataTemplate，根据各个属性生成所需要的代码，放入list中
    def parseDataTemplate(self, dataTemplateObject):
        dataTemplateItems = []
        for item in dataTemplateObject:
            itemDict = {}
            itemName = item[Const.DT_NAME]
            itemType = item[Const.DT_TYPE]
            itemRange = item[Const.DT_RANGE]
            isEnum = (itemType == Const.DT_TYPE_ENUM)
            className = AndroidUtil.convertToClassNameStyle(itemName)
            constStringName = AndroidUtil.convertToConstStringStyle(itemName)

            itemDict[Const.DT_DICT_KEY_CONST_STRING] = "String {} = {};".format(constStringName, Util.wrapQuote(itemName))
            itemDict[Const.DT_DICT_KEY_ENUM_CONST] = self.genEnumConstant(className, itemRange, isEnum)

            dataTemplateItems.append(itemDict)
        return dataTemplateItems


def generateFile(outFilePath, configObject):
    outFile = open(outFilePath, "w+")
    generateCode(outFile, configObject)
    outFile.close()


def generateCode(outFile, configObject):
    dataTemplateInstance = DataTemplate()
    dataTemplateObject = configObject[Const.DATA_TEMPLATE]
    # basestring for python 2.x, str for python 3.x
    if isinstance(dataTemplateObject, basestring) or isinstance(dataTemplateObject, str):
        dataTemplateObject = json.loads(dataTemplateObject)
    dataTemplateItems = dataTemplateInstance.parseDataTemplate(dataTemplateObject)

    constStringCode = ""
    enumConstCode = ""

    for item in dataTemplateItems:
        # DataTemplate常量代码
        constStringCode += Util.genNewLineAndSpaceStr(4) + item[Const.DT_DICT_KEY_CONST_STRING]
        if item[Const.DT_DICT_KEY_ENUM_CONST]:
            # 枚举类代码
            enumConstCode += Util.genNewLineAndSpaceStr(4) + item[Const.DT_DICT_KEY_ENUM_CONST]

    outFile.write("public interface TCDataConstant {\n")
    outFile.write(constStringCode)
    outFile.write(enumConstCode)
    outFile.write("\n}")


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
    parser.add_argument('-c', dest='config', metavar='tc_iot_product.json', required=True,
                        help='配置文件本地路径，该文件可从控制台导出到本地： https://console.qcloud.com/iotsuite/product')

    args = parser.parse_args()
    configFilePath = args.config
    outFilePath = os.path.join(os.getcwd(), "TCDataConstant.java")

    if not os.path.isfile(configFilePath):
        print("错误：{} 文件不存在，请重新指定 tc_iot_product.json 文件路径".format(configFilePath))
        return -1

    configObject = getConfigObject(configFilePath)
    generateFile(outFilePath, configObject)


if __name__ == "__main__":
    main()
