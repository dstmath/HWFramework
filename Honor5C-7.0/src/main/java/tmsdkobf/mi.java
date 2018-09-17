package tmsdkobf;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.List;
import tmsdk.common.TMSDKContext;

/* compiled from: Unknown */
public class mi {
    public static void bA(int i) {
        if (fw.w().getStartCount() != 0 && eS()) {
            fw.w().d(System.currentTimeMillis());
            ((lq) fe.ad(4)).a(new Runnable() {
                public void run() {
                    try {
                        boolean k = mi.k(TMSDKContext.getApplicaionContext());
                        boolean l = mi.l(TMSDKContext.getApplicaionContext());
                        if (!k) {
                            ma.bx(1320009);
                        } else if (l) {
                            ma.bx(29985);
                        } else {
                            ma.bx(29986);
                        }
                        if (k && !l) {
                            mi.m(TMSDKContext.getApplicaionContext());
                            Thread.sleep(15000);
                            if (mi.l(TMSDKContext.getApplicaionContext())) {
                                ma.bx(29984);
                                int startCount = fw.w().getStartCount();
                                if (startCount > 0) {
                                    fw.w().ai(startCount - 1);
                                    return;
                                }
                                return;
                            }
                            ma.bx(29983);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (Throwable th) {
                    }
                }
            }, "checkStartTMSecure");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int ct(String str) {
        InputStreamReader inputStreamReader;
        BufferedReader bufferedReader;
        InputStreamReader inputStreamReader2;
        Throwable th;
        BufferedReader bufferedReader2 = null;
        if (!TextUtils.isEmpty(str)) {
            try {
                Process exec = Runtime.getRuntime().exec("ps");
                exec.waitFor();
                inputStreamReader = new InputStreamReader(exec.getInputStream());
            } catch (Throwable th2) {
                th = th2;
                inputStreamReader = null;
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (Throwable th3) {
                        throw th;
                    }
                }
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                throw th;
            }
            try {
                bufferedReader = new BufferedReader(inputStreamReader);
                while (true) {
                    try {
                        Object readLine = bufferedReader.readLine();
                        if (!TextUtils.isEmpty(readLine)) {
                            String[] split = readLine.split("[\\s]+");
                            if (split.length == 9 && split[8].equals(str)) {
                                break;
                            }
                        } else {
                            break;
                        }
                        if (inputStreamReader != null) {
                            try {
                                inputStreamReader.close();
                            } catch (Throwable th4) {
                            }
                        }
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                        return r1;
                    } catch (Throwable th5) {
                        Throwable th6 = th5;
                        bufferedReader2 = bufferedReader;
                        th = th6;
                    }
                }
                int i = -1;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                return i;
            } catch (Throwable th7) {
                th = th7;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (bufferedReader2 != null) {
                    bufferedReader2.close();
                }
                throw th;
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static boolean eS() {
        boolean z;
        boolean z2 = true;
        long currentTimeMillis = System.currentTimeMillis();
        long M = fw.w().M();
        long N = fw.w().N();
        if (N != 0) {
            if (currentTimeMillis / 1000 >= N) {
                z = false;
                if (z) {
                    return false;
                }
                if (!(currentTimeMillis > M)) {
                }
                z2 = false;
                return z2;
            }
        }
        z = true;
        if (z) {
            return false;
        }
        if (currentTimeMillis > M) {
        }
        if (currentTimeMillis > M) {
            if (currentTimeMillis - M >= 86400000) {
            }
        }
        z2 = false;
        return z2;
    }

    public static boolean k(Context context) {
        try {
            Signature[] signatureArr = context.getPackageManager().getPackageInfo(jw.uF, 64).signatures;
            MessageDigest instance = MessageDigest.getInstance("MD5");
            if (signatureArr != null && signatureArr.length > 0) {
                instance.update(signatureArr[0].toByteArray());
            }
            byte[] digest = instance.digest();
            char[] cArr = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            StringBuilder stringBuilder = new StringBuilder(digest.length * 2);
            for (int i = 0; i < digest.length; i++) {
                stringBuilder.append(cArr[(digest[i] & 240) >>> 4]);
                stringBuilder.append(cArr[digest[i] & 15]);
            }
            return "00B1208638DE0FCD3E920886D658DAF6".equalsIgnoreCase(stringBuilder.toString()) || "7CC749CFC0FB5677E6ABA342EDBDBA5A".equalsIgnoreCase(stringBuilder.toString());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean l(Context context) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        try {
            boolean z2;
            List<RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            if (runningAppProcesses != null && runningAppProcesses.size() > 5) {
                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (jw.uF.equalsIgnoreCase(runningAppProcessInfo.processName)) {
                        z2 = true;
                        break;
                    }
                }
            }
            ma.bx(1320010);
            if (ct(jw.uF) > 0) {
                z = true;
            }
            z2 = z;
            return z2;
        } catch (Exception e) {
            ma.bx(1320010);
            return ct(jw.uF) > 0;
        }
    }

    public static void m(Context context) {
        PackageInfo packageInfo;
        na.s("cccccc", "startByActivity:");
        try {
            packageInfo = context.getPackageManager().getPackageInfo(jw.uF, 16384);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            packageInfo = null;
        }
        if (packageInfo != null && packageInfo.versionCode >= 1066) {
            String str = "EP_Secure_SDK_" + TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SOFTVERSION);
            String str2 = "{'dest_view':65537,'show_id':'show_001','show_channel':'" + TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL) + "'}";
            na.s("cccccc", "jumpParams:" + str2 + "   appToken:" + str);
            try {
                Intent launchIntentForPackage = context.getPackageManager().getLaunchIntentForPackage(jw.uF);
                Bundle bundle = new Bundle();
                bundle.putString("platform_id", str);
                bundle.putString("launch_param", str2);
                launchIntentForPackage.putExtras(bundle);
                launchIntentForPackage.setFlags(402653184);
                context.startActivity(launchIntentForPackage);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }
}
