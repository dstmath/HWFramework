# huawei framework source

## version:Honor5C, Android 7.0,EMUI5.0
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
	echo "===========dex转java=============="
	files=`find . -name "*.dex"`
	for file in $files
	do
	    jadx -d out --show-bad-code $file
	done
dex 反编译就有很多工具来实现了，这里推荐 `jadx`.

ps: 8.0之后dex保存在vdex中需要用新的工具从vdex中提取dex，工具链接[vdexExtractor](https://github.com/anestisb/vdexExtractor)

* 卡刷包提取framework方法
	- system.new.dat.br 
	- system.new.dat
	- system.img

1. .br文件是google的压缩算法brotli压缩之后的产物，需要先解压缩得到原始文件。
linux下安装brotli：
`apt install brotli`

2. 解压缩.br文件
`brotli --decompress --input system.new.dat.br --output system.new.dat`

3. 提取system.img, 用到开源项目[sdat2img](https://github.com/xpirt/sdat2img)
`python sdat2img.py system.transfer.list system.new.dat system.img`

4. 挂载system.img
`mount　system.img tmp`

tmp目录下即为系统文件。

## Android 10
Android 10上面,vdex不包含dex文件了，dex文件存放在原apk中或者jar包中。

## Android 11
Android 11 新增`apex`格式文件，部分代码存储在`/system/apex`目录下面：
```
system/apex/
├── com.android.apex.cts.shim.apex
├── com.android.art.release.apex
├── com.android.i18n.apex
├── com.android.permission.apex
├── com.android.runtime.apex
├── com.android.vndk.current.apex
├── com.android.wifi.apex
├── com.google.android.adbd.apex
├── com.google.android.cellbroadcast.apex
├── com.google.android.conscrypt.apex
├── com.google.android.extservices.apex
├── com.google.android.ipsec.apex
├── com.google.android.media.apex
├── com.google.android.mediaprovider.apex
├── com.google.android.media.swcodec.apex
├── com.google.android.neuralnetworks.apex
├── com.google.android.os.statsd.apex
├── com.google.android.permission.apex
├── com.google.android.resolv.apex
├── com.google.android.sdkext.apex
├── com.google.android.tethering.apex
└── com.google.android.tzdata2.apex

```
以`com.android.wifi.apex`为例：
1. 查看文件格式：
```
file com.android.wifi.apex 

output:

com.android.wifi.apex: Java archive data (JAR)
```
2. 解压文件:
```
unzip com.android.wifi.apex -d com.android.wifi.apex_out

查看解压后的文件:

com.android.wifi.apex_out/
├── AndroidManifest.xml
├── apex_build_info.pb
├── apex_manifest.pb
├── apex_payload.img
├── apex_pubkey
├── assets
├── META-INF
└── resources.arsc

```

3. 挂载`apex_payload.img`文件:
```
sudo mount -o loop,ro apex_payload.img tmp/
```
4. 查看tmp目录下内容:
```
tmp
├── apex_manifest.pb
├── app
│   └── OsuLogin
│       └── OsuLogin.apk
├── etc
│   └── security
│       └── cacerts_wfa
│           ├── 21125ccd.0
│           ├── 674b5f5b.0
│           └── ea93cb5b.0
├── javalib
│   ├── framework-wifi.jar
│   └── service-wifi.jar
├── lost+found [error opening dir]
└── priv-app
    └── ServiceWifiResources
        └── ServiceWifiResources.apk
```
`javalib`目录下的jar即为`wifi　service`相关的源码。
ps: Android 10上的`/system/apex`目录下存放的是对应的文件夹，在文件夹下存放着相关的jar包，省去了解压挂载文件系统的步骤。
WifiServiceImpl.java所在的jar,需要查看bp文件。
https://android.googlesource.com/platform/frameworks/opt/net/wifi/+/refs/tags/android-11.0.0_r42/service/Android.bp。

