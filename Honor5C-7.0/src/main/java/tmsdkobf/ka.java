package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import java.util.List;
import tmsdk.bg.module.qscanner.ICertCheckerV2;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.qscanner.QScanResultEntity;
import tmsdk.common.module.update.IUpdateObserver;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;

/* compiled from: Unknown */
public final class ka implements ICertCheckerV2 {
    private Context mContext;
    private List<ef> uY;
    private String uZ;
    private int va;
    private List<ef> vb;
    private String vc;
    private int vd;
    private final long ve;
    private final String vf;
    private IUpdateObserver vg;

    public ka(Context context) {
        this.ve = 12884901888L;
        this.vf = "action_cert_list_update";
        this.vg = new IUpdateObserver() {
            final /* synthetic */ ka vh;

            {
                this.vh = r1;
            }

            public void onChanged(UpdateInfo updateInfo) {
                if (updateInfo.flag == UpdateConfig.UPDATE_FLAG_PAY_LIST) {
                    this.vh.cQ();
                    this.vh.mContext.sendBroadcast(new Intent("action_cert_list_update"));
                } else if (updateInfo.flag == UpdateConfig.UPDATE_FLAG_STEAL_ACCOUNT_LIST) {
                    this.vh.cR();
                    this.vh.mContext.sendBroadcast(new Intent("action_cert_list_update"));
                }
            }
        };
        this.mContext = context;
        cQ();
        cR();
    }

    private void cQ() {
        ej ejVar = (ej) nj.b(this.mContext, UpdateConfig.PAY_LIST_NAME, UpdateConfig.intToString(90003), new ej());
        if (ejVar != null) {
            this.uY = ejVar.kR;
            if (ejVar.kS != null) {
                this.uZ = ejVar.kS.kV;
                this.va = ejVar.kS.kW;
            }
        }
    }

    private void cR() {
        ej ejVar = (ej) nj.b(this.mContext, UpdateConfig.STEAL_ACCOUNT_LIST_NAME, UpdateConfig.intToString(90002), new ej());
        if (ejVar != null) {
            this.vb = ejVar.kR;
            if (ejVar.kS != null) {
                this.vc = ejVar.kS.kV;
                this.vd = ejVar.kS.kW;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public QScanResultEntity a(QScanResultEntity qScanResultEntity) {
        if (this.uY != null) {
            for (ef efVar : this.uY) {
                if (efVar.cL.equals(qScanResultEntity.packageName)) {
                    qScanResultEntity.isInPayList = true;
                    if (qScanResultEntity.safeLevel != 1) {
                        if (efVar.jt != 1 || efVar.iq.contains(qScanResultEntity.certMd5)) {
                            if (efVar.jt == 0) {
                            }
                        }
                        qScanResultEntity.type = 9;
                        qScanResultEntity.discription = this.uZ;
                        qScanResultEntity.safeLevel = this.va;
                        qScanResultEntity.advice = 1;
                    }
                }
            }
        }
        if (!(qScanResultEntity.type == 9 || this.vb == null)) {
            for (ef efVar2 : this.vb) {
                if (efVar2.cL.equals(qScanResultEntity.packageName)) {
                    qScanResultEntity.isInStealAccountList = true;
                    if (qScanResultEntity.safeLevel != 1) {
                        if (efVar2.jt != 1 || efVar2.iq.contains(qScanResultEntity.certMd5)) {
                            if (efVar2.jt == 0) {
                            }
                        }
                        qScanResultEntity.type = 10;
                        qScanResultEntity.discription = this.vc;
                        qScanResultEntity.safeLevel = this.vd;
                        qScanResultEntity.advice = 1;
                    }
                }
            }
        }
        return qScanResultEntity;
    }

    public void cN() {
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).addObserver(12884901888L, this.vg);
    }

    public void cP() {
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).removeObserver(12884901888L);
    }

    public QScanResultEntity checkCert(String str) {
        QScanResultEntity qScanResultEntity = new QScanResultEntity();
        qScanResultEntity.packageName = str;
        qScanResultEntity.safeLevel = 0;
        py b = TMServiceFactory.getSystemInfoService().b(str, 16);
        if (b == null || b.hD() == null) {
            return qScanResultEntity;
        }
        qScanResultEntity.certMd5 = b.hD();
        return a(qScanResultEntity);
    }
}
