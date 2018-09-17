package tmsdkobf;

import android.content.Context;
import java.io.File;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class mz {
    public static boolean e(Context context, String str) {
        String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_PRE_LIB_PATH);
        if (strFromEnvMap == null) {
            try {
                strFromEnvMap = context.getCacheDir().toString();
                if (strFromEnvMap.endsWith("/")) {
                    strFromEnvMap = strFromEnvMap.substring(0, strFromEnvMap.length() - 2);
                }
                int lastIndexOf = strFromEnvMap.lastIndexOf(47);
                if (lastIndexOf == -1) {
                    strFromEnvMap = "";
                } else {
                    strFromEnvMap = strFromEnvMap.substring(0, lastIndexOf) + "/lib/";
                }
            } catch (Exception e) {
                strFromEnvMap = "/data/data/" + context.getPackageName() + "/lib/";
            }
        }
        if (strFromEnvMap.length() == 0) {
            try {
                System.loadLibrary(str);
            } catch (UnsatisfiedLinkError e2) {
                e2.printStackTrace();
                return false;
            }
        }
        boolean z;
        String str2;
        String str3;
        File file;
        File file2;
        boolean z2;
        if (new File(strFromEnvMap + str).exists()) {
            try {
                System.load(strFromEnvMap + str);
                z = true;
            } catch (UnsatisfiedLinkError e3) {
            }
            if (z || str.endsWith(".so")) {
                str2 = str;
            } else {
                str2 = str + ".so";
                if (new File(strFromEnvMap + str2).exists()) {
                    try {
                        System.load(strFromEnvMap + str2);
                        z = true;
                    } catch (UnsatisfiedLinkError e4) {
                    }
                }
            }
            if (!(z || str2.startsWith("lib"))) {
                str3 = "lib" + str2;
                file = new File(strFromEnvMap + str3);
                if (file.exists()) {
                    File file3 = file;
                    str2 = strFromEnvMap;
                    file2 = file3;
                } else {
                    str2 = strFromEnvMap.replace("/lib/", "/app_p_lib/");
                    file2 = new File(str2 + str3);
                }
                if (file2.exists()) {
                    try {
                        System.load(str2 + str3);
                        z2 = true;
                    } catch (UnsatisfiedLinkError e5) {
                    }
                    if (!z2) {
                        try {
                            System.loadLibrary(str);
                        } catch (UnsatisfiedLinkError e22) {
                            e22.printStackTrace();
                            return false;
                        }
                    }
                }
            }
            z2 = z;
            if (z2) {
                System.loadLibrary(str);
            }
        }
        z = false;
        if (z) {
            str2 = str + ".so";
            if (new File(strFromEnvMap + str2).exists()) {
                System.load(strFromEnvMap + str2);
                z = true;
            }
            str3 = "lib" + str2;
            file = new File(strFromEnvMap + str3);
            if (file.exists()) {
                str2 = strFromEnvMap.replace("/lib/", "/app_p_lib/");
                file2 = new File(str2 + str3);
            } else {
                File file32 = file;
                str2 = strFromEnvMap;
                file2 = file32;
            }
            if (file2.exists()) {
                System.load(str2 + str3);
                z2 = true;
                if (z2) {
                    System.loadLibrary(str);
                }
            }
            z2 = z;
            if (z2) {
                System.loadLibrary(str);
            }
        }
        str2 = str;
        str3 = "lib" + str2;
        file = new File(strFromEnvMap + str3);
        if (file.exists()) {
            File file322 = file;
            str2 = strFromEnvMap;
            file2 = file322;
        } else {
            str2 = strFromEnvMap.replace("/lib/", "/app_p_lib/");
            file2 = new File(str2 + str3);
        }
        if (file2.exists()) {
            System.load(str2 + str3);
            z2 = true;
            if (z2) {
                System.loadLibrary(str);
            }
        }
        z2 = z;
        if (z2) {
            System.loadLibrary(str);
        }
        return true;
    }
}
