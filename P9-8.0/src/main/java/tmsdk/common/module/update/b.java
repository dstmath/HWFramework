package tmsdk.common.module.update;

import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Hashtable;
import tmsdk.common.utils.f;
import tmsdkobf.ab;
import tmsdkobf.ae;
import tmsdkobf.im;
import tmsdkobf.jy;

class b {
    private static b Kb;
    private Hashtable<Integer, ae> Kc = new Hashtable();

    private b() {
    }

    public static synchronized b hL() {
        b bVar;
        synchronized (b.class) {
            if (Kb == null) {
                Kb = new b();
            }
            bVar = Kb;
        }
        return bVar;
    }

    public void e(UpdateInfo updateInfo) {
        ae aeVar = new ae();
        aeVar.aE = UpdateConfig.getFileIdByFileName(updateInfo.fileName);
        if (updateInfo.url != null) {
            aeVar.url = updateInfo.url;
        }
        aeVar.checkSum = updateInfo.checkSum;
        aeVar.timestamp = updateInfo.timestamp;
        aeVar.success = (byte) updateInfo.success;
        aeVar.downSize = updateInfo.downSize;
        aeVar.downType = (byte) updateInfo.downType;
        aeVar.errorCode = updateInfo.errorCode;
        aeVar.downnetType = updateInfo.downnetType;
        aeVar.downNetName = updateInfo.downNetName;
        aeVar.errorMsg = updateInfo.errorMsg;
        aeVar.rssi = updateInfo.rssi;
        aeVar.sdcardStatus = updateInfo.sdcardStatus;
        aeVar.fileSize = updateInfo.fileSize;
        this.Kc.put(Integer.valueOf(aeVar.aE), aeVar);
        f.f("update_report", "configReport info: fileId=" + aeVar.aE + " url=" + aeVar.url + " checkSum=" + aeVar.checkSum + " timestamp=" + aeVar.timestamp + " success=" + aeVar.success + " downSize=" + aeVar.downSize + " downType=" + aeVar.downType + " errorCode=" + aeVar.errorCode + " downnetType=" + aeVar.downnetType + " downNetName=" + aeVar.downNetName + " errorMsg=" + aeVar.errorMsg + " rssi=" + aeVar.rssi + " sdcardStatus=" + aeVar.sdcardStatus + " fileSize=" + aeVar.fileSize);
    }

    public void en() {
        f.d("update_report", "report, size: " + this.Kc.size());
        if (this.Kc.size() != 0) {
            JceStruct abVar = new ab();
            abVar.aA = new ArrayList(this.Kc.values());
            this.Kc.clear();
            f.d("update_report", "before send shark");
            im.bK().a(109, abVar, null, 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    f.f("update_report", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                    if (jceStruct == null) {
                        f.f("update_report", "onFinish() null");
                    }
                }
            });
        }
    }
}
