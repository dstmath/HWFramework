package tmsdk.fg.module.deepclean;

import android.content.Context;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.module.lang.MultiLangManager;
import tmsdk.common.utils.m;
import tmsdk.fg.creator.BaseManagerF;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;
import tmsdkobf.gj;
import tmsdkobf.gl;
import tmsdkobf.hb;
import tmsdkobf.jg;
import tmsdkobf.ma;

/* compiled from: Unknown */
public final class DeepcleanManager extends BaseManagerF {
    public static final int ERROR_CODE_PROCESS_ERROR = -2;
    public static final int ERROR_CODE_SCAN_LOAD_ERROR = -1;
    private a Lu;
    final int Lv;
    private RubbishEntityManager Lw;
    protected final boolean Lx;
    private final String TAG;

    public DeepcleanManager() {
        this.TAG = "DeepcleanManager";
        this.Lv = SmsCheckResult.ESCT_180;
        this.Lw = new RubbishEntityManager();
        this.Lx = false;
    }

    private boolean ja() {
        this.Lu.preLoad();
        return true;
    }

    public void appendWhitePath(String str) {
        this.Lu.appendWhitePath(str);
    }

    public void cancelClean() {
        this.Lu.cancelClean();
    }

    public void cancelScan() {
        this.Lu.cancelScan();
    }

    public void cleanSoftRubModelRubbish(SoftRubModel softRubModel) {
        if (!jg.cl()) {
            this.Lu.cleanSoftRubModelRubbish(softRubModel);
        }
    }

    public void cleanSoftRubModelRubbishInstall(SoftRubModel softRubModel) {
        if (!jg.cl()) {
            this.Lu.cleanSoftRubModelRubbishInstall(softRubModel);
        }
    }

    public void freeSoftwareRubbishManagerInstall() {
        gl.freeSoftwareRubbishManagerInstall();
    }

    public long getAllCleanRubbishSize() {
        return this.Lw.getCleanRubbishSize();
    }

    public long getAllRubbishSize() {
        return this.Lw.getAllRubbishSize();
    }

    public long getSelectedRubbishSize() {
        return this.Lw.getSelectedRubbishSize();
    }

    public RubbishEntityManager getmRubbishEntityManager() {
        return this.Lw;
    }

    protected boolean iZ() {
        return false;
    }

    public boolean init(ScanProcessListener scanProcessListener) {
        if (scanProcessListener == null) {
            return false;
        }
        m.wakeup();
        return (jg.cl() || !this.Lu.init(scanProcessListener)) ? false : ja();
    }

    public boolean insertUninstallPkg(String str) {
        if (jg.cl()) {
            return false;
        }
        this.Lu.aF(str);
        return true;
    }

    public boolean isUseCloudList() {
        return gj.aJ().isUseCloudList();
    }

    public void onCreate(Context context) {
        this.Lu = new a();
        this.Lu.a(this);
        this.Lu.onCreate(context);
        a(this.Lu);
    }

    public void onDestory() {
        this.Lu.onDestory();
    }

    public boolean quickScan() {
        hb.s(((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        if (jg.cl()) {
            return false;
        }
        ma.bx(29965);
        this.Lu.cF(15);
        return true;
    }

    public SoftRubModel scanSoftRubbish(String str) {
        if (jg.cl()) {
            return null;
        }
        ma.bx(29964);
        return this.Lu.scanSoftRubbish(str);
    }

    public SoftRubModel scanSoftRubbishInstall(String str) {
        if (jg.cl()) {
            return null;
        }
        ma.bx(29994);
        return this.Lu.scanSoftRubbishInstall(str);
    }

    public boolean startClean() {
        return !jg.cl() ? this.Lu.startClean() : false;
    }

    public boolean startScan(int i) {
        hb.s(((MultiLangManager) ManagerCreatorC.getManager(MultiLangManager.class)).isENG());
        if (jg.cl()) {
            return false;
        }
        ma.bx(29965);
        this.Lu.startScan(i);
        return true;
    }

    public int updateRubbishData(UpdateRubbishDataCallback updateRubbishDataCallback) {
        if (jg.cl()) {
            return ERROR_CODE_SCAN_LOAD_ERROR;
        }
        ma.bx(29963);
        gj.aJ().a(updateRubbishDataCallback);
        return 0;
    }
}
