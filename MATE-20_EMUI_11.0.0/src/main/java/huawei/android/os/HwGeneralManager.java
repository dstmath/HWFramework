package huawei.android.os;

import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Flog;
import com.huawei.android.os.RemoteExceptionEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.util.SlogEx;
import com.huawei.immersion.Vibetonz;
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
        this.mService = IHwGeneralManager.Stub.asInterface(ServiceManagerEx.getService(HwContextEx.HW_GENERAL_SERVICE));
        initEffectNames();
    }

    public boolean isSupportTrikey() {
        if (!this.mInitTrikey) {
            File file = new File(TRIKEY_PATH);
            this.mIsSupportTrikey = file.exists() && file.isDirectory();
            this.mInitTrikey = true;
            SlogEx.i(TAG, "isSupportTrikey is:" + String.valueOf(this.mIsSupportTrikey));
        }
        return this.mIsSupportTrikey;
    }

    public int setSDLockPassword(String pw) {
        try {
            if (this.mService != null) {
                return this.mService.setSDLockPassword(pw);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public int clearSDLockPassword() {
        try {
            if (this.mService != null) {
                return this.mService.clearSDLockPassword();
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public void resetTouchWeight() {
        try {
            if (this.mService != null) {
                SlogEx.i("touch weight", "HwGeneralManager resetTouchWeight");
                this.mService.resetTouchWeight();
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int unlockSDCard(String pw) {
        try {
            if (this.mService != null) {
                return this.mService.unlockSDCard(pw);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public void eraseSDLock() {
        try {
            if (this.mService != null) {
                this.mService.eraseSDLock();
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int getSDLockState() {
        try {
            if (this.mService != null) {
                return this.mService.getSDLockState();
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public String getSDCardId() {
        try {
            if (this.mService != null) {
                return this.mService.getSDCardId();
            }
            return null;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return null;
        }
    }

    public String getTouchWeightValue() {
        try {
            if (this.mService == null) {
                return null;
            }
            String val = this.mService.getTouchWeightValue();
            SlogEx.i("touch weight", "HwGeneralManager getTouchWeightValue " + val);
            return val;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return null;
        }
    }

    public void startFileBackup() {
        try {
            if (this.mService != null) {
                this.mService.startFileBackup();
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int forceIdle() {
        try {
            if (this.mService != null) {
                return this.mService.forceIdle();
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public boolean isSupportForce() {
        if (!this.mInit) {
            try {
                if (this.mService != null) {
                    this.mIsSupportPressure = this.mService.isSupportForce();
                    this.mInit = true;
                }
            } catch (RemoteException e) {
                Flog.e(1504, "HwGeneralManager binder error!");
            }
        }
        return this.mIsSupportPressure;
    }

    public boolean isCurveScreen() {
        IHwGeneralManager iHwGeneralManager;
        if (this.mIsCurveScreen == -1 && (iHwGeneralManager = this.mService) != null) {
            try {
                this.mIsCurveScreen = iHwGeneralManager.isCurveScreen() ? 1 : 0;
            } catch (RemoteException e) {
                Flog.e(1504, "HwGeneralManager binder error!");
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
            Flog.e(1504, "HwGeneralManager RemoteException error!");
        }
        return 0.0f;
    }

    private void initEffectNames() {
        effectNames.put(100, "COUNTDOWN_SWING");
        effectNames.put(200, "TIMING_ROTATE");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_NUMBERPICKER_ITEMSCROLL), "NUMBERPICKER_ITEMSCROLL");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_NUMBERPICKER_TUNER), "NUMBERPICKER_TUNER");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_FM_ADJUST), "FM_ADJUST");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_FM_SPIN), "FM_SPIN");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_FM_ADJUST_DONE), "FM_ADJUST_DONE");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_HOMESCREEN_ICON_FLY_WORKSPACE), "HOMESCREEN_ICON_FLY_WORKSPACE");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_BULK_MOVE_ICONDROP), "BULK_MOVE_ICONDROP");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_BULK_MOVE_ICON_GATHER), "BULK_MOVE_ICON_GATHER");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_BULK_MOVE_ICONPICKUP), "BULK_MOVE_ICONPICKUP");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_HOMESCREEN_SHAKE_ALIGN), "HOMESCREEN_SHAKE_ALIGN");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_LONG_PRESS), "LONG_PRESS");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_LONG_PRESS_WORKSPACE), "LONG_PRESS_WORKSPACE");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_SCROLL_INDICATOR_POP), "SCROLL_INDICATOR_POP");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_CONTACT_ALPHA_SWITCH), "CONTACT_ALPHA_SWITCH");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_TEXTVIEW_SELECTCHAR), "TEXTVIEW_SELECTCHAR");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_TEXTVIEW_DOUBLE_TAP_SELECTWORD), "TEXTVIEW_DOUBLE_TAP_SELECTWORD");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_TEXTVIEW_TAPWORD), "TEXTVIEW_TAPWORD");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_TEXTVIEW_SETCURSOR), "TEXTVIEW_SETCURSOR");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_WEATHER_RAIN), "WEATHER_RAIN");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_WEATHER_SAND), "WEATHER_SAND");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_WEATHER_THUNDERSTORM), "WEATHER_THUNDERSTORM");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_WEATHER_WINDY), "WEATHER_WINDY");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_LOCKSCREEN_UNLOCK), "LOCKSCREEN_UNLOCK");
        effectNames.put(Integer.valueOf((int) Vibetonz.HAPTIC_EVENT_VIRTUAL_KEY), "VIRTUAL_KEY");
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
            SlogEx.e(TAG, "HwGeneralManager playIvtEffect binder error!");
        }
    }

    public void stopPlayEffect() {
        try {
            if (this.mService != null) {
                this.mService.stopPlayEffect();
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager stopPlayEffect binder error!");
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
            SlogEx.e(TAG, "HwGeneralManager pausePlayEffect binder error!");
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
            SlogEx.e(TAG, "HwGeneralManager resumePausedEffect binder error!");
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
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager isPlaying binder error!");
            return false;
        }
    }

    public boolean startHaptic(Context mContext, int callerID, int ringtoneType, Uri uri) {
        try {
            if (this.mService != null) {
                return this.mService.startHaptic(callerID, ringtoneType, uri);
            }
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager startHaptic binder error!");
            return false;
        }
    }

    public boolean hasHaptic(Context mContext, Uri uri) {
        try {
            if (this.mService != null) {
                return this.mService.hasHaptic(uri);
            }
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager hasHaptic binder error!");
            return false;
        }
    }

    public void stopHaptic() {
        try {
            if (this.mService != null) {
                this.mService.stopHaptic();
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager stopHaptic binder error!");
        }
    }

    public IHwGeneralManager getService() {
        return this.mService;
    }

    public boolean mkDataDir(String path) {
        try {
            if (this.mService == null) {
                return false;
            }
            this.mService.mkDataDir(path);
            return true;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager mkDataDir binder error!");
            return false;
        }
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        try {
            if (this.mService != null) {
                return this.mService.readProtectArea(optItem, readBufLen, readBuf, errorNum);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        try {
            if (this.mService != null) {
                return this.mService.writeProtectArea(optItem, writeLen, writeBuf, errorNum);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        try {
            if (this.mService != null) {
                return this.mService.setSdCardCryptdEnable(enable, volId);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            if (this.mService != null) {
                return this.mService.unlockSdCardKey(userId, serialNumber, token, secret);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            if (this.mService != null) {
                return this.mService.addSdCardUserKeyAuth(userId, serialNumber, token, secret);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public int backupSecretkey() {
        try {
            if (this.mService != null) {
                return this.mService.backupSecretkey();
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public boolean supportHwPush() {
        try {
            if (this.mService != null) {
                return this.mService.supportHwPush();
            }
            return false;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager supportHwPush binder error!");
            return false;
        }
    }

    public long getPartitionInfo(String partitionName, int infoType) {
        try {
            if (this.mService != null) {
                return this.mService.getPartitionInfo(partitionName, infoType);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager getPartitionInfo binder error!");
            return -1;
        }
    }

    public String mountCifs(String source, String option, IBinder binder) {
        try {
            if (this.mService != null) {
                return this.mService.mountCifs(source, option, binder);
            }
            return null;
        } catch (RemoteException ex) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            throw RemoteExceptionEx.rethrowFromSystemServer(ex);
        }
    }

    public void unmountCifs(String mountPoint) {
        try {
            if (this.mService != null) {
                this.mService.unmountCifs(mountPoint);
            }
        } catch (RemoteException ex) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            throw RemoteExceptionEx.rethrowFromSystemServer(ex);
        }
    }

    public int isSupportedCifs() {
        try {
            if (this.mService != null) {
                return this.mService.isSupportedCifs();
            }
            return -1;
        } catch (RemoteException ex) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            throw RemoteExceptionEx.rethrowFromSystemServer(ex);
        }
    }

    public int getLocalDevStat(int dev) {
        try {
            if (this.mService != null) {
                return this.mService.getLocalDevStat(dev);
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }

    public String getDeviceId(int dev) {
        try {
            if (this.mService != null) {
                return this.mService.getDeviceId(dev);
            }
            return null;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return null;
        }
    }

    public int doSdcardCheckRW() {
        try {
            if (this.mService != null) {
                return this.mService.doSdcardCheckRW();
            }
            return -1;
        } catch (RemoteException e) {
            SlogEx.e(TAG, "HwGeneralManager binder error!");
            return -1;
        }
    }
}
