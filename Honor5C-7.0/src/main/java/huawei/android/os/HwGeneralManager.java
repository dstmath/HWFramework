package huawei.android.os;

import android.content.Context;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Flog;
import android.util.Slog;
import com.huawei.connectivitylog.ConnectivityLogManager;
import huawei.android.app.admin.ConstantValue;
import huawei.android.content.HwContextEx;
import huawei.android.os.IHwGeneralManager.Stub;
import huawei.android.provider.HwSettings.System;
import java.io.File;
import java.util.Map;

public class HwGeneralManager {
    public static final int STATE_FALSE = 0;
    public static final int STATE_INVALID = -1;
    public static final int STATE_TRUE = 1;
    private static final String TAG = "HwGeneralManager";
    private static String TRIKEY_PATH;
    private static Map<Integer, String> effectNames;
    private static volatile HwGeneralManager mInstance;
    private boolean mInit;
    private boolean mInitTrikey;
    private int mIsCurveScreen;
    private boolean mIsSupportPressure;
    private boolean mIsSupportTrikey;
    IHwGeneralManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.os.HwGeneralManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.os.HwGeneralManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.os.HwGeneralManager.<clinit>():void");
    }

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
        this.mIsCurveScreen = STATE_INVALID;
        this.mInit = false;
        this.mInitTrikey = false;
        this.mIsSupportTrikey = false;
        this.mService = Stub.asInterface(ServiceManager.getService(HwContextEx.HW_GENERAL_SERVICE));
        initEffectNames();
    }

    public boolean isSupportTrikey() {
        if (!this.mInitTrikey) {
            File file = new File(TRIKEY_PATH);
            this.mIsSupportTrikey = file.exists() ? file.isDirectory() : false;
            this.mInitTrikey = true;
            Slog.i(TAG, "isSupportTrikey is:" + String.valueOf(this.mIsSupportTrikey));
        }
        return this.mIsSupportTrikey;
    }

    public void switchBootOrShutSound(String openOrClose) {
        try {
            if (this.mService != null) {
                this.mService.switchBootOrShutSound(openOrClose);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
    }

    public int getBootAnimSoundSwitch() {
        try {
            if (this.mService != null) {
                return this.mService.getBootAnimSoundSwitch();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
    }

    public boolean isBootOrShutdownSoundCapable() {
        try {
            if (this.mService != null) {
                return this.mService.isBootOrShutdownSoundCapable();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return false;
    }

    public int setSDLockPassword(String pw) {
        try {
            if (this.mService != null) {
                return this.mService.setSDLockPassword(pw);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
    }

    public int clearSDLockPassword() {
        try {
            if (this.mService != null) {
                return this.mService.clearSDLockPassword();
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
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
        return STATE_INVALID;
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
        return STATE_INVALID;
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
        return STATE_INVALID;
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
        if (this.mIsCurveScreen == STATE_INVALID) {
            try {
                this.mIsCurveScreen = this.mService.isCurveScreen() ? STATE_TRUE : STATE_FALSE;
            } catch (RemoteException e) {
                Flog.e(ConstantValue.transaction_turnOnGPS, "HwGeneralManager binder error!");
            }
        }
        if (this.mIsCurveScreen == STATE_TRUE) {
            return true;
        }
        return false;
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
        effectNames.put(Integer.valueOf(100), "COUNTDOWN_SWING");
        effectNames.put(Integer.valueOf(ConnectivityLogManager.WIFI_HAL_DRIVER_DEVICE_EXCEPTION), "TIMING_ROTATE");
        effectNames.put(Integer.valueOf(300), "NUMBERPICKER_ITEMSCROLL");
        effectNames.put(Integer.valueOf(400), "NUMBERPICKER_TUNER");
        effectNames.put(Integer.valueOf(500), "FM_ADJUST");
        effectNames.put(Integer.valueOf(600), "FM_SPIN");
        effectNames.put(Integer.valueOf(700), "FM_ADJUST_DONE");
        effectNames.put(Integer.valueOf(800), "HOMESCREEN_ICON_FLY_WORKSPACE");
        effectNames.put(Integer.valueOf(900), "BULK_MOVE_ICONDROP");
        effectNames.put(Integer.valueOf(1000), "BULK_MOVE_ICON_GATHER");
        effectNames.put(Integer.valueOf(1100), "BULK_MOVE_ICONPICKUP");
        effectNames.put(Integer.valueOf(1200), "HOMESCREEN_SHAKE_ALIGN");
        effectNames.put(Integer.valueOf(1300), "LONG_PRESS");
        effectNames.put(Integer.valueOf(1400), "LONG_PRESS_WORKSPACE");
        effectNames.put(Integer.valueOf(1500), "SCROLL_INDICATOR_POP");
        effectNames.put(Integer.valueOf(1600), "CONTACT_ALPHA_SWITCH");
        effectNames.put(Integer.valueOf(1700), "TEXTVIEW_SELECTCHAR");
        effectNames.put(Integer.valueOf(1800), "TEXTVIEW_DOUBLE_TAP_SELECTWORD");
        effectNames.put(Integer.valueOf(1900), "TEXTVIEW_TAPWORD");
        effectNames.put(Integer.valueOf(System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT), "TEXTVIEW_SETCURSOR");
        effectNames.put(Integer.valueOf(2100), "WEATHER_RAIN");
        effectNames.put(Integer.valueOf(2200), "WEATHER_SAND");
        effectNames.put(Integer.valueOf(2300), "WEATHER_THUNDERSTORM");
        effectNames.put(Integer.valueOf(2400), "WEATHER_WINDY");
        effectNames.put(Integer.valueOf(2500), "LOCKSCREEN_UNLOCK");
        effectNames.put(Integer.valueOf(2600), "VIRTUAL_KEY");
    }

    public void playIvtEffect(int effectNo) {
        playIvtEffect((String) effectNames.get(Integer.valueOf(effectNo)));
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
        pausePlayEffect((String) effectNames.get(Integer.valueOf(effectNo)));
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
        resumePausedEffect((String) effectNames.get(Integer.valueOf(effectNo)));
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
        return isPlaying((String) effectNames.get(Integer.valueOf(effectNo)));
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
        return STATE_INVALID;
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        try {
            if (this.mService != null) {
                return this.mService.writeProtectArea(optItem, writeLen, writeBuf, errorNum);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        try {
            if (this.mService != null) {
                return this.mService.setSdCardCryptdEnable(enable, volId);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            if (this.mService != null) {
                return this.mService.unlockSdCardKey(userId, serialNumber, token, secret);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        try {
            if (this.mService != null) {
                return this.mService.addSdCardUserKeyAuth(userId, serialNumber, token, secret);
            }
        } catch (RemoteException e) {
            Slog.e(TAG, "HwGeneralManager binder error!");
        }
        return STATE_INVALID;
    }
}
