# huawei framework source

## version:Honor5C, Android 7.0
本项目为研究hwframework层的源码。

```
adb pull /system/framework .

adb shell dumpsys activity top // 得到apk的packageName

adb shell pm path {packageName} //得到apk的安装目录 apkPath eg:/system/priv-app/xxx/xxxx.apk

adb pull ../{apkPath} .   //从rom中提取apk文件

dextra -dextract *.oat(或*.odex) //从oat文件或odex文件中提取dex

jadx d out --show-bad-code *.dex //转换dex为java
```