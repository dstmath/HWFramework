package com.android.server;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Flog;
import android.util.Log;
import com.android.server.VibetonzProxy;
import com.android.server.gesture.GestureNavConst;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.content.ContentResolverExt;
import com.huawei.android.os.IDeviceIdleControllerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.storage.StorageManagerInternalEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import dalvik.system.PathClassLoader;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HwGeneralService extends DefaultHwGeneralService {
    private static final String CFG_FILE_SCREEN = "/sys/class/graphics/fb0/panel_info";
    private static final String FILE_PATH = "sys/touchscreen/supported_func_indicater";
    private static final boolean IS_FORCE_TOUCH_DISABLED = SystemPropertiesEx.getBoolean("ro.config.disable_force_touch", false);
    private static final int NOT_SUPPORT_SDLOCK = -1;
    public static final int RESULT_NOT_SUPPORT = -10;
    static final String TAG = "HwGeneralService";
    private static final String WRITE_MEDIA_STORAGE = "android.permission.WRITE_MEDIA_STORAGE";
    private Context mContext;
    IDeviceIdleControllerEx mDeviceIdleControllerEx = null;
    private Handler mHandler;
    private VibetonzProxy.IVibetonzImpl mIVibetonzImpl = this.mVibetonzProxy.getInstance();
    private int mIsCurveScreen = -1;
    private boolean mIsInit = false;
    private boolean mIsSupportForce = false;
    private float mLimit = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private PathClassLoader mLoader = null;
    private ContentObserver mPressLimitObserver;
    private final ContentResolver mResolver;
    private VibetonzProxy mVibetonzProxy = new VibetonzProxy();

    static {
        try {
            System.loadLibrary("general_jni");
        } catch (UnsatisfiedLinkError e) {
            SlogEx.e(TAG, "Load general libarary failed >>>>>");
        }
    }

    public HwGeneralService(Context context, Handler handler) {
        super(context, handler);
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
        if (!getSDLockSupport()) {
            return -1;
        }
        return HwSdLockService.getInstance(this.mContext).setSDLockPassword(pw);
    }

    public int clearSDLockPassword() {
        if (!getSDLockSupport()) {
            return -1;
        }
        return HwSdLockService.getInstance(this.mContext).clearSDLockPassword();
    }

    public int unlockSDCard(String pw) {
        if (!getSDLockSupport()) {
            return -1;
        }
        return HwSdLockService.getInstance(this.mContext).unlockSDCard(pw);
    }

    public void eraseSDLock() {
        if (getSDLockSupport()) {
            HwSdLockService.getInstance(this.mContext).eraseSDLock();
        }
    }

    public int getSDLockState() {
        if (!getSDLockSupport()) {
            return -1;
        }
        return HwSdLockService.getInstance(this.mContext).getSDLockState();
    }

    public String getSDCardId() {
        if (!getSDLockSupport()) {
            return null;
        }
        return HwSdLockService.getInstance(this.mContext).getSDCardId();
    }

    private boolean getSDLockSupport() {
        long ident = Binder.clearCallingIdentity();
        try {
            return SystemPropertiesEx.getBoolean("ro.config.support_sdcard_lock", true);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public void startFileBackup() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.HUAWEI_SYSTEM_NODE_ACCESS", null);
        SystemPropertiesEx.set("ctl.start", "filebackup");
    }

    public int forceIdle() {
        if (Binder.getCallingUid() != 1000) {
            SlogEx.e(TAG, "HwGeneralService:forceIdle, permission not allowed. uid = " + Binder.getCallingUid());
            return -1;
        }
        if (this.mDeviceIdleControllerEx == null) {
            this.mDeviceIdleControllerEx = new IDeviceIdleControllerEx();
        }
        IDeviceIdleControllerEx iDeviceIdleControllerEx = this.mDeviceIdleControllerEx;
        if (iDeviceIdleControllerEx != null) {
            try {
                return iDeviceIdleControllerEx.forceIdle();
            } catch (RemoteException e) {
                Log.e(TAG, " forceIdle RemoteException");
            }
        }
        return -1;
    }

    public boolean isSupportForce() {
        if (!this.mIsInit) {
            initForce();
            this.mIsInit = true;
            if (IS_FORCE_TOUCH_DISABLED) {
                Log.i(TAG, "IS_FORCE_TOUCH_DISABLED = " + IS_FORCE_TOUCH_DISABLED);
                this.mIsSupportForce = false;
            }
        }
        return this.mIsSupportForce;
    }

    public boolean isCurveScreen() {
        if (this.mIsCurveScreen == -1) {
            initScreenState();
        }
        return this.mIsCurveScreen == 1;
    }

    private void parseScreenParams(String strParas) {
        String[] params = strParas.split(AwarenessInnerConstants.COMMA_KEY);
        for (String param : params) {
            int pos = param.indexOf(AwarenessInnerConstants.COLON_KEY);
            if (pos > 0 && pos < param.length()) {
                String key = param.substring(0, pos).trim();
                String val = param.substring(pos + 1).trim();
                if ("curved".equalsIgnoreCase(key)) {
                    this.mIsCurveScreen = "1".equals(val) ? 1 : 0;
                }
            }
        }
        Log.i(TAG, "parseScreenParams " + this.mIsCurveScreen + " " + strParas);
    }

    private void initScreenState() {
        StringBuilder sb;
        BufferedReader reader = null;
        String line = null;
        this.mIsCurveScreen = 0;
        Log.v(TAG, "sercie initScreenState" + this.mIsCurveScreen);
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(CFG_FILE_SCREEN);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
            String readLine = reader2.readLine();
            line = readLine;
            if (readLine != null) {
                parseScreenParams(line);
            }
            try {
                reader2.close();
            } catch (IOException e) {
                Log.e(TAG, "initScreenState reader close exception: " + e);
            }
            try {
                fis2.close();
            } catch (IOException e2) {
                e = e2;
                sb = new StringBuilder();
            }
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "initScreenState file not found exception.");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e4) {
                    Log.e(TAG, "initScreenState reader close exception: " + e4);
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e5) {
                    e = e5;
                    sb = new StringBuilder();
                }
            }
        } catch (IOException e6) {
            Log.e(TAG, "initScreenState file access io exception.");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e7) {
                    Log.e(TAG, "initScreenState reader close exception: " + e7);
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e8) {
                    e = e8;
                    sb = new StringBuilder();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e9) {
                    Log.e(TAG, "initScreenState reader close exception: " + e9);
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e10) {
                    Log.e(TAG, "initScreenState fis close exception: " + e10);
                }
            }
            throw th;
        }
        Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
        sb.append("initScreenState fis close exception: ");
        sb.append(e);
        Log.e(TAG, sb.toString());
        Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
    }

    private void initForce() {
        BufferedReader reader = null;
        int line2int = 0;
        FileInputStream fis = null;
        try {
            FileInputStream fis2 = new FileInputStream(FILE_PATH);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
            String line = reader2.readLine();
            if (line != null) {
                line2int = Integer.parseInt(line.trim());
                if ((line2int & 16) == 16) {
                    this.mIsSupportForce = true;
                }
            }
            try {
                reader2.close();
            } catch (Exception e) {
                Log.e(TAG, "initForce reader close catch Exception");
            }
            try {
                fis2.close();
            } catch (Exception e2) {
                Log.e(TAG, "initForce fis close catch Exception");
            }
        } catch (FileNotFoundException e3) {
            Log.e(TAG, "initForce file not found exception.");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e4) {
                    Log.e(TAG, "initForce reader close catch Exception");
                }
            }
            if (0 != 0) {
                fis.close();
            }
        } catch (IOException e5) {
            Log.e(TAG, "initForce file access io exception.");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e6) {
                    Log.e(TAG, "initForce reader close catch Exception");
                }
            }
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (Exception e7) {
                    Log.e(TAG, "initForce reader close catch Exception");
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (Exception e8) {
                    Log.e(TAG, "initForce fis close catch Exception");
                }
            }
            throw th;
        }
        Flog.i(1504, "initForce mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
    }

    private void initObserver(Handler handler) {
        this.mPressLimitObserver = new ContentObserver(handler) {
            /* class com.android.server.HwGeneralService.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwGeneralService hwGeneralService = HwGeneralService.this;
                hwGeneralService.mLimit = SettingsEx.System.getFloatForUser(hwGeneralService.mContext.getContentResolver(), "pressure_habit_threshold", 0.2f, ActivityManagerEx.getCurrentUser());
                Flog.i(1504, "initObserver limit = " + HwGeneralService.this.mLimit);
            }
        };
        ContentResolverExt.registerContentObserver(this.mResolver, Settings.System.getUriFor("pressure_habit_threshold"), false, this.mPressLimitObserver, -1);
        this.mLimit = SettingsEx.System.getFloatForUser(this.mContext.getContentResolver(), "pressure_habit_threshold", 0.2f, ActivityManagerEx.getCurrentUser());
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

    public boolean startHaptic(int callerId, int ringtoneType, Uri uri) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.VIBRATE") == 0) {
            return this.mIVibetonzImpl.startHaptic(this.mContext, callerId, ringtoneType, uri);
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
        if (Binder.getCallingUid() != 1000) {
            SlogEx.e(TAG, "HwGeneralService:mkDataDir, permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else if (path == null || "".equals(path)) {
            Log.e(TAG, "path is null");
            return false;
        } else if (!path.startsWith("/data/")) {
            Log.e(TAG, "path not startsWith data dir");
            return false;
        } else {
            File file = new File(path);
            if (file.exists()) {
                return true;
            }
            return file.mkdir();
        }
    }

    public Messenger getTestService() {
        if (!"true".equals(SystemPropertiesEx.get("ro.emui.test", "false"))) {
            return null;
        }
        try {
            if (this.mLoader == null) {
                Log.e(TAG, " mLoader is null -> new PathClassLoader");
                this.mLoader = new PathClassLoader("/system/framework/HwServiceTest.jar", getClass().getClassLoader());
            }
            Thread.currentThread().setContextClassLoader(this.mLoader);
            Class clazz = this.mLoader.loadClass("com.huawei.test.systemserver.service.TestRunnerService");
            return (Messenger) clazz.getMethod("onBind", new Class[0]).invoke(clazz.getConstructor(Context.class).newInstance(this.mContext), new Object[0]);
        } catch (Throwable th) {
            Log.e(TAG, "can not get test service");
            return null;
        }
    }

    public int readProtectArea(String optItem, int readBufLen, String[] readBuf, int[] errorNum) {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        SlogEx.i(TAG, "readProtectArea: callingPid is " + pid + ",callingUid is " + uid);
        return HwProtectAreaService.getInstance(this.mContext).readProtectArea(optItem, readBufLen, readBuf, errorNum);
    }

    public int writeProtectArea(String optItem, int writeLen, String writeBuf, int[] errorNum) {
        int pid = Binder.getCallingPid();
        int uid = Binder.getCallingUid();
        SlogEx.i(TAG, "writeProtectArea: callingPid is " + pid + ",callingUid is " + uid);
        return HwProtectAreaService.getInstance(this.mContext).writeProtectArea(optItem, writeLen, writeBuf, errorNum);
    }

    public int setSdCardCryptdEnable(boolean isEnable, String volId) {
        if (!getCryptsdSupport()) {
            return -10;
        }
        return HwSdCryptdService.getInstance(this.mContext).setSdCardCryptdEnable(isEnable, volId);
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (!getCryptsdSupport()) {
            return -10;
        }
        return HwSdCryptdService.getInstance(this.mContext).unlockSdCardKey(userId, serialNumber, token, secret);
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        if (!getCryptsdSupport()) {
            return -10;
        }
        return HwSdCryptdService.getInstance(this.mContext).addSdCardUserKeyAuth(userId, serialNumber, token, secret);
    }

    private boolean getCryptsdSupport() {
        long ident = Binder.clearCallingIdentity();
        try {
            return SystemPropertiesEx.getBoolean("ro.config.support_sdcard_crypt", true);
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    public int backupSecretkey() {
        if (!getCryptsdSupport()) {
            return -10;
        }
        return HwSdCryptdService.getInstance(this.mContext).backupSecretkey();
    }

    public boolean supportHwPush() {
        File jarFile = HwCfgFilePolicy.getCfgFile("jars/hwpush.jar", 0);
        if (jarFile != null && jarFile.exists()) {
            SlogEx.i(TAG, "push jarFile is exist in cust");
            return true;
        } else if (new File("/system/framework/hwpush.jar").exists()) {
            SlogEx.i(TAG, "push jarFile is exist in system");
            return true;
        } else {
            SlogEx.i(TAG, "push jarFile is not exist");
            return false;
        }
    }

    public long getPartitionInfo(String partitionName, int infoType) {
        return StorageManagerServiceEx.getPartitionInfo(partitionName, infoType);
    }

    public String mountCifs(String source, String option, IBinder binder) {
        return HwMountManagerService.getInstance(this.mContext).mountCifs(source, option, binder);
    }

    public void unmountCifs(String mountPoint) {
        HwMountManagerService.getInstance(this.mContext).unmountCifs(mountPoint);
    }

    public int isSupportedCifs() {
        return HwMountManagerService.getInstance(this.mContext).isSupportedCifs();
    }

    public int getLocalDevStat(int dev) {
        return HwLocalDevManagerService.getInstance(this.mContext).getLocalDevStat(dev);
    }

    public String getDeviceId(int dev) {
        return HwLocalDevManagerService.getInstance(this.mContext).getDeviceId(dev);
    }

    public int doSdcardCheckRW() {
        return HwLocalDevManagerService.getInstance(this.mContext).doSdcardCheckRW();
    }

    public boolean isIsolatedStorageApp(int uid, String packageName) {
        if (uid != Binder.getCallingUid()) {
            this.mContext.enforceCallingPermission(WRITE_MEDIA_STORAGE, "need permission:android.permission.WRITE_MEDIA_STORAGE");
        }
        StorageManagerInternalEx smi = new StorageManagerInternalEx();
        long token = Binder.clearCallingIdentity();
        try {
            int mode = smi.getExternalStorageMountMode(uid, packageName);
            boolean z = true;
            if (!(mode == 0 || mode == 1)) {
                z = false;
            }
            return z;
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    /* JADX INFO: finally extract failed */
    public String[] getIsolatedStorageApps(int excludeFlag) {
        this.mContext.enforceCallingPermission(WRITE_MEDIA_STORAGE, "need permission:android.permission.WRITE_MEDIA_STORAGE");
        StorageManagerInternalEx smi = new StorageManagerInternalEx();
        List<ApplicationInfo> list = this.mContext.getPackageManager().getInstalledApplications(0);
        List<String> isolatedApps = new ArrayList<>(0);
        long token = Binder.clearCallingIdentity();
        try {
            for (ApplicationInfo ai : list) {
                if ((ai.flags & excludeFlag) == 0) {
                    int mode = smi.getExternalStorageMountMode(ai.uid, ai.packageName);
                    if (mode == 0 || mode == 1) {
                        isolatedApps.add(ai.packageName);
                    }
                }
            }
            Binder.restoreCallingIdentity(token);
            return (String[]) isolatedApps.toArray(new String[isolatedApps.size()]);
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
            throw th;
        }
    }
}
