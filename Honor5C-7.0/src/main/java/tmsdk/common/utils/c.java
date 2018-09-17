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
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.h.a;
import tmsdkobf.ms;
import tmsdkobf.nc;
import tmsdkobf.np;
import tmsdkobf.ns;
import tmsdkobf.py;

/* compiled from: Unknown */
public class c {
    private static Integer KW;
    private static String TAG;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.utils.c.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.utils.c.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.utils.c.<clinit>():void");
    }

    public static String ip() {
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

    public static long iq() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return ((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize());
    }

    public static int ir() {
        return !ScriptHelper.isSuExist ? 0 : ScriptHelper.getRootState() != 0 ? 2 : 1;
    }

    public static int is() {
        if (KW == null) {
            try {
                File[] listFiles = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                    public boolean accept(File file) {
                        return Pattern.matches("cpu[0-9]", file.getName());
                    }
                });
                if (listFiles == null) {
                    return 1;
                }
                d.e(TAG, "CPU Count: " + listFiles.length);
                KW = Integer.valueOf(listFiles.length);
            } catch (Throwable th) {
                d.f(TAG, th);
                return 1;
            }
        }
        return KW.intValue();
    }

    public static long it() {
        StatFs statFs = new StatFs(Environment.getRootDirectory().getPath());
        return (((long) statFs.getBlockCount()) * ((long) statFs.getBlockSize())) + iq();
    }

    public static String[] w(Context context) {
        String str;
        String[] strArr = new String[4];
        strArr[0] = Build.MODEL;
        strArr[1] = VERSION.RELEASE;
        String str2 = "";
        try {
            str = ms.cz("/proc/cpuinfo").split("\\n")[0];
        } catch (Exception e) {
            e.printStackTrace();
            str = str2;
        }
        strArr[2] = str;
        strArr[3] = Integer.toString(h.H(context)) + "*" + Integer.toString(h.I(context));
        return strArr;
    }

    public static String x(Context context) {
        String str = new String();
        String[] strArr = new String[4];
        strArr = w(context);
        String str2 = (((((str + "MODEL " + strArr[0] + ";") + "ANDROID " + strArr[1] + ";") + "CPU " + strArr[2] + ";") + "CPUFreq " + ip() + ";") + "CPUNum " + Runtime.getRuntime().availableProcessors() + ";") + "resolution " + strArr[3] + ";";
        np fv = ((ns) ManagerCreatorC.getManager(ns.class)).fv();
        str = (fv == null ? str2 : str2 + "ram " + fv.fu() + ";") + "rom " + iq() + ";";
        a aVar = new a();
        h.a(aVar);
        str = (((str + "sdcard " + aVar.Le + ";") + "simNum " + (!h.iE() ? 1 : 2) + ";") + "baseband " + SystemProperties.get("gsm.version.baseband", "") + ";") + "inversion " + Build.DISPLAY + ";";
        str2 = new nc("NetInterfaceManager").getString("upload_config_des", null);
        return (str2 == null || str2.length() == 0) ? str : str + str2;
    }

    public static boolean y(Context context) {
        py b = TMServiceFactory.getSystemInfoService().b(context.getPackageName(), 1);
        return b != null && b.hA();
    }
}
