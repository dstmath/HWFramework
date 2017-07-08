package tmsdk.fg.module.qscanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import tmsdk.common.module.qscanner.QScanConstants;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.utils.ScriptHelper;
import tmsdkobf.ea;

/* compiled from: Unknown */
final class e implements a {
    private final String Mn;
    String Mo;
    int Mp;
    String Mq;
    int Mr;
    String mName;
    int mType;

    public e(ea eaVar) {
        this.Mn = "kungfu";
        this.mName = eaVar.name;
        this.Mo = eaVar.jk;
        this.Mp = eaVar.level;
        this.Mq = eaVar.iH;
        this.Mr = eaVar.advice;
        this.mType = eaVar.type;
    }

    private int du(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mount -o remount rw /system\n");
        stringBuilder.append("chattr -i " + str + "\n");
        stringBuilder.append("rm -r " + str + "\n");
        try {
            return ScriptHelper.acquireRootAndRunScript(-1, stringBuilder.toString()) == null ? -1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private int dv(String str) {
        String charSequence = str.subSequence(0, str.length() - 1).toString();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("mount -o remount rw /system\n");
        stringBuilder.append("chattr -i " + charSequence + "\n");
        stringBuilder.append("rm -r " + charSequence + "\n");
        stringBuilder.append("mv " + str + " " + charSequence + "\n");
        try {
            return ScriptHelper.acquireRootAndRunScript(-1, stringBuilder.toString()) == null ? -1 : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private List<String> jh() {
        int i = 0;
        Object arrayList = new ArrayList();
        String[] strArr = new String[]{"/system/xbin/ccb", "/system/etc/.dhcpcd", "/system/etc/dhcpcd.lock", "/system/etc/.rild_cfg", "/data/dhcpcd.lock"};
        int length = strArr.length;
        for (int i2 = 0; i2 < length; i2++) {
            if (new File(strArr[i2]).exists()) {
                arrayList.add(strArr[i2]);
            }
        }
        if (arrayList.size() > 0) {
            arrayList.add("kungfu");
        }
        String[] strArr2 = new String[]{"/system/bin/installdd", "/system/bin/dhcpcdd", "/system/bin/bootanimationd"};
        int length2 = strArr2.length;
        while (i < length2) {
            if (new File(strArr2[i]).exists()) {
                arrayList.add(strArr2[i]);
            }
            i++;
        }
        return arrayList;
    }

    public boolean b(QScanResultEntity qScanResultEntity) {
        if (qScanResultEntity == null) {
            return false;
        }
        List list = qScanResultEntity.dirtyDataPathes;
        if (list == null || list.size() == 0) {
            return false;
        }
        boolean z;
        int i;
        int indexOf = list.indexOf("kungfu");
        if (indexOf <= 0) {
            z = false;
        } else {
            z = false;
            for (i = 0; i < indexOf; i++) {
                z = du((String) list.get(i)) == 0;
            }
        }
        int size = list.size();
        if (indexOf < size) {
            for (i = indexOf + 1; i < size; i++) {
                z = dv((String) list.get(i)) == 0;
            }
        }
        return z;
    }

    public QScanResultEntity je() {
        QScanResultEntity qScanResultEntity = new QScanResultEntity();
        Collection jh = jh();
        qScanResultEntity.special = QScanConstants.SPECIAL_KUNGFU_VIRUS;
        qScanResultEntity.shortDesc = this.Mo;
        qScanResultEntity.discription = this.Mq;
        qScanResultEntity.name = this.mName;
        if (jh != null && jh.size() > 0) {
            qScanResultEntity.type = this.mType;
            qScanResultEntity.safeLevel = this.Mp;
            qScanResultEntity.advice = this.Mr;
            qScanResultEntity.dirtyDataPathes = new ArrayList(jh);
            qScanResultEntity.needRootToHandle = true;
        } else {
            qScanResultEntity.type = 1;
            qScanResultEntity.safeLevel = 0;
        }
        return qScanResultEntity;
    }
}
