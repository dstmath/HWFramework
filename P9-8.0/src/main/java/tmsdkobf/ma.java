package tmsdkobf;

import android.content.Context;
import java.io.File;
import tmsdk.common.TMSDKContext;

public class ma {
    public static boolean f(Context context, String -l_2_R) {
        String strFromEnvMap = TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_LIB_PATH);
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
                System.loadLibrary(-l_2_R);
            } catch (UnsatisfiedLinkError e2) {
                e2.printStackTrace();
                return false;
            }
        }
        String str;
        Object obj = null;
        if (new File(strFromEnvMap + -l_2_R).exists()) {
            try {
                System.load(strFromEnvMap + -l_2_R);
                obj = 1;
            } catch (UnsatisfiedLinkError e3) {
            }
        }
        if (obj == null && !-l_2_R.endsWith(".so")) {
            str = -l_2_R + ".so";
            if (new File(strFromEnvMap + str).exists()) {
                try {
                    System.load(strFromEnvMap + str);
                    obj = 1;
                } catch (UnsatisfiedLinkError e4) {
                }
            }
        } else {
            str = -l_2_R;
        }
        if (obj == null && !str.startsWith("lib")) {
            str = "lib" + str;
            File file = new File(strFromEnvMap + str);
            if (!file.exists()) {
                strFromEnvMap = strFromEnvMap.replace("/lib/", "/app_p_lib/");
                file = new File(strFromEnvMap + str);
            }
            if (file.exists()) {
                try {
                    System.load(strFromEnvMap + str);
                    obj = 1;
                } catch (UnsatisfiedLinkError e5) {
                }
            }
        }
        if (obj == null) {
            try {
                System.loadLibrary(-l_2_R);
            } catch (UnsatisfiedLinkError e6) {
                e6.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
