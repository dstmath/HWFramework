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
    /* access modifiers changed from: protected */
    public short mWpId;

    protected ZrHungImpl(String wpName) {
        this.mWpId = getWatchponitId(wpName);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    public static IZrHung getZrHung(String wpName) {
        char c;
        switch (wpName.hashCode()) {
            case -1823227498:
                if (wpName.equals("appeye_anr")) {
                    c = 7;
                    break;
                }
            case -843673519:
                if (wpName.equals("appeye_homekey")) {
                    c = 6;
                    break;
                }
            case -707278626:
                if (wpName.equals("appeye_receiver")) {
                    c = 0;
                    break;
                }
            case -358036264:
                if (wpName.equals("appeye_nofocuswindow")) {
                    c = 12;
                    break;
                }
            case -112346872:
                if (wpName.equals("appeye_ssbinderfull")) {
                    c = 14;
                    break;
                }
            case 129990321:
                if (wpName.equals("appeye_xcollie")) {
                    c = 15;
                    break;
                }
            case 226809278:
                if (wpName.equals("appeye_clear")) {
                    c = 3;
                    break;
                }
            case 292286899:
                if (wpName.equals("appeye_transparentwindow")) {
                    c = 11;
                    break;
                }
            case 450048453:
                if (wpName.equals("appeye_observer")) {
                    c = 1;
                    break;
                }
            case 488462959:
                if (wpName.equals("zrhung_wp_screenon_framework")) {
                    c = 10;
                    break;
                }
            case 891740963:
                if (wpName.equals("appeye_clearall")) {
                    c = 4;
                    break;
                }
            case 1118179582:
                if (wpName.equals("appeye_frameworkblock")) {
                    c = 9;
                    break;
                }
            case 1256170960:
                if (wpName.equals("zrhung_wp_vm_watchdog")) {
                    c = 8;
                    break;
                }
            case 1935326413:
                if (wpName.equals("appeye_uiprobe")) {
                    c = 2;
                    break;
                }
            case 2011374409:
                if (wpName.equals("appeye_backkey")) {
                    c = 5;
                    break;
                }
            case 2114922495:
                if (wpName.equals("appeye_bootfail")) {
                    c = 13;
                    break;
                }
            default:
                c = 65535;
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
                return new AppEyeANR(wpName);
            case 8:
                return new SysHungVmWTG(wpName);
            case 9:
                return AppEyeFwkBlock.getInstance();
            case 10:
                return SysHungScreenOn.getInstance(wpName);
            case 11:
                return AppEyeTransparentWindow.getInstance(wpName);
            case 12:
                return AppEyeFocusWindow.getInstance(wpName);
            case 13:
                return AppBootFail.getInstance(wpName);
            case 14:
                return AppEyeBinderBlock.getInstance(wpName);
            case 15:
                return AppEyeXcollie.getInstance(wpName);
            default:
                return null;
        }
    }

    public int init(ZrHungData args) {
        return 0;
    }

    public boolean start(ZrHungData args) {
        return false;
    }

    public boolean check(ZrHungData args) {
        return false;
    }

    public boolean cancelCheck(ZrHungData args) {
        return false;
    }

    public boolean stop(ZrHungData args) {
        return false;
    }

    public boolean sendEvent(ZrHungData args) {
        return false;
    }

    public ZrHungData query() {
        return null;
    }

    public boolean addInfo(ZrHungData args) {
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

    /* JADX WARNING: Can't fix incorrect switch cases order */
    private short getWatchponitId(String name) {
        char c;
        switch (name.hashCode()) {
            case -1823227498:
                if (name.equals("appeye_anr")) {
                    c = 7;
                    break;
                }
            case -843673519:
                if (name.equals("appeye_homekey")) {
                    c = 6;
                    break;
                }
            case -707278626:
                if (name.equals("appeye_receiver")) {
                    c = 0;
                    break;
                }
            case -358036264:
                if (name.equals("appeye_nofocuswindow")) {
                    c = 12;
                    break;
                }
            case -112346872:
                if (name.equals("appeye_ssbinderfull")) {
                    c = 14;
                    break;
                }
            case 129990321:
                if (name.equals("appeye_xcollie")) {
                    c = 15;
                    break;
                }
            case 226809278:
                if (name.equals("appeye_clear")) {
                    c = 3;
                    break;
                }
            case 292286899:
                if (name.equals("appeye_transparentwindow")) {
                    c = 11;
                    break;
                }
            case 450048453:
                if (name.equals("appeye_observer")) {
                    c = 1;
                    break;
                }
            case 488462959:
                if (name.equals("zrhung_wp_screenon_framework")) {
                    c = 10;
                    break;
                }
            case 891740963:
                if (name.equals("appeye_clearall")) {
                    c = 4;
                    break;
                }
            case 1118179582:
                if (name.equals("appeye_frameworkblock")) {
                    c = 9;
                    break;
                }
            case 1256170960:
                if (name.equals("zrhung_wp_vm_watchdog")) {
                    c = 8;
                    break;
                }
            case 1935326413:
                if (name.equals("appeye_uiprobe")) {
                    c = 2;
                    break;
                }
            case 2011374409:
                if (name.equals("appeye_backkey")) {
                    c = 5;
                    break;
                }
            case 2114922495:
                if (name.equals("appeye_bootfail")) {
                    c = 13;
                    break;
                }
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                return 276;
            case 1:
                return 277;
            case 2:
                return 258;
            case 3:
                return 265;
            case 4:
                return 266;
            case 5:
                return 516;
            case 6:
                return 515;
            case 7:
                return 269;
            case 8:
                return 22;
            case 9:
                return 271;
            case 10:
                return 11;
            case 11:
                return 273;
            case 12:
                return 272;
            case 13:
                return 264;
            case 14:
                return 289;
            case 15:
                return ZRHung.XCOLLIE_FWK_SERVICE;
            default:
                return 0;
        }
    }

    /* access modifiers changed from: protected */
    public boolean sendAppEyeEvent(short wpId, ZrHungData args, String cmdBuf, String buffer) {
        StringBuilder sb = new StringBuilder();
        if (args != null) {
            try {
                int uid = args.getInt("uid");
                if (uid > 0) {
                    sb.append("uid = ");
                    sb.append(Integer.toString(uid));
                    sb.append(10);
                }
                int pid = args.getInt("pid");
                if (pid > 0) {
                    sb.append("pid = ");
                    sb.append(Integer.toString(pid));
                    sb.append(10);
                }
                String pkgName = args.getString("packageName");
                if (pkgName != null) {
                    sb.append("packageName = ");
                    sb.append(pkgName);
                    sb.append(10);
                }
                String procName = args.getString("processName");
                if (procName != null) {
                    sb.append("processName = ");
                    sb.append(procName);
                    sb.append(10);
                }
                String recoverresult = args.getString(ZRHUNG_RECOVERRESULT_PARAM);
                if (recoverresult != null) {
                    sb.append("result = ");
                    sb.append(recoverresult);
                    sb.append(10);
                }
            } catch (Exception ex) {
                Log.e(TAG, "exception info ex:" + ex);
                return false;
            }
        }
        if (buffer != null) {
            sb.append(buffer);
        }
        if (!ZRHung.sendHungEvent(wpId, cmdBuf, sb.toString())) {
            Log.e(TAG, " sendAppFreezeEvent failed!");
        }
        return true;
    }
}
