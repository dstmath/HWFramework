package tmsdkobf;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.util.List;

public class fy {
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0072 A:{SYNTHETIC, Splitter: B:38:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0069 A:{SYNTHETIC, Splitter: B:34:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x007c A:{SYNTHETIC, Splitter: B:44:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0085 A:{SYNTHETIC, Splitter: B:48:0x0085} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0072 A:{SYNTHETIC, Splitter: B:38:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0069 A:{SYNTHETIC, Splitter: B:34:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x007c A:{SYNTHETIC, Splitter: B:44:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0085 A:{SYNTHETIC, Splitter: B:48:0x0085} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x0072 A:{SYNTHETIC, Splitter: B:38:0x0072} */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0069 A:{SYNTHETIC, Splitter: B:34:0x0069} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x007c A:{SYNTHETIC, Splitter: B:44:0x007c} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0085 A:{SYNTHETIC, Splitter: B:48:0x0085} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int W(String str) {
        Throwable th;
        Reader reader;
        Throwable th2;
        int i = -1;
        if (!TextUtils.isEmpty(str)) {
            InputStreamReader reader2 = null;
            BufferedReader bufferedReader = null;
            try {
                Process exec = Runtime.getRuntime().exec("ps");
                exec.waitFor();
                Reader inputStreamReader = new InputStreamReader(exec.getInputStream());
                try {
                    try {
                        BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader);
                        while (true) {
                            try {
                                Object readLine = bufferedReader2.readLine();
                                if (!TextUtils.isEmpty(readLine)) {
                                    String[] split = readLine.split("[\\s]+");
                                    if (split.length == 9 && split[8].equals(str)) {
                                        i = Integer.parseInt(split[1]);
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            } catch (Throwable th3) {
                                th2 = th3;
                                bufferedReader = bufferedReader2;
                                reader2 = inputStreamReader;
                                if (reader2 != null) {
                                }
                                if (bufferedReader != null) {
                                }
                                throw th2;
                            }
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (bufferedReader2 != null) {
                            try {
                                bufferedReader2.close();
                            } catch (Throwable th4) {
                                th4.printStackTrace();
                            }
                        }
                    } catch (Throwable th5) {
                        th2 = th5;
                        reader2 = inputStreamReader;
                        if (reader2 != null) {
                        }
                        if (bufferedReader != null) {
                        }
                        throw th2;
                    }
                } catch (Throwable th6) {
                    th2 = th6;
                    reader2 = inputStreamReader;
                    if (reader2 != null) {
                    }
                    if (bufferedReader != null) {
                    }
                    throw th2;
                }
            } catch (Throwable th7) {
                th4 = th7;
                th4.printStackTrace();
                if (reader2 != null) {
                    reader2.close();
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th42) {
                        th42.printStackTrace();
                    }
                }
                return i;
            }
        }
        return i;
    }

    public static PackageInfo a(Context context, String str, int i) {
        PackageInfo packageInfo = null;
        try {
            return context.getPackageManager().getPackageInfo(str, i);
        } catch (Throwable th) {
            return packageInfo;
        }
    }

    public static boolean a(Context context, String str) {
        boolean z = false;
        if (context == null || str == null) {
            return false;
        }
        try {
            List<RunningAppProcessInfo> runningAppProcesses = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            if (runningAppProcesses == null || runningAppProcesses.size() <= 5) {
                kt.saveActionData(1320010);
            }
            if (runningAppProcesses != null) {
                for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
                    if (str.equalsIgnoreCase(runningAppProcessInfo.processName)) {
                        z = true;
                        break;
                    }
                }
            }
            if (!z) {
                z = W(str) > 0;
            }
            if (z) {
                return z;
            }
            for (RunningServiceInfo runningServiceInfo : ((ActivityManager) context.getSystemService("activity")).getRunningServices(Integer.MAX_VALUE)) {
                if (runningServiceInfo != null && str.equalsIgnoreCase(runningServiceInfo.process)) {
                    return true;
                }
            }
            return z;
        } catch (Throwable th) {
            return false;
        }
    }

    public static boolean a(Context context, String str, String str2) {
        return context.getPackageManager().getComponentEnabledSetting(new ComponentName(str, str2)) == 2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:6:0x001c A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean a(String str, String str2) {
        if (str2.equals("0")) {
            return true;
        }
        if (!str2.startsWith(">=")) {
            return str2.equalsIgnoreCase(str);
        } else {
            String[] split = str.split("[\\._]");
            String[] split2 = str2.substring(2).split("[\\._]");
            if (split.length >= 2 && split2.length >= 3) {
                int intValue = Integer.valueOf(split[0]).intValue();
                int intValue2 = Integer.valueOf(split[1]).intValue();
                int i = 0;
                if (split.length >= 3) {
                    i = Integer.valueOf(split[2]).intValue();
                }
                int intValue3 = Integer.valueOf(split2[0]).intValue();
                int intValue4 = Integer.valueOf(split2[1]).intValue();
                int intValue5 = Integer.valueOf(split2[2]).intValue();
                if (intValue > intValue3) {
                    return true;
                }
                if (intValue == intValue3) {
                    if (intValue2 > intValue4) {
                        return true;
                    }
                    if (intValue2 == intValue4 && i >= intValue5) {
                        return true;
                    }
                }
            }
        }
    }

    public static boolean c(Context context, ft ftVar) {
        if (ftVar.y() == null) {
            return false;
        }
        try {
            Signature[] signatureArr = a(context, ftVar.y(), 64).signatures;
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
            return stringBuilder.toString().equalsIgnoreCase(ftVar.z());
        } catch (Exception e) {
            return false;
        }
    }
}
