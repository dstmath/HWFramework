package tmsdk.common.utils;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.utils.l.a;
import tmsdkobf.lu;
import tmsdkobf.md;
import tmsdkobf.ov;

public class e {
    private static Integer LE = null;
    private static String TAG = "EnvUtil";

    public static String[] D(Context context) {
        String[] strArr = new String[4];
        strArr[0] = Build.MODEL;
        strArr[1] = VERSION.RELEASE;
        String str = "";
        try {
            str = lu.bN("/proc/cpuinfo").split("\\n")[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        strArr[2] = str;
        strArr[3] = Integer.toString(l.Q(context)) + "*" + Integer.toString(l.R(context));
        return strArr;
    }

    public static String E(Context context) {
        int i = 1;
        String str = new String();
        String[] strArr = new String[4];
        strArr = D(context);
        str = (((((((str + "MODEL " + strArr[0] + ";") + "ANDROID " + strArr[1] + ";") + "CPU " + strArr[2] + ";") + "CPUFreq " + iz() + ";") + "CPUNum " + Runtime.getRuntime().availableProcessors() + ";") + "resolution " + strArr[3] + ";") + "ram " + l.iV() + ";") + "rom " + iA() + ";";
        a aVar = new a();
        l.a(aVar);
        str = str + "sdcard " + aVar.LN + ";";
        boolean iM = l.iM();
        StringBuilder append = new StringBuilder().append(str).append("simNum ");
        if (iM) {
            i = 2;
        }
        str = (append.append(i).append(";").toString() + "baseband " + SystemProperties.get("gsm.version.baseband", "") + ";") + "inversion " + Build.DISPLAY + ";";
        String string = new md("NetInterfaceManager").getString("upload_config_des", null);
        return (string == null || string.length() == 0) ? str : str + string;
    }

    public static boolean F(Context context) {
        ov a = TMServiceFactory.getSystemInfoService().a(context.getPackageName(), 1);
        return a != null && a.hx();
    }

    public static long iA() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return ((long) statFs.getBlockSize()) * ((long) statFs.getBlockCount());
    }

    public static int iB() {
        return !ScriptHelper.isSuExist ? 0 : ScriptHelper.getRootState() != 0 ? 2 : 1;
    }

    public static int iC() {
        if (LE == null) {
            try {
                File[] listFiles = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return Pattern.matches("cpu[0-9]", file.getName());
                    }
                });
                if (listFiles == null) {
                    return 1;
                }
                f.d(TAG, "CPU Count: " + listFiles.length);
                LE = Integer.valueOf(listFiles.length);
            } catch (Throwable th) {
                f.g(TAG, th);
                return 1;
            }
        }
        return LE.intValue();
    }

    public static long iD() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getPath());
        return (((long) statFs.getBlockSize()) * ((long) statFs.getBlockCount())) + iA();
    }

    public static String iz() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStream inputStream = new ProcessBuilder(new String[]{"/system/bin/cat", "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"}).start().getInputStream();
            byte[] bArr = new byte[24];
            while (inputStream.read(bArr) != -1) {
                stringBuilder.append(new String(bArr));
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            stringBuilder = new StringBuilder("N/A");
        }
        return stringBuilder.toString().trim();
    }
}
