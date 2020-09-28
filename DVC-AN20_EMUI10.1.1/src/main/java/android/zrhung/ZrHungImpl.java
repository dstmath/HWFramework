package android.zrhung;

import android.util.Log;
import android.util.ZRHung;
import android.zrhung.appeye.AppBootFail;
import android.zrhung.appeye.AppEyeANR;
import android.zrhung.appeye.AppEyeBK;
import android.zrhung.appeye.AppEyeBinderBlock;
import android.zrhung.appeye.AppEyeCL;
import android.zrhung.appeye.AppEyeCLA;
import android.zrhung.appeye.AppEyeFocusWindow;
import android.zrhung.appeye.AppEyeFwkBlock;
import android.zrhung.appeye.AppEyeHK;
import android.zrhung.appeye.AppEyeObs;
import android.zrhung.appeye.AppEyeRcv;
import android.zrhung.appeye.AppEyeResume;
import android.zrhung.appeye.AppEyeTransparentWindow;
import android.zrhung.appeye.AppEyeUiProbe;
import android.zrhung.appeye.AppEyeXcollie;
import android.zrhung.watchpoint.SysHungScreenOn;

public class ZrHungImpl implements IZrHung {
    private static final String TAG = "ZrHungImpl";
    protected static final String ZRHUNG_PID_PARAM = "pid";
    protected static final String ZRHUNG_PKGNAME_PARAM = "packageName";
    protected static final String ZRHUNG_PROCNAME_PARAM = "processName";
    protected static final String ZRHUNG_RECOVERRESULT_PARAM = "recoverresult";
    protected static final String ZRHUNG_UID_PARAM = "uid";
    protected short mWpId;

    protected ZrHungImpl(String wpName) {
        this.mWpId = getWatchponitId(wpName);
    }

    public static IZrHung getZrHung(String wpName) {
        if (wpName == null) {
            return null;
        }
        char c = 65535;
        switch (wpName.hashCode()) {
            case -1823227498:
                if (wpName.equals("appeye_anr")) {
                    c = 7;
                    break;
                }
                break;
            case -1135438116:
                if (wpName.equals("appeye_resume")) {
                    c = 16;
                    break;
                }
                break;
            case -843673519:
                if (wpName.equals("appeye_homekey")) {
                    c = 6;
                    break;
                }
                break;
            case -707278626:
                if (wpName.equals("appeye_receiver")) {
                    c = 0;
                    break;
                }
                break;
            case -358036264:
                if (wpName.equals("appeye_nofocuswindow")) {
                    c = '\f';
                    break;
                }
                break;
            case -112346872:
                if (wpName.equals("appeye_ssbinderfull")) {
                    c = 14;
                    break;
                }
                break;
            case 129990321:
                if (wpName.equals("appeye_xcollie")) {
                    c = 15;
                    break;
                }
                break;
            case 226809278:
                if (wpName.equals("appeye_clear")) {
                    c = 3;
                    break;
                }
                break;
            case 292286899:
                if (wpName.equals("appeye_transparentwindow")) {
                    c = 11;
                    break;
                }
                break;
            case 450048453:
                if (wpName.equals("appeye_observer")) {
                    c = 1;
                    break;
                }
                break;
            case 488462959:
                if (wpName.equals("zrhung_wp_screenon_framework")) {
                    c = '\n';
                    break;
                }
                break;
            case 891740963:
                if (wpName.equals("appeye_clearall")) {
                    c = 4;
                    break;
                }
                break;
            case 1118179582:
                if (wpName.equals("appeye_frameworkblock")) {
                    c = '\t';
                    break;
                }
                break;
            case 1256170960:
                if (wpName.equals("zrhung_wp_vm_watchdog")) {
                    c = '\b';
                    break;
                }
                break;
            case 1935326413:
                if (wpName.equals("appeye_uiprobe")) {
                    c = 2;
                    break;
                }
                break;
            case 2011374409:
                if (wpName.equals("appeye_backkey")) {
                    c = 5;
                    break;
                }
                break;
            case 2114922495:
                if (wpName.equals("appeye_bootfail")) {
                    c = '\r';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return AppEyeRcv.getInstance(wpName);
            case 1:
                return AppEyeObs.getInstance(wpName);
            case 2:
                return AppEyeUiProbe.get();
            case 3:
                return AppEyeCL.getInstance(wpName);
            case 4:
                return AppEyeCLA.getInstance(wpName);
            case 5:
                return new AppEyeBK(wpName);
            case 6:
                return new AppEyeHK(wpName);
            case 7:
                return AppEyeANR.getInstance();
            case '\b':
                return new SysHungVmWTG(wpName);
            case '\t':
                return AppEyeFwkBlock.getInstance();
            case '\n':
                return SysHungScreenOn.getInstance(wpName);
            case 11:
                return AppEyeTransparentWindow.getInstance(wpName);
            case '\f':
                return AppEyeFocusWindow.getInstance(wpName);
            case '\r':
                return AppBootFail.getInstance(wpName);
            case 14:
                return AppEyeBinderBlock.getInstance(wpName);
            case 15:
                return AppEyeXcollie.getInstance(wpName);
            case 16:
                return AppEyeResume.getInstance(wpName);
            default:
                return null;
        }
    }

    public int init(ZrHungData zrHungData) {
        return 0;
    }

    public boolean start(ZrHungData zrHungData) {
        return false;
    }

    public boolean check(ZrHungData zrHungData) {
        return false;
    }

    public boolean cancelCheck(ZrHungData zrHungData) {
        return false;
    }

    public boolean stop(ZrHungData zrHungData) {
        return false;
    }

    public boolean sendEvent(ZrHungData zrHungData) {
        return false;
    }

    public ZrHungData query() {
        return new ZrHungData();
    }

    public boolean addInfo(ZrHungData zrHungData) {
        return false;
    }

    /* access modifiers changed from: protected */
    public ZRHung.HungConfig getConfig() {
        ZRHung.HungConfig cfg = ZRHung.getHungConfig(this.mWpId);
        if (cfg == null || cfg.status != 0) {
            Log.e(TAG, "ZRHung.getConfig failed!");
            return null;
        }
        Log.d(TAG, "ZRHung.getConfig success!");
        return cfg;
    }

    private short getWatchponitId(String name) {
        if (name == null) {
            return 0;
        }
        char c = 65535;
        switch (name.hashCode()) {
            case -1823227498:
                if (name.equals("appeye_anr")) {
                    c = 7;
                    break;
                }
                break;
            case -1135438116:
                if (name.equals("appeye_resume")) {
                    c = 16;
                    break;
                }
                break;
            case -843673519:
                if (name.equals("appeye_homekey")) {
                    c = 6;
                    break;
                }
                break;
            case -707278626:
                if (name.equals("appeye_receiver")) {
                    c = 0;
                    break;
                }
                break;
            case -358036264:
                if (name.equals("appeye_nofocuswindow")) {
                    c = '\f';
                    break;
                }
                break;
            case -112346872:
                if (name.equals("appeye_ssbinderfull")) {
                    c = 14;
                    break;
                }
                break;
            case 129990321:
                if (name.equals("appeye_xcollie")) {
                    c = 15;
                    break;
                }
                break;
            case 226809278:
                if (name.equals("appeye_clear")) {
                    c = 3;
                    break;
                }
                break;
            case 292286899:
                if (name.equals("appeye_transparentwindow")) {
                    c = 11;
                    break;
                }
                break;
            case 450048453:
                if (name.equals("appeye_observer")) {
                    c = 1;
                    break;
                }
                break;
            case 488462959:
                if (name.equals("zrhung_wp_screenon_framework")) {
                    c = '\n';
                    break;
                }
                break;
            case 891740963:
                if (name.equals("appeye_clearall")) {
                    c = 4;
                    break;
                }
                break;
            case 1118179582:
                if (name.equals("appeye_frameworkblock")) {
                    c = '\t';
                    break;
                }
                break;
            case 1256170960:
                if (name.equals("zrhung_wp_vm_watchdog")) {
                    c = '\b';
                    break;
                }
                break;
            case 1935326413:
                if (name.equals("appeye_uiprobe")) {
                    c = 2;
                    break;
                }
                break;
            case 2011374409:
                if (name.equals("appeye_backkey")) {
                    c = 5;
                    break;
                }
                break;
            case 2114922495:
                if (name.equals("appeye_bootfail")) {
                    c = '\r';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return ZRHung.APPEYE_RCV;
            case 1:
                return ZRHung.APPEYE_OBS;
            case 2:
                return ZRHung.APPEYE_UIP_FREEZE;
            case 3:
                return ZRHung.APPEYE_CL;
            case 4:
                return ZRHung.APPEYE_CLA;
            case 5:
                return ZRHung.ZRHUNG_EVENT_BACKKEY;
            case 6:
                return ZRHung.ZRHUNG_EVENT_HOMEKEY;
            case 7:
                return ZRHung.APPEYE_CANR;
            case '\b':
                return 22;
            case '\t':
                return ZRHung.APPEYE_FWB_FREEZE;
            case '\n':
                return 11;
            case 11:
                return ZRHung.APPEYE_TWIN;
            case '\f':
                return ZRHung.APPEYE_NFW;
            case '\r':
                return ZRHung.APPEYE_BF;
            case 14:
                return ZRHung.APPEYE_TEMP1;
            case 15:
                return ZRHung.XCOLLIE_FWK_SERVICE;
            case 16:
                return ZRHung.APPEYE_RESUME;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendAppEyeEvent(short wpId, ZrHungData zrHungData, String cmdBuf, String buffer) {
        try {
            StringBuilder hungEventInfo = new StringBuilder();
            if (zrHungData != null) {
                int uid = zrHungData.getInt("uid");
                if (uid > 0) {
                    hungEventInfo.append("uid = ");
                    hungEventInfo.append(uid);
                    hungEventInfo.append(System.lineSeparator());
                }
                int pid = zrHungData.getInt("pid");
                if (pid > 0) {
                    hungEventInfo.append("pid = ");
                    hungEventInfo.append(pid);
                    hungEventInfo.append(System.lineSeparator());
                }
                String pkgName = zrHungData.getString("packageName");
                if (pkgName != null) {
                    hungEventInfo.append("packageName = ");
                    hungEventInfo.append(pkgName);
                    hungEventInfo.append(System.lineSeparator());
                }
                String procName = zrHungData.getString("processName");
                if (procName != null) {
                    hungEventInfo.append("processName = ");
                    hungEventInfo.append(procName);
                    hungEventInfo.append(System.lineSeparator());
                }
                String recoverresult = zrHungData.getString(ZRHUNG_RECOVERRESULT_PARAM);
                if (recoverresult != null) {
                    hungEventInfo.append("result = ");
                    hungEventInfo.append(recoverresult);
                    hungEventInfo.append(System.lineSeparator());
                }
            }
            if (buffer != null) {
                hungEventInfo.append(buffer);
            }
            if (ZRHung.sendHungEvent(wpId, cmdBuf, hungEventInfo.toString())) {
                return true;
            }
            Log.e(TAG, " sendAppFreezeEvent failed!");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "send appeye event exception");
            return false;
        }
    }
}
