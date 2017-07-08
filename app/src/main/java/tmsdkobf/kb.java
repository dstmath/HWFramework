package tmsdkobf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.module.qscanner.QScanAdPluginEntity;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.utils.d;
import tmsdk.fg.module.qscanner.QScanResult;

/* compiled from: Unknown */
public class kb {
    public static QScanResultEntity a(QScanResult qScanResult) {
        if (qScanResult == null) {
            return null;
        }
        QScanResultEntity qScanResultEntity = new QScanResultEntity();
        qScanResultEntity.packageName = qScanResult.apkkey.pkgName;
        qScanResultEntity.softName = qScanResult.apkkey.softName;
        qScanResultEntity.version = qScanResult.apkkey.version;
        qScanResultEntity.versionCode = qScanResult.apkkey.versionCode;
        qScanResultEntity.path = qScanResult.apkkey.path;
        qScanResultEntity.apkType = qScanResult.apkkey.apkType;
        qScanResultEntity.certMd5 = qScanResult.apkkey.certMd5;
        qScanResultEntity.size = qScanResult.apkkey.size;
        qScanResultEntity.type = qScanResult.type;
        qScanResultEntity.advice = qScanResult.advice;
        qScanResultEntity.malwareid = qScanResult.malwareid;
        qScanResultEntity.name = qScanResult.name;
        qScanResultEntity.label = qScanResult.label;
        qScanResultEntity.discription = qScanResult.discription;
        qScanResultEntity.url = qScanResult.url;
        qScanResultEntity.safeLevel = qScanResult.safelevel;
        qScanResultEntity.product = qScanResult.product;
        qScanResultEntity.dexSha1 = qScanResult.dexsha1;
        qScanResultEntity.plugins = l(qScanResult.plugins);
        qScanResultEntity.name = qScanResult.name;
        qScanResultEntity.category = qScanResult.category;
        return qScanResultEntity;
    }

    public static eg a(QScanResultEntity qScanResultEntity, int i) {
        if (qScanResultEntity == null) {
            return null;
        }
        eg egVar = new eg();
        egVar.fy = new dg(qScanResultEntity.packageName, qScanResultEntity.softName, qScanResultEntity.version, qScanResultEntity.versionCode, qScanResultEntity.certMd5, qScanResultEntity.size);
        egVar.jx = 1;
        egVar.bv = qScanResultEntity.apkType == 1;
        egVar.jy = 4;
        egVar.dexSha1 = qScanResultEntity.dexSha1;
        d.e("QScanInternalUtils", "dex-sha1=" + egVar.dexSha1);
        egVar.jz = qScanResultEntity.type;
        egVar.jB = qScanResultEntity.malwareid;
        egVar.jA = qScanResultEntity.name;
        egVar.jD = i;
        if (qScanResultEntity.plugins == null || qScanResultEntity.plugins.size() == 0) {
            egVar.jE = null;
        } else {
            egVar.jE = new ArrayList(qScanResultEntity.plugins.size() + 1);
            Iterator it = qScanResultEntity.plugins.iterator();
            while (it.hasNext()) {
                egVar.jE.add(Integer.valueOf(((QScanAdPluginEntity) it.next()).id));
            }
        }
        return egVar;
    }

    public static ArrayList<QScanAdPluginEntity> l(List<co> list) {
        if (list == null) {
            return null;
        }
        ArrayList<QScanAdPluginEntity> arrayList = new ArrayList(list.size() + 1);
        for (co coVar : list) {
            QScanAdPluginEntity qScanAdPluginEntity = new QScanAdPluginEntity();
            qScanAdPluginEntity.id = coVar.id;
            qScanAdPluginEntity.type = coVar.type;
            qScanAdPluginEntity.behaviors = (((long) coVar.fv) << 32) | ((long) coVar.fu);
            qScanAdPluginEntity.banUrls = coVar.banUrls;
            qScanAdPluginEntity.banIps = coVar.banIps;
            qScanAdPluginEntity.name = coVar.name;
            arrayList.add(qScanAdPluginEntity);
        }
        return arrayList;
    }
}
