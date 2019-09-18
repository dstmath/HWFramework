package huawei.android.os;

import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.rms.iaware.AppTypeInfo;
import android.util.Flog;
import android.util.Slog;
import huawei.android.app.admin.ConstantValue;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwGeneralManager;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class HwGeneralManager {
    public static final int STATE_FALSE = 0;
    public static final int STATE_INVALID = -1;
    public static final int STATE_TRUE = 1;
    private static final String TAG = "HwGeneralManager";
    private static String TRIKEY_PATH = "/proc/device-tree/huawei_touch_key";
    private static Map<Integer, String> effectNames = new HashMap();
    private static volatile HwGeneralManager mInstance = null;
    private boolean mInit;
    private boolean mInitTrikey;
    private int mIsCurveScreen;
    private boolean mIsSupportPressure;
    private boolean mIsSupportTrikey;
    IHwGeneralManager mService;

    public static synchronized HwGeneralManager getInstance() {
        HwGeneralManager hwGeneralManager;
        synchronized (HwGeneralManager.class) {
            if (mInstance == null || mInstance.getService() == null) {
                mInstance = new HwGeneralManager();
            }
            hwGeneralManager = mInstance;
        }
        return hwGeneralManager;
    }

    private HwGeneralManager() {
        this.mService = null;
        this.mIsSupportPressure = false;
        this.mIsCurveScreen = -1;
        this.mInit = false;
        this.mInitTrikey = false;
        this.mIsSupportTrikey = false;
        this.mService = IHwGeneralManager.Stub.asInterface(ServiceManager.getService(HwContextEx.HW_GENERAL_SERVICE));
        initEffectNames();
    }

    public boolean isSupportTrikey() {
        if (!this.mInitTrikey) {
            File file = new File(TRIKEY_PATH);
            this.mIsSupportTrikey = file.exists() && file.isDirectory();
            this.mInitTrikey = true;
            Slog.i(TAG, "isSupportTrikey is:" + String.valueOf(this.mIsSupportTrikey));
        }
        return this.mIsSupportTrikey;
    }

    public int setSDLockPassword(String pw) {
        try {
            if (this.mService != null) {
                return this.mService.setSDLockPassword(pw);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public int clearSDLockPassword() {
        try {
            if (this.mService != null) {
                return this.mService.clearSDLockPassword();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public void resetTouchWeight() {
        try {
            if (this.mService != null) {
                Slog.i("touch weight", "HwGeneralManager resetTouchWeight");
                this.mService.resetTouchWeight();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int unlockSDCard(String pw) {
        try {
            if (this.mService != null) {
                return this.mService.unlockSDCard(pw);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public void eraseSDLock() {
        try {
            if (this.mService != null) {
                this.mService.eraseSDLock();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int getSDLockState() {
        try {
            if (this.mService != null) {
                return this.mService.getSDLockState();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public String getSDCardId() {
        try {
            if (this.mService != null) {
                return this.mService.getSDCardId();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return null;
    }

    public String getTouchWeightValue() {
        try {
            if (this.mService != null) {
                String val = this.mService.getTouchWeightValue();
                Slog.i("touch weight", "HwGeneralManager getTouchWeightValue " + val);
                return val;
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return null;
    }

    public void startFileBackup() {
        try {
            if (this.mService != null) {
                this.mService.startFileBackup();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int forceIdle() {
        try {
            if (this.mService != null) {
                return this.mService.forceIdle();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public boolean isSupportForce() {
        if (!this.mInit) {
            try {
                if (this.mService != null) {
                    this.mIsSupportPressure = this.mService.isSupportForce();
                    this.mInit = true;
                }
            } catch (RemoteException e) {
                Flog.e(ConstantValue.transaction_turnOnGPS, "HwGeneralManager binder error!");
            }
        }
        return this.mIsSupportPressure;
    }

    public boolean isCurveScreen() {
        if (this.mIsCurveScreen == -1) {
            try {
                this.mIsCurveScreen = this.mService.isCurveScreen() ? 1 : 0;
            } catch (RemoteException e) {
                Flog.e(ConstantValue.transaction_turnOnGPS, "HwGeneralManager binder error!");
            }
        }
        return this.mIsCurveScreen == 1;
    }

    public float getPressureLimit() {
        try {
            if (this.mService != null) {
                return this.mService.getPressureLimit();
            }
        } catch (RemoteException e) {
            Flog.e(ConstantValue.transaction_turnOnGPS, "HwGeneralManager RemoteException error!");
        }
        return 0.0f;
    }

    private void initEffectNames() {
        effectNames.put(100, "COUNTDOWN_SWING");
        effectNames.put(200, "TIMING_ROTATE");
        effectNames.put(Integer.valueOf(AppTypeInfo.PG_TYPE_BASE), "NUMBERPICKER_ITEMSCROLL");
        effectNames.put(400, "NUMBERPICKER_TUNER");
        effectNames.put(500, "FM_ADJUST");
        effectNames.put(600, "FM_SPIN");
        effectNames.put(700, "FM_ADJUST_DONE");
        effectNames.put(800, "HOMESCREEN_ICON_FLY_WORKSPACE");
        effectNames.put(900, "BULK_MOVE_ICONDROP");
        effectNames.put(1000, "BULK_MOVE_ICON_GATHER");
        effectNames.put(1100, "BULK_MOVE_ICONPICKUP");
        effectNames.put(1200, "HOMESCREEN_SHAKE_ALIGN");
        effectNames.put(1300, "LONG_PRESS");
        effectNames.put(1400, "LONG_PRESS_WORKSPACE");
        effectNames.put(1500, "SCROLL_INDICATOR_POP");
        effectNames.put(1600, "CONTACT_ALPHA_SWITCH");
        effectNames.put(1700, "TEXTVIEW_SELECTCHAR");
        effectNames.put(1800, "TEXTVIEW_DOUBLE_TAP_SELECTWORD");
        effectNames.put(1900, "TEXTVIEW_TAPWORD");
        effectNames.put(2000, "TEXTVIEW_SETCURSOR");
        effectNames.put(2100, "WEATHER_RAIN");
        effectNames.put(2200, "WEATHER_SAND");
        effectNames.put(2300, "WEATHER_THUNDERSTORM");
        effectNames.put(2400, "WEATHER_WINDY");
        effectNames.put(2500, "LOCKSCREEN_UNLOCK");
        effectNames.put(2600, "VIRTUAL_KEY");
    }

    public void playIvtEffect(int effectNo) {
        playIvtEffect(effectNames.get(Integer.valueOf(effectNo)));
    }

    public void playIvtEffect(String effectName) {
        try {
            if (this.mService != null) {
                this.mService.playIvtEffect(effectName);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager playIvtEffect binder error!");
        }
    }

    public void stopPlayEffect() {
        try {
            if (this.mService != null) {
                this.mService.stopPlayEffect();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager stopPlayEffect binder error!");
        }
    }

    public void pausePlayEffect(int effectNo) {
        pausePlayEffect(effectNames.get(Integer.valueOf(effectNo)));
    }

    public void pausePlayEffect(String effectName) {
        try {
            if (this.mService != null) {
                this.mService.pausePlayEffect(effectName);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager pausePlayEffect binder error!");
        }
    }

    public void resumePausedEffect(int effectNo) {
        resumePausedEffect(effectNames.get(Integer.valueOf(effectNo)));
    }

    public void resumePausedEffect(String effectName) {
        try {
            if (this.mService != null) {
                this.mService.resumePausedEffect(effectName);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager resumePausedEffect binder error!");
        }
    }

    public boolean isPlaying(int effectNo) {
        return isPlaying(effectNames.get(Integer.valueOf(effectNo)));
    }

    public boolean isPlaying(String effectName) {
        try {
            if (this.mService != null) {
                return this.mService.isPlaying(effectName);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager isPlaying binder error!");
        }
        return false;
    }

    public boolean startHaptic(Context mContext, int callerID, int ringtoneType, Uri uri) {
        try {
            if (this.mService != null) {
                return this.mService.startHaptic(callerID, ringtoneType, uri);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager startHaptic binder error!");
        }
        return false;
    }

    public boolean hasHaptic(Context mContext, Uri uri) {
        try {
            if (this.mService != null) {
                return this.mService.hasHaptic(uri);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager hasHaptic binder error!");
        }
        return false;
    }

    public void stopHaptic() {
        try {
            if (this.mService != null) {
                this.mService.stopHaptic();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager stopHaptic binder error!");
        }
    }

    public IHwGeneralManager getService() {
        return this.mService;
    }

    public boolean mkDataDir(String path) {
        try {
            if (this.mService != null) {
                this.mService.mkDataDir(path);
                return true;
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager mkDataDir binder error!");
        }
        return false;
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        try {
            if (this.mService != null) {
                return this.mService.readProtectArea(optItem, readBufLen, readBuf, errorNum);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        try {
            if (this.mService != null) {
                return this.mService.writeProtectArea(optItem, writeLen, writeBuf, errorNum);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        try {
            if (this.mService != null) {
                return this.mService.setSdCardCryptdEnable(enable, volId);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            if (this.mService != null) {
                return this.mService.unlockSdCardKey(userId, serialNumber, token, secret);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            if (this.mService != null) {
                return this.mService.addSdCardUserKeyAuth(userId, serialNumber, token, secret);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public int backupSecretkey() {
        try {
            if (this.mService != null) {
                return this.mService.backupSecretkey();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public boolean supportHwPush() {
        try {
            if (this.mService != null) {
                return this.mService.supportHwPush();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager supportHwPush binder error!");
        }
        return false;
    }

    public long getPartitionInfo(String partitionName, int infoType) {
        try {
            if (this.mService != null) {
                return this.mService.getPartitionInfo(partitionName, infoType);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager getPartitionInfo binder error!");
        }
        return -1;
    }

    public String mountCifs(String source, String option, IBinder binder) {
        try {
            if (this.mService != null) {
                return this.mService.mountCifs(source, option, binder);
            }
            return null;
        } catch (RemoteException ex) {
            Slog.e(TAG, "HwGeneralManager binder error!");
            throw ex.rethrowFromSystemServer();
        }
    }

    public void unmountCifs(String mountPoint) {
        try {
            if (this.mService != null) {
                this.mService.unmountCifs(mountPoint);
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "HwGeneralManager binder error!");
            throw ex.rethrowFromSystemServer();
        }
    }

    public int isSupportedCifs() {
        try {
            if (this.mService != null) {
                return this.mService.isSupportedCifs();
            }
            return -1;
        } catch (RemoteException ex) {
            Slog.e(TAG, "HwGeneralManager binder error!");
            throw ex.rethrowFromSystemServer();
        }
    }

    public int getLocalDevStat(int dev) {
        try {
            if (this.mService != null) {
                return this.mService.getLocalDevStat(dev);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }

    public String getDeviceId(int dev) {
        try {
            if (this.mService != null) {
                return this.mService.getDeviceId(dev);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return null;
    }

    public int doSdcardCheckRW() {
        try {
            if (this.mService != null) {
                return this.mService.doSdcardCheckRW();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return -1;
    }
}
