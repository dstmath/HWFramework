package tmsdk.fg.module.qscanner;

import android.content.Context;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.IUpdateObserver;
import tmsdk.common.module.update.UpdateConfig;
import tmsdk.common.module.update.UpdateInfo;
import tmsdk.common.module.update.UpdateManager;
import tmsdkobf.ea;
import tmsdkobf.eb;
import tmsdkobf.nj;

/* compiled from: Unknown */
public class SystemScanConfigManager {
    private static SystemScanConfigManager Mw;
    private List<ea> Mx;
    private List<ea> My;
    private eb Mz;
    private Context mContext;
    private IUpdateObserver vg;

    private SystemScanConfigManager(Context context) {
        this.Mx = new ArrayList();
        this.My = new ArrayList();
        this.vg = new IUpdateObserver() {
            final /* synthetic */ SystemScanConfigManager MA;

            {
                this.MA = r1;
            }

            public void onChanged(UpdateInfo updateInfo) {
                if (updateInfo.flag == UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG) {
                    this.MA.jp();
                }
            }
        };
        this.mContext = context;
        ((UpdateManager) ManagerCreatorC.getManager(UpdateManager.class)).addObserver(UpdateConfig.UPDATE_FLAG_SYSTEM_SCAN_CONFIG, this.vg);
        jp();
    }

    static SystemScanConfigManager M(Context context) {
        if (Mw == null) {
            synchronized (SystemScanConfigManager.class) {
                if (Mw == null) {
                    Mw = new SystemScanConfigManager(context);
                }
            }
        }
        return Mw;
    }

    private void jp() {
        this.Mz = (eb) nj.a(this.mContext, UpdateConfig.SYSTEM_SCAN_CONFIG_NAME, UpdateConfig.intToString(30001), new eb(), "UTF-8");
        if (this.Mz != null && this.Mz.jm != null) {
            Iterator it = this.Mz.jm.iterator();
            while (it.hasNext()) {
                ea eaVar = (ea) it.next();
                if (eaVar.jl != 0) {
                    if (eaVar.type == 12) {
                        this.Mx.add(eaVar);
                    } else if (eaVar.type == 11) {
                        this.My.add(eaVar);
                    }
                }
            }
        }
    }

    List<ea> jn() {
        return this.Mx;
    }

    List<ea> jo() {
        return this.My;
    }
}
