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
* art下rom反编译技巧
 
在android 5.0之后，系统的/system/framework目录下的jar包里面已经不包含代码了，只是一些空的jar文件。代码被存储在oat文件或者odex文件中，其中framework的代码主要集中在：

```
1. /system/framework/oat/arm64

├── am.odex
├── android.test.runner.odex
├── applist.odex
├── appwidget.odex
├── bmgr.odex
├── bu.odex
├── com.android.contacts.separated.odex
├── com.android.future.usb.accessory.odex
├── com.android.location.provider.odex
├── com.android.media.remotedisplay.odex
├── com.android.mediadrm.signer.odex
├── com.android.nfc_extras.odex
├── com.google.android.maps.odex
├── com.google.android.media.effects.odex
├── com.gsma.services.nfc.odex
├── com.hisi.perfhub.odex
├── com.huawei.iconnect.wearable.odex
├── com.huawei.launcher.separated.odex
├── com.huawei.systemmanager.separated.odex
├── com.huawei.theme.stat.odex
├── content.odex
├── dpm.odex
├── ethernet-service.odex
├── hid.odex
├── hwServices.odex
├── hwWifi-service.odex
├── hwcustServices.odex
├── hwcustwifi-service.odex
├── hwpush.odex
├── hwtransition.odex
├── ime.odex
├── input.odex
├── javax.obex.odex
├── jcifs-1.3.17-dex.odex
├── media_cmd.odex
├── monkey.odex
├── pm.odex
├── requestsync.odex
├── services.odex
├── settings.odex
├── sm.odex
├── svc.odex
├── telecom.odex
├── uiautomator.odex
├── wifi-service.odex
└── wm.odex

2. /system/framework/arm64
.
├── boot-apache-xml.art
├── boot-apache-xml.oat
├── boot-bouncycastle.art
├── boot-bouncycastle.oat
├── boot-conscrypt.art
├── boot-conscrypt.oat
├── boot-core-junit.art
├── boot-core-junit.oat
├── boot-core-libart.art
├── boot-core-libart.oat
├── boot-ext.art
├── boot-ext.oat
├── boot-framework.art
├── boot-framework.oat
├── boot-hwEmui.art
├── boot-hwEmui.oat
├── boot-hwTelephony-common.art
├── boot-hwTelephony-common.oat
├── boot-hwaps.art
├── boot-hwaps.oat
├── boot-hwcustEmui.art
├── boot-hwcustEmui.oat
├── boot-hwcustTelephony-common.art
├── boot-hwcustTelephony-common.oat
├── boot-hwcustframework.art
├── boot-hwcustframework.oat
├── boot-hwframework.art
├── boot-hwframework.oat
├── boot-ims-common.art
├── boot-ims-common.oat
├── boot-okhttp.art
├── boot-okhttp.oat
├── boot-org.apache.http.legacy.boot.art
├── boot-org.apache.http.legacy.boot.oat
├── boot-org.ifaa.android.manager.art
├── boot-org.ifaa.android.manager.oat
├── boot-org.simalliance.openmobileapi.art
├── boot-org.simalliance.openmobileapi.oat
├── boot-telephony-common.art
├── boot-telephony-common.oat
├── boot-voip-common.art
├── boot-voip-common.oat
├── boot.art
└── boot.oat
```

既然找到了代码的真正存储位置，只需要从这些文件中提取出代码即可，首先来看下oat文件和odex文件的本质:

```
$ file boot.oat 
boot.oat: ELF 64-bit LSB shared object, ARM aarch64, version 1 (GNU/Linux), dynamically linked, stripped

$ file services.odex 
services.odex: ELF 64-bit LSB shared object, ARM aarch64, version 1 (GNU/Linux), dynamically linked, stripped
```

可以看到odex文件和oat文件本质上都是elf文件（Linux上的一种可执行文件）。通过查看相关文档可以发现，oat或odex文件中是存在有完整的dex文件的，我们只需要从oat或odex中将dex提取出来即可。

* 在Mac或Linux平台上有个强大的工具dextra，只需要如下的命令就能从oat或odex中提取出dex文件：

```	
$ dextra -dextract services.odex 
	N OAT file (079)
	OFF: 9b4
	Dex header @0x10c7899e8 (2721 classes) at 0x9e8: /system/framework/services.jar
 	Written to system@framework@services.jar@classes.dex
	Location Length: 30
	
提取出来的dex文件：
	system@framework@services.jar@classes.dex
```

* 在Windows平台

windows平台工具：[SmaliEx] (https://github.com/testwhat/SmaliEx)
	
	PS: 对此转换过程可以写个脚本oat2dex.sh放在framework目录下，来完成批量转换工作：
	
	files=`find arm64 oat/arm64 -name "*.oat" -o -name "*.odex"`
	if [ -d "tmp" ]; then
		echo 文件夹存在
		rm -rf tmp
	fi
	mkdir tmp
	echo $PWD
	prefix=$PWD
	cd tmp
	echo "==========从oat或odex中提取dex================="
	for file in $files
	do
		dextra -dextract "$prefix/$file"
	done
dex 反编译就有很多工具来实现了，这里推荐 `jadx`.


