package com.android.server;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;
import android.util.Slog;
import com.android.internal.os.HwBootAnimationOeminfo;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipFile;

public class HwBootanimService {
    static final String BOOTANIMATION = "bootanimation";
    static final String SHUTANIMATIN = "shutdownanimation";
    static final int SWITCH_CLOSE = 2;
    static final int SWITCH_OPEN = 1;
    static final String TAG = "HwBootanimService";
    static final int VALUEE_RROR = -2;
    static final String aniamtion_path = "/system/media";
    static final String custBootSoundFile = "/data/cust/media/audio/animationsounds/bootSound.ogg";
    static final String custShutSoundFile = "/data/cust/media/audio/animationsounds/shutSound.ogg";
    static final String cust_aniamtion_path = "/data/cust/media";
    static final String cust_preinstall_aniamtion_path = "/cust/preinstalled/public/media";
    private static volatile HwBootanimService mInstance = null;
    private static boolean mIsBootOrShutdownSoundCapable = false;
    static final String oem_aniamtion_path = "/oem/media";
    static final String systemBootSoundFile = "/system/media/audio/animationsounds/bootSound.ogg";
    static final String systemShutSoundFile = "/system/media/audio/animationsounds/shutSound.ogg";
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.HwBootanimService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.HwBootanimService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwBootanimService.<clinit>():void");
    }

    public HwBootanimService(Context context) {
        this.mContext = context;
    }

    public static synchronized HwBootanimService getInstance(Context context) {
        HwBootanimService hwBootanimService;
        synchronized (HwBootanimService.class) {
            if (mInstance == null) {
                mInstance = new HwBootanimService(context);
            }
            hwBootanimService = mInstance;
        }
        return hwBootanimService;
    }

    public void switchBootOrShutSound(String openOrClose) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", null);
        int value = VALUEE_RROR;
        if (openOrClose.equals("open")) {
            value = SWITCH_OPEN;
        } else if (openOrClose.equals("close")) {
            value = SWITCH_CLOSE;
        }
        if (VALUEE_RROR == value) {
            Slog.d(TAG, "switchBootOrShutSound parameter error");
        } else {
            HwBootAnimationOeminfo.setBootAnimSoundSwitch(value);
        }
    }

    public int getBootAnimSoundSwitch() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", null);
        return HwBootAnimationOeminfo.getBootAnimSoundSwitch();
    }

    public boolean isBootOrShutdownSoundCapable() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.SHUTDOWN", null);
        Slog.d(TAG, "Boot or Shutdown sound is Capable :" + mIsBootOrShutdownSoundCapable);
        return mIsBootOrShutdownSoundCapable;
    }

    private static void setIsBootOrShutdownSoundCapable(boolean isBootOrShutdownSoundCapable) {
        mIsBootOrShutdownSoundCapable = isBootOrShutdownSoundCapable;
    }

    public void isBootOrShutdownSoundCapableForService() {
        setIsBootOrShutdownSoundCapable(isBootOrShutdownSoundCapableInter());
    }

    private boolean isBootOrShutdownSoundCapableInter() {
        String mccmnc = SystemProperties.get("persist.sys.mccmnc", PPPOEStateMachine.PHASE_DEAD);
        if (isPlaySound(mccmnc, BOOTANIMATION) && isBootSoundExist(mccmnc)) {
            return true;
        }
        if (isPlaySound(mccmnc, SHUTANIMATIN) && isShutSoundExist(mccmnc)) {
            return true;
        }
        return false;
    }

    private boolean isBootSoundExist(String mccmnc) {
        String BootSoundPath = null;
        boolean doseBootSoundFileExist = false;
        boolean dosemccmncBootSoundFileExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/audio/animationsounds/bootSound.ogg", 0) != null) {
                doseBootSoundFileExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            BootSoundPath = "media/audio/animationsounds/bootSound_" + mccmnc + ".ogg";
        }
        if (BootSoundPath != null) {
            try {
                if (HwCfgFilePolicy.getCfgFile(BootSoundPath, 0) != null) {
                    dosemccmncBootSoundFileExist = true;
                }
            } catch (NoClassDefFoundError e2) {
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            }
        }
        if (doseBootSoundFileExist || (BootSoundPath != null && dosemccmncBootSoundFileExist)) {
            return true;
        }
        boolean ifFileExsit = false;
        File custBootSoundF = new File(custBootSoundFile);
        File systemBootSoundF = new File(systemBootSoundFile);
        File file = null;
        String mccmncBootSoundPath = null;
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            mccmncBootSoundPath = "/data/cust/media/audio/animationsounds/bootSound_" + mccmnc + ".ogg";
        }
        if (mccmncBootSoundPath != null) {
            file = new File(mccmncBootSoundPath);
        }
        if (custBootSoundF.exists() || systemBootSoundF.exists() || (r6 != null && r6.exists())) {
            ifFileExsit = true;
        }
        return ifFileExsit;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isPlaySound(String mccmnc, String animtionName) {
        Object[] objArr;
        boolean equals;
        Throwable th;
        File animation = null;
        ZipFile zipFile = null;
        if (!PPPOEStateMachine.PHASE_DEAD.equals(mccmnc)) {
            objArr = new Object[SWITCH_CLOSE];
            objArr[0] = animtionName;
            objArr[SWITCH_OPEN] = mccmnc;
            animation = HwCfgFilePolicy.getCfgFile(String.format("media/%s_%s.zip", objArr), 0);
        }
        objArr = new Object[SWITCH_OPEN];
        objArr[0] = animtionName;
        String animPath = String.format("media/%s.zip", objArr);
        String decrypt = SystemProperties.get("vold.decrypt");
        objArr = new Object[SWITCH_OPEN];
        objArr[0] = animtionName;
        String encryptedAnimPath = String.format("media/%s-encrypted.zip", objArr);
        if (PPPOEStateMachine.PHASE_DEAD.equals(decrypt)) {
            equals = "trigger_restart_min_framework".equals(decrypt);
        } else {
            equals = true;
        }
        if (animation == null) {
            try {
                animation = HwCfgFilePolicy.getCfgFile(animPath, 0);
            } catch (NoClassDefFoundError e) {
                return false;
            }
        }
        if (animation == null) {
            if (BOOTANIMATION.equals(animtionName)) {
                animation = getCustAnimation(mccmnc, BOOTANIMATION);
            }
        }
        if (animation == null && equals) {
            try {
                animation = HwCfgFilePolicy.getCfgFile(encryptedAnimPath, 0);
            } catch (NoClassDefFoundError e2) {
                return false;
            }
        }
        if (animation == null) {
            if (SHUTANIMATIN.equals(animtionName)) {
                animation = getCustAnimation(mccmnc, SHUTANIMATIN);
            }
        }
        if (animation == null) {
            File animationTemp;
            objArr = new Object[SWITCH_CLOSE];
            objArr[0] = oem_aniamtion_path;
            objArr[SWITCH_OPEN] = animtionName;
            String oemAnimPath = String.format("%s/%s.zip", objArr);
            objArr = new Object[SWITCH_CLOSE];
            objArr[0] = aniamtion_path;
            objArr[SWITCH_OPEN] = animtionName;
            animPath = String.format("%s/%s.zip", objArr);
            objArr = new Object[SWITCH_CLOSE];
            objArr[0] = aniamtion_path;
            objArr[SWITCH_OPEN] = animtionName;
            encryptedAnimPath = String.format("%s/%s-encrypted.zip", objArr);
            if (equals) {
                animationTemp = new File(encryptedAnimPath);
            }
            animationTemp = new File(oemAnimPath);
            if (!animationTemp.exists()) {
                animationTemp = new File(animPath);
            }
            animation = animationTemp;
        }
        if (animation != null && animation.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                ZipFile bootanimationZip = new ZipFile(animation);
                try {
                    inputStream = bootanimationZip.getInputStream(bootanimationZip.getEntry("desc.txt"));
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                    while (true) {
                        try {
                            String cha = br.readLine();
                            if (cha == null) {
                                break;
                            }
                            if (cha.endsWith(PPPOEStateMachine.PHASE_INITIALIZE)) {
                                if (cha.split("[^\\w\\d]").length == 6) {
                                    break;
                                }
                            }
                        } catch (IOException e3) {
                            bufferedReader = br;
                            zipFile = bootanimationZip;
                        } catch (Throwable th2) {
                            th = th2;
                            bufferedReader = br;
                            zipFile = bootanimationZip;
                        }
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e4) {
                            Slog.w(TAG, "read close error");
                        } catch (Throwable th3) {
                        }
                    }
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e5) {
                            Slog.w(TAG, "br close error");
                        } catch (Throwable th4) {
                        }
                    }
                    if (bootanimationZip != null) {
                        try {
                            bootanimationZip.close();
                        } catch (IOException e6) {
                            Slog.w(TAG, "bootanimationZip close error");
                        } catch (Throwable th5) {
                        }
                    }
                    return true;
                } catch (IOException e7) {
                    zipFile = bootanimationZip;
                    try {
                        Slog.d(TAG, "PlaySound IO error");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e8) {
                                Slog.w(TAG, "read close error");
                            } catch (Throwable th6) {
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e9) {
                                Slog.w(TAG, "br close error");
                            } catch (Throwable th7) {
                            }
                        }
                        if (zipFile != null) {
                            try {
                                zipFile.close();
                            } catch (IOException e10) {
                                Slog.w(TAG, "bootanimationZip close error");
                            } catch (Throwable th8) {
                            }
                        }
                        return false;
                    } catch (Throwable th9) {
                        th = th9;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e11) {
                                Slog.w(TAG, "read close error");
                            } catch (Throwable th10) {
                            }
                        }
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e12) {
                                Slog.w(TAG, "br close error");
                            } catch (Throwable th11) {
                            }
                        }
                        if (zipFile != null) {
                            try {
                                zipFile.close();
                            } catch (IOException e13) {
                                Slog.w(TAG, "bootanimationZip close error");
                            } catch (Throwable th12) {
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th13) {
                    th = th13;
                    zipFile = bootanimationZip;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (zipFile != null) {
                        zipFile.close();
                    }
                    throw th;
                }
            } catch (IOException e14) {
                Slog.d(TAG, "PlaySound IO error");
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (zipFile != null) {
                    zipFile.close();
                }
                return false;
            }
        }
        return false;
    }

    private File getCustAnimation(String mccmnc, String animtionName) {
        Object[] objArr = new Object[SWITCH_CLOSE];
        objArr[0] = cust_aniamtion_path;
        objArr[SWITCH_OPEN] = animtionName;
        String custAnimPath = String.format("%s/%s.zip", objArr);
        objArr = new Object[SWITCH_CLOSE];
        objArr[0] = cust_preinstall_aniamtion_path;
        objArr[SWITCH_OPEN] = animtionName;
        String custPreinstallAnimPath = String.format("%s/%s.zip", objArr);
        File file = null;
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            file = new File(String.format("%s/%s_%s.zip", new Object[]{cust_aniamtion_path, animtionName, mccmnc}));
        }
        if (file == null || !file.exists()) {
            file = new File(custAnimPath);
            if (!file.exists()) {
                file = new File(custPreinstallAnimPath);
                if (file.exists()) {
                    return file;
                }
                return null;
            }
        }
        return file;
    }

    private boolean isShutSoundExist(String mccmnc) {
        String ShutSoundPath = null;
        boolean doseShutSoundFileExist = false;
        boolean dosemccmncShutSoundFileExist = false;
        try {
            if (HwCfgFilePolicy.getCfgFile("media/audio/animationsounds/shutSound.ogg", 0) != null) {
                doseShutSoundFileExist = true;
            }
        } catch (NoClassDefFoundError e) {
            Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
        }
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            ShutSoundPath = "media/audio/animationsounds/shutSound_" + mccmnc + ".ogg";
        }
        if (ShutSoundPath != null) {
            try {
                if (HwCfgFilePolicy.getCfgFile(ShutSoundPath, 0) != null) {
                    dosemccmncShutSoundFileExist = true;
                }
            } catch (NoClassDefFoundError e2) {
                Log.d(TAG, "HwCfgFilePolicy NoClassDefFoundError");
            }
        }
        if (doseShutSoundFileExist || (ShutSoundPath != null && dosemccmncShutSoundFileExist)) {
            return true;
        }
        boolean ifFileExsit = false;
        File custShutSoundF = new File(custShutSoundFile);
        File systemShutSoundF = new File(systemShutSoundFile);
        File file = null;
        String mccmncShutSoundPath = null;
        if (!mccmnc.equals(PPPOEStateMachine.PHASE_DEAD)) {
            mccmncShutSoundPath = "/data/cust/media/audio/animationsounds/shutSound_" + mccmnc + ".ogg";
        }
        if (mccmncShutSoundPath != null) {
            file = new File(mccmncShutSoundPath);
        }
        if (custShutSoundF.exists() || systemShutSoundF.exists() || (r6 != null && r6.exists())) {
            ifFileExsit = true;
        }
        return ifFileExsit;
    }
}
