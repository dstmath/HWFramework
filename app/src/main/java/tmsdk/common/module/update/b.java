package tmsdk.common.module.update;

import java.util.ArrayList;
import java.util.Hashtable;
import tmsdk.common.utils.d;
import tmsdkobf.aa;
import tmsdkobf.fs;
import tmsdkobf.jq;
import tmsdkobf.lg;
import tmsdkobf.x;

/* compiled from: Unknown */
class b {
    private static b JZ;
    private Hashtable<Integer, aa> Ka;

    private b() {
        this.Ka = new Hashtable();
    }

    public static synchronized b hN() {
        b bVar;
        synchronized (b.class) {
            if (JZ == null) {
                JZ = new b();
            }
            bVar = JZ;
        }
        return bVar;
    }

    public void d(UpdateInfo updateInfo) {
        aa aaVar = new aa();
        aaVar.am = UpdateConfig.getFileIdByFileName(updateInfo.fileName);
        if (updateInfo.url != null) {
            aaVar.url = updateInfo.url;
        }
        aaVar.checkSum = updateInfo.checkSum;
        aaVar.timestamp = updateInfo.timestamp;
        aaVar.success = (byte) updateInfo.success;
        aaVar.downSize = updateInfo.downSize;
        aaVar.downType = (byte) updateInfo.downType;
        aaVar.errorCode = updateInfo.errorCode;
        aaVar.downnetType = updateInfo.downnetType;
        aaVar.downNetName = updateInfo.downNetName;
        aaVar.errorMsg = updateInfo.errorMsg;
        aaVar.rssi = updateInfo.rssi;
        aaVar.sdcardStatus = updateInfo.sdcardStatus;
        aaVar.fileSize = updateInfo.fileSize;
        this.Ka.put(Integer.valueOf(aaVar.am), aaVar);
        d.d("update_report", "configReport info: fileId=" + aaVar.am + " url=" + aaVar.url + " checkSum=" + aaVar.checkSum + " timestamp=" + aaVar.timestamp + " success=" + aaVar.success + " downSize=" + aaVar.downSize + " downType=" + aaVar.downType + " errorCode=" + aaVar.errorCode + " downnetType=" + aaVar.downnetType + " downNetName=" + aaVar.downNetName + " errorMsg=" + aaVar.errorMsg + " rssi=" + aaVar.rssi + " sdcardStatus=" + aaVar.sdcardStatus + " fileSize=" + aaVar.fileSize);
    }

    public void hO() {
        d.e("update_report", "report, size: " + this.Ka.size());
        if (this.Ka.size() != 0) {
            fs xVar = new x();
            xVar.ai = new ArrayList(this.Ka.values());
            this.Ka.clear();
            d.e("update_report", "before send shark");
            jq.cu().a(109, xVar, null, 0, new lg() {
                final /* synthetic */ b Kb;

                {
                    this.Kb = r1;
                }

                public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
                    d.d("update_report", "onFinish() seqNo: " + i + " cmdId: " + i2 + " retCode: " + i3 + " dataRetCode: " + i4);
                    if (fsVar == null) {
                        d.d("update_report", "onFinish() null");
                    }
                }
            });
        }
    }
}
