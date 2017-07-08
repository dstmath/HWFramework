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
import com.android.server.input.HwCircleAnimation;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.IOTController;
import dalvik.system.PathClassLoader;
import huawei.android.os.IHwGeneralManager.Stub;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class HwGeneralService extends Stub {
    private static String CFG_FILE_SCREEN = null;
    private static String FILE_PATH = null;
    private static final String HWOUC_PACKAGE_NAME = "com.huawei.android.hwouc";
    static final String TAG = "HwGeneralService";
    IDeviceIdleController dic;
    private boolean init;
    private Context mContext;
    private Handler mHandler;
    private IVibetonzImpl mIVibetonzImpl;
    private int mIsCurveScreen;
    private boolean mIsSupportForce;
    private float mLimit;
    private ContentObserver mPressLimitObserver;
    private final ContentResolver mResolver;
    private VibetonzProxy mVibetonzProxy;

    /* renamed from: com.android.server.HwGeneralService.1 */
    class AnonymousClass1 extends ContentObserver {
        AnonymousClass1(Handler $anonymous0) {
            super($anonymous0);
        }

        public void onChange(boolean selfChange) {
            HwGeneralService.this.mLimit = System.getFloatForUser(HwGeneralService.this.mContext.getContentResolver(), "pressure_habit_threshold", HwCircleAnimation.BG_ALPHA_FILL, ActivityManager.getCurrentUser());
            Flog.i(1504, "initObserver limit = " + HwGeneralService.this.mLimit);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwGeneralService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwGeneralService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwGeneralService.<clinit>():void");
    }

    public HwGeneralService(Context context, Handler handler) {
        this.dic = null;
        this.mVibetonzProxy = new VibetonzProxy();
        this.mIVibetonzImpl = this.mVibetonzProxy.getInstance();
        this.mIsCurveScreen = -1;
        this.init = false;
        this.mIsSupportForce = false;
        this.mLimit = 0.0f;
        this.mContext = context;
        this.mHandler = handler;
        HwBootanimService.getInstance(context).isBootOrShutdownSoundCapableForService();
        HwSdLockService.getInstance(context);
        this.mResolver = context.getContentResolver();
        initObserver(handler);
    }

    public void switchBootOrShutSound(String openOrClose) {
        String pacakgeName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (IOTController.TYPE_MASTER == Binder.getCallingUid() || HWOUC_PACKAGE_NAME.equals(pacakgeName)) {
            HwBootanimService.getInstance(this.mContext).switchBootOrShutSound(openOrClose);
        } else {
            Slog.e(TAG, "HwGeneralService:switchBootOrShutSound, permission not allowed. uid = " + Binder.getCallingUid());
        }
    }

    public int getBootAnimSoundSwitch() {
        String pacakgeName = this.mContext.getPackageManager().getNameForUid(Binder.getCallingUid());
        if (IOTController.TYPE_MASTER == Binder.getCallingUid() || HWOUC_PACKAGE_NAME.equals(pacakgeName)) {
            return HwBootanimService.getInstance(this.mContext).getBootAnimSoundSwitch();
        }
        Slog.e(TAG, "HwGeneralService:getBootAnimSoundSwitch, permission not allowed. uid = " + Binder.getCallingUid());
        return -1;
    }

    public boolean isBootOrShutdownSoundCapable() {
        if (IOTController.TYPE_MASTER == Binder.getCallingUid()) {
            return HwBootanimService.getInstance(this.mContext).isBootOrShutdownSoundCapable();
        }
        Slog.e(TAG, "HwGeneralService:isBootOrShutdownSoundCapable, permission not allowed. uid = " + Binder.getCallingUid());
        return false;
    }

    public int setSDLockPassword(String pw) {
        return HwSdLockService.getInstance(this.mContext).setSDLockPassword(pw);
    }

    public int clearSDLockPassword() {
        return HwSdLockService.getInstance(this.mContext).clearSDLockPassword();
    }

    public int unlockSDCard(String pw) {
        return HwSdLockService.getInstance(this.mContext).unlockSDCard(pw);
    }

    public void eraseSDLock() {
        HwSdLockService.getInstance(this.mContext).eraseSDLock();
    }

    public int getSDLockState() {
        return HwSdLockService.getInstance(this.mContext).getSDLockState();
    }

    public String getSDCardId() {
        return HwSdLockService.getInstance(this.mContext).getSDCardId();
    }

    public void startFileBackup() {
        this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.HUAWEI_SYSTEM_NODE_ACCESS", null);
        SystemProperties.set("ctl.start", "filebackup");
    }

    public int forceIdle() {
        if (IOTController.TYPE_MASTER != Binder.getCallingUid()) {
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
                    this.mIsCurveScreen = val.equals(PPPOEStateMachine.PHASE_INITIALIZE) ? 1 : 0;
                }
            }
        }
        Log.i(TAG, "parseScreenParams " + this.mIsCurveScreen + " " + strParas);
    }

    private void initScreenState() {
        BufferedReader reader;
        Throwable th;
        BufferedReader bufferedReader = null;
        String line = null;
        this.mIsCurveScreen = 0;
        Log.v(TAG, "sercie initScreenState" + this.mIsCurveScreen);
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fis = new FileInputStream(CFG_FILE_SCREEN);
            try {
                reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            } catch (FileNotFoundException e) {
                fileInputStream = fis;
                Log.e(TAG, "initScreenState file not found exception.");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "initScreenState reader close exception: " + e2);
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e22) {
                        Log.e(TAG, "initScreenState fis close exception: " + e22);
                    }
                }
                Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
            } catch (IOException e3) {
                fileInputStream = fis;
                try {
                    Log.e(TAG, "initScreenState file access io exception.");
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222) {
                            Log.e(TAG, "initScreenState reader close exception: " + e222);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2222) {
                            Log.e(TAG, "initScreenState fis close exception: " + e2222);
                        }
                    }
                    Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22222) {
                            Log.e(TAG, "initScreenState reader close exception: " + e22222);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e222222) {
                            Log.e(TAG, "initScreenState fis close exception: " + e222222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
            try {
                line = reader.readLine();
                if (line != null) {
                    parseScreenParams(line);
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2222222) {
                        Log.e(TAG, "initScreenState reader close exception: " + e2222222);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e22222222) {
                        Log.e(TAG, "initScreenState fis close exception: " + e22222222);
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e4) {
                fileInputStream = fis;
                bufferedReader = reader;
                Log.e(TAG, "initScreenState file not found exception.");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
            } catch (IOException e5) {
                fileInputStream = fis;
                bufferedReader = reader;
                Log.e(TAG, "initScreenState file access io exception.");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fis;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            Log.e(TAG, "initScreenState file not found exception.");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
        } catch (IOException e7) {
            Log.e(TAG, "initScreenState file access io exception.");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
        }
        Log.i(TAG, "initScreenState isCurveScreen = " + this.mIsCurveScreen + " params = " + line);
    }

    private void initForce() {
        Throwable th;
        BufferedReader bufferedReader = null;
        int line2int = 0;
        FileInputStream fileInputStream = null;
        try {
            BufferedReader reader;
            FileInputStream fis = new FileInputStream(FILE_PATH);
            try {
                reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            } catch (FileNotFoundException e) {
                fileInputStream = fis;
                Log.e(TAG, "initForce file not found exception.");
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e2) {
                        Log.e(TAG, "initForce reader close exception: " + e2);
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Exception e22) {
                        Log.e(TAG, "initForce fis close exception: " + e22);
                    }
                }
                Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
            } catch (IOException e3) {
                fileInputStream = fis;
                try {
                    Log.e(TAG, "initForce file access io exception.");
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e222) {
                            Log.e(TAG, "initForce reader close exception: " + e222);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Exception e2222) {
                            Log.e(TAG, "initForce fis close exception: " + e2222);
                        }
                    }
                    Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Exception e22222) {
                            Log.e(TAG, "initForce reader close exception: " + e22222);
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Exception e222222) {
                            Log.e(TAG, "initForce fis close exception: " + e222222);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
            try {
                String line = reader.readLine();
                if (line != null) {
                    line2int = Integer.parseInt(line.trim());
                    if ((line2int & 16) == 16) {
                        this.mIsSupportForce = true;
                    }
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e2222222) {
                        Log.e(TAG, "initForce reader close exception: " + e2222222);
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e22222222) {
                        Log.e(TAG, "initForce fis close exception: " + e22222222);
                    }
                }
                bufferedReader = reader;
            } catch (FileNotFoundException e4) {
                fileInputStream = fis;
                bufferedReader = reader;
                Log.e(TAG, "initForce file not found exception.");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
            } catch (IOException e5) {
                fileInputStream = fis;
                bufferedReader = reader;
                Log.e(TAG, "initForce file access io exception.");
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
            } catch (Throwable th4) {
                th = th4;
                fileInputStream = fis;
                bufferedReader = reader;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            Log.e(TAG, "initForce file not found exception.");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
        } catch (IOException e7) {
            Log.e(TAG, "initForce file access io exception.");
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
        }
        Flog.i(1504, "initForce   mIsSupportForce = " + this.mIsSupportForce + " line2int = " + line2int);
    }

    private void initObserver(Handler handler) {
        this.mPressLimitObserver = new AnonymousClass1(handler);
        this.mResolver.registerContentObserver(System.getUriFor("pressure_habit_threshold"), false, this.mPressLimitObserver, -1);
        this.mLimit = System.getFloatForUser(this.mContext.getContentResolver(), "pressure_habit_threshold", HwCircleAnimation.BG_ALPHA_FILL, ActivityManager.getCurrentUser());
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
        if (IOTController.TYPE_MASTER != Binder.getCallingUid()) {
            Slog.e(TAG, "HwGeneralService:mkDataDir, permission not allowed. uid = " + Binder.getCallingUid());
            return false;
        } else if (path == null || AppHibernateCst.INVALID_PKG.equals(path)) {
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
        if (!"true".equals(SystemProperties.get("ro.emui.test", "false"))) {
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
        return 0;
    }

    public int unlockSdCardKey(int userId, int serialNumber, byte[] token, byte[] secret) {
        return 0;
    }

    public int addSdCardUserKeyAuth(int userId, int serialNumber, byte[] token, byte[] secret) {
        return 0;
    }
}
