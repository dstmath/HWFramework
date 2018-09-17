package com.android.server;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Flog;
import android.util.Log;
import android.util.Slog;
import com.android.server.VibetonzProxy.IVibetonzImpl;
import com.android.server.devicepolicy.StorageUtils;
import dalvik.system.PathClassLoader;
import huawei.android.os.IHwGeneralManager.Stub;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class HwGeneralService extends Stub {
    private static String CFG_FILE_SCREEN = "/sys/class/graphics/fb0/panel_info";
    private static String FILE_PATH = "sys/touchscreen/supported_func_indicater";
    private static final int NOT_SUPPORT_SDLOCK = -1;
    public static final int RESULT_NOT_SUPPORT = -10;
    static final String TAG = "HwGeneralService";
    IDeviceIdleController dic = null;
    private boolean init = false;
    private Context mContext;
    private Handler mHandler;
    private IVibetonzImpl mIVibetonzImpl = this.mVibetonzProxy.getInstance();
    private int mIsCurveScreen = -1;
    private boolean mIsSupportForce = false;
    private float mLimit = 0.0f;
    private ContentObserver mPressLimitObserver;
    private final ContentResolver mResolver;
    private VibetonzProxy mVibetonzProxy = new VibetonzProxy();

    public HwGeneralService(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        if (getSDLockSupport()) {
            HwSdLockService.getInstance(context);
        }
        if (getCryptsdSupport()) {
            HwSdCryptdService.getInstance(context);
        }
        this.mResolver = context.getContentResolver();
        initObserver(handler);
    }

    public int setSDLockPassword(String pw) {
        if (getSDLockSupport()) {
            return HwSdLockService.getInstance(this.mContext).setSDLockPassword(pw);
        }
        return -1;
    }

    public int clearSDLockPassword() {
        if (getSDLockSupport()) {
            return HwSdLockService.getInstance(this.mContext).clearSDLockPassword();
        }
        return -1;
    }

    public int unlockSDCard(String pw) {
        if (getSDLockSupport()) {
            return HwSdLockService.getInstance(this.mContext).unlockSDCard(pw);
        }
        return -1;
    }

    public void eraseSDLock() {
        if (getSDLockSupport()) {
            HwSdLockService.getInstance(this.mContext).eraseSDLock();
        }
    }

    public int getSDLockState() {
        if (getSDLockSupport()) {
            return HwSdLockService.getInstance(this.mContext).getSDLockState();
        }
        return -1;
    }

    public String getSDCardId() {
        if (getSDLockSupport()) {
            return HwSdLockService.getInstance(this.mContext).getSDCardId();
        }
        return null;
    }

    private boolean getSDLockSupport() {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean state = SystemProperties.getBoolean("ro.config.support_sdcard_lock", true);
            return state;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void startFileBackup() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.HUAWEI_SYSTEM_NODE_ACCESS", null);
        SystemProperties.set("ctl.start", "filebackup");
    }

    public int forceIdle() {
        if (1000 != Binder.getCallingUid()) {
            Slog.e(TAG, "HwGeneralService:forceIdle, permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        }
        if (this.dic == null) {
            this.dic = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        }
        if (this.dic != null) {
            try {
                return this.dic.forceIdle();
            } catch (RemoteException e) {
            }
        }
        return -1;
    }

    static {
        try {
            System.loadLibrary("general_jni");
        } catch (UnsatisfiedLinkError e) {
            Slog.e(TAG, "Load general libarary failed >>>>>" + e);
        }
    }

    public boolean isSupportForce() {
        if (!this.init) {
            initForce();
            this.init = true;
        }
        return this.mIsSupportForce;
    }

    public boolean isCurveScreen() {
        if (this.mIsCurveScreen == -1) {
            initScreenState();
        }
        if (this.mIsCurveScreen == 1) {
            return true;
        }
        return false;
    }

    private void parseScreenParams(String strParas) {
        String[] params = strParas.split(",");
        for (String param : params) {
            int pos = param.indexOf(":");
            if (pos > 0 && pos < param.length()) {
                String key = param.substring(0, pos).trim();
                String val = param.substring(pos + 1).trim();
                if ("curved".equalsIgnoreCase(key)) {
                    this.mIsCurveScreen = val.equals("1") ? 1 : 0;
                }
            }
        }
        Log.i(TAG, "parseScreenParams " + this.mIsCurveScreen + " " + strParas);
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x0105 A:{SYNTHETIC, Splitter: B:39:0x0105} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x010a A:{SYNTHETIC, Splitter: B:42:0x010a} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00b7 A:{SYNTHETIC, Splitter: B:26:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00bc A:{SYNTHETIC, Splitter: B:29:0x00bc} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x014b A:{SYNTHETIC, Splitter: B:50:0x014b} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0150 A:{SYNTHETIC, Splitter: B:53:0x0150} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0105 A:{SYNTHETIC, Splitter: B:39:0x0105} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x010a A:{SYNTHETIC, Splitter: B:42:0x010a} */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x00b7 A:{SYNTHETIC, Splitter: B:26:0x00b7} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00bc A:{SYNTHETIC, Splitter: B:29:0x00bc} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x014b A:{SYNTHETIC, Splitter: B:50:0x014b} */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0150 A:{SYNTHETIC, Splitter: B:53:0x0150} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initScreenState() {
        Throwable th;
        BufferedReader reader = null;
        String line = null;
        this.mIsCurveScreen = 0;
        Log.v(TAG, "sercie initScreenState" + this.mIsCurveScreen);
        FileInputStream fis = null;
        try {
            BufferedReader reader2;
            FileInputStream fis2 = new FileInputStream(CFG_FILE_SCREEN);
            try {
                reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
            } catch (FileNotFoundException e) {
                fis = fis2;
                Log.e(TAG, "initScreenState file not found exception.");
                if (reader != null) {
                }
                if (fis != null) {
                }
                Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
            } catch (IOException e2) {
                fis = fis2;
                try {
                    Log.e(TAG, "initScreenState file access io exception.");
                    if (reader != null) {
                    }
                    if (fis != null) {
                    }
                    Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                    }
                    if (fis != null) {
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e3) {
                        Log.e(TAG, "initScreenState reader close exception: " + e3);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e32) {
                        Log.e(TAG, "initScreenState fis close exception: " + e32);
                    }
                }
                throw th;
            }
            try {
                line = reader2.readLine();
                if (line != null) {
                    parseScreenParams(line);
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e322) {
                        Log.e(TAG, "initScreenState reader close exception: " + e322);
                    }
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (IOException e3222) {
                        Log.e(TAG, "initScreenState fis close exception: " + e3222);
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e4) {
                fis = fis2;
                reader = reader2;
                Log.e(TAG, "initScreenState file not found exception.");
                if (reader != null) {
                }
                if (fis != null) {
                }
                Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
            } catch (IOException e5) {
                fis = fis2;
                reader = reader2;
                Log.e(TAG, "initScreenState file access io exception.");
                if (reader != null) {
                }
                if (fis != null) {
                }
                Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
            } catch (Throwable th4) {
                th = th4;
                fis = fis2;
                reader = reader2;
                if (reader != null) {
                }
                if (fis != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            Log.e(TAG, "initScreenState file not found exception.");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e32222) {
                    Log.e(TAG, "initScreenState reader close exception: " + e32222);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e322222) {
                    Log.e(TAG, "initScreenState fis close exception: " + e322222);
                }
            }
            Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
        } catch (IOException e7) {
            Log.e(TAG, "initScreenState file access io exception.");
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e3222222) {
                    Log.e(TAG, "initScreenState reader close exception: " + e3222222);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e32222222) {
                    Log.e(TAG, "initScreenState fis close exception: " + e32222222);
                }
            }
            Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
        }
        Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
    }

    /* JADX WARNING: Removed duplicated region for block: B:41:0x00f6 A:{SYNTHETIC, Splitter: B:41:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00fb A:{SYNTHETIC, Splitter: B:44:0x00fb} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a8 A:{SYNTHETIC, Splitter: B:28:0x00a8} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ad A:{SYNTHETIC, Splitter: B:31:0x00ad} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x013c A:{SYNTHETIC, Splitter: B:52:0x013c} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0141 A:{SYNTHETIC, Splitter: B:55:0x0141} */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00f6 A:{SYNTHETIC, Splitter: B:41:0x00f6} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00fb A:{SYNTHETIC, Splitter: B:44:0x00fb} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x00a8 A:{SYNTHETIC, Splitter: B:28:0x00a8} */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x00ad A:{SYNTHETIC, Splitter: B:31:0x00ad} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x013c A:{SYNTHETIC, Splitter: B:52:0x013c} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0141 A:{SYNTHETIC, Splitter: B:55:0x0141} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void initForce() {
        Throwable th;
        BufferedReader reader = null;
        int line2int = 0;
        FileInputStream fis = null;
        try {
            BufferedReader reader2;
            FileInputStream fis2 = new FileInputStream(FILE_PATH);
            try {
                reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
            } catch (FileNotFoundException e) {
                fis = fis2;
                Log.e(TAG, "initForce file not found exception.");
                if (reader != null) {
                }
                if (fis != null) {
                }
                Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
            } catch (IOException e2) {
                fis = fis2;
                try {
                    Log.e(TAG, "initForce file access io exception.");
                    if (reader != null) {
                    }
                    if (fis != null) {
                    }
                    Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
                } catch (Throwable th2) {
                    th = th2;
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (Exception e3) {
                            Log.e(TAG, "initForce reader close exception: " + e3);
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (Exception e32) {
                            Log.e(TAG, "initForce fis close exception: " + e32);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fis = fis2;
                if (reader != null) {
                }
                if (fis != null) {
                }
                throw th;
            }
            try {
                String line = reader2.readLine();
                if (line != null) {
                    line2int = Integer.parseInt(line.trim());
                    if ((line2int & 16) == 16) {
                        this.mIsSupportForce = true;
                    }
                }
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (Exception e322) {
                        Log.e(TAG, "initForce reader close exception: " + e322);
                    }
                }
                if (fis2 != null) {
                    try {
                        fis2.close();
                    } catch (Exception e3222) {
                        Log.e(TAG, "initForce fis close exception: " + e3222);
                    }
                }
                reader = reader2;
            } catch (FileNotFoundException e4) {
                fis = fis2;
                reader = reader2;
                Log.e(TAG, "initForce file not found exception.");
                if (reader != null) {
                }
                if (fis != null) {
                }
                Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
            } catch (IOException e5) {
                fis = fis2;
                reader = reader2;
                Log.e(TAG, "initForce file access io exception.");
                if (reader != null) {
                }
                if (fis != null) {
                }
                Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
            } catch (Throwable th4) {
                th = th4;
                fis = fis2;
                reader = reader2;
                if (reader != null) {
                }
                if (fis != null) {
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            Log.e(TAG, "initForce file not found exception.");
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e32222) {
                    Log.e(TAG, "initForce reader close exception: " + e32222);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e322222) {
                    Log.e(TAG, "initForce fis close exception: " + e322222);
                }
            }
            Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
        } catch (IOException e7) {
            Log.e(TAG, "initForce file access io exception.");
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e3222222) {
                    Log.e(TAG, "initForce reader close exception: " + e3222222);
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e32222222) {
                    Log.e(TAG, "initForce fis close exception: " + e32222222);
                }
            }
            Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
        }
        Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
    }

    private void initObserver(Handler handler) {
        this.mPressLimitObserver = new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                HwGeneralService.this.mLimit = System.getFloatForUser(HwGeneralService.this.mContext.getContentResolver(), "pressure_habit_threshold", 0.2f, ActivityManager.getCurrentUser());
                Flog.i(1504, "initObserver limit = " + HwGeneralService.this.mLimit);
            }
        };
        this.mResolver.registerContentObserver(System.getUriFor("pressure_habit_threshold"), false, this.mPressLimitObserver, -1);
        this.mLimit = System.getFloatForUser(this.mContext.getContentResolver(), "pressure_habit_threshold", 0.2f, ActivityManager.getCurrentUser());
    }

    public float getPressureLimit() {
        return this.mLimit;
    }

    public void playIvtEffect(String effectName) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            Log.e(TAG, "playIvtEffect Method requires android.Manifest.permission.VIBRATE permission");
        } else {
            this.mIVibetonzImpl.playIvtEffect(effectName);
        }
    }

    public void stopPlayEffect() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            Log.e(TAG, "stopPlayEffect Method requires android.Manifest.permission.VIBRATE permission");
        } else {
            this.mIVibetonzImpl.stopPlayEffect();
        }
    }

    public void pausePlayEffect(String effectName) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            Log.e(TAG, "pausePlayEffect Method requires android.Manifest.permission.VIBRATE permission");
        } else {
            this.mIVibetonzImpl.pausePlayEffect(effectName);
        }
    }

    public void resumePausedEffect(String effectName) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            Log.e(TAG, "resumePausedEffect Method requires android.Manifest.permission.VIBRATE permission");
        } else {
            this.mIVibetonzImpl.resumePausedEffect(effectName);
        }
    }

    public boolean isPlaying(String effectName) {
        return this.mIVibetonzImpl.isPlaying(effectName);
    }

    public boolean startHaptic(int callerID, int ringtoneType, Uri uri) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") == 0) {
            return this.mIVibetonzImpl.startHaptic(this.mContext, callerID, ringtoneType, uri);
        }
        Log.e(TAG, "startHaptic Method requires android.Manifest.permission.VIBRATE permission");
        return false;
    }

    public boolean hasHaptic(Uri uri) {
        return this.mIVibetonzImpl.hasHaptic(this.mContext, uri);
    }

    public void stopHaptic() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") != 0) {
            Log.e(TAG, "stopHaptic Method requires android.Manifest.permission.VIBRATE permission");
        } else {
            this.mIVibetonzImpl.stopHaptic();
        }
    }

    public void resetTouchWeight() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.CHECK_TOUCH_WEIGHT", null);
        HwTouchWeightService.getInstance(this.mContext, this.mHandler).resetTouchWeight();
    }

    public String getTouchWeightValue() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.CHECK_TOUCH_WEIGHT", null);
        return HwTouchWeightService.getInstance(this.mContext, this.mHandler).getTouchWeightValue();
    }

    public boolean mkDataDir(String path) {
        if (1000 != Binder.getCallingUid()) {
            Slog.e(TAG, "HwGeneralService:mkDataDir, permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else if (path == null || "".equals(path)) {
            Log.e(TAG, "path is null");
            return false;
        } else if (path.startsWith("/data/")) {
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
            return file.mkdir();
        } else {
            Log.e(TAG, "path not startsWith data dir");
            return false;
        }
    }

    public Messenger getTestService() {
        if (!StorageUtils.SDCARD_ROMOUNTED_STATE.equals(SystemProperties.get("ro.emui.test", StorageUtils.SDCARD_RWMOUNTED_STATE))) {
            return null;
        }
        try {
            PathClassLoader loader = new PathClassLoader("/system/framework/HwServiceTest.jar", getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(loader);
            Class clazz = loader.loadClass("com.huawei.test.systemserver.service.TestRunnerService");
            return (Messenger) clazz.getMethod("onBind", null).invoke(clazz.getConstructor(new Class[]{Context.class}).newInstance(new Object[]{this.mContext}), null);
        } catch (Throwable e) {
            Log.e(TAG, "can  not get test service", e);
            return null;
        }
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        int pid = Binder.getCallingPid();
        Slog.i(TAG, "readProtectArea: callingPid is " + pid + ",callingUid is " + Binder.getCallingUid());
        return HwProtectAreaService.getInstance(this.mContext).readProtectArea(optItem, readBufLen, readBuf, errorNum);
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        int pid = Binder.getCallingPid();
        Slog.i(TAG, "writeProtectArea: callingPid is " + pid + ",callingUid is " + Binder.getCallingUid());
        return HwProtectAreaService.getInstance(this.mContext).writeProtectArea(optItem, writeLen, writeBuf, errorNum);
    }

    public int setSdCardCryptdEnable(boolean enable, String volId) {
        if (getCryptsdSupport()) {
            return HwSdCryptdService.getInstance(this.mContext).setSdCardCryptdEnable(enable, volId);
        }
        return -10;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (getCryptsdSupport()) {
            return HwSdCryptdService.getInstance(this.mContext).unlockSdCardKey(userId, serialNumber, token, secret);
        }
        return -10;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (getCryptsdSupport()) {
            return HwSdCryptdService.getInstance(this.mContext).addSdCardUserKeyAuth(userId, serialNumber, token, secret);
        }
        return -10;
    }

    private boolean getCryptsdSupport() {
        long ident = Binder.clearCallingIdentity();
        try {
            boolean state = SystemProperties.getBoolean("ro.config.support_sdcard_crypt", true);
            return state;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int backupSecretkey() {
        if (getCryptsdSupport()) {
            return HwSdCryptdService.getInstance(this.mContext).backupSecretkey();
        }
        return -10;
    }

    public boolean supportHwPush() {
        File jarFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
        if (jarFile != null && jarFile.exists()) {
            Slog.i(TAG, "push jarFile is exist in cust");
            return true;
        } else if (new File("/system/framework/hwpush.jar").exists()) {
            Slog.i(TAG, "push jarFile is exist in system");
            return true;
        } else {
            Slog.i(TAG, "push jarFile is not exist");
            return false;
        }
    }
}
