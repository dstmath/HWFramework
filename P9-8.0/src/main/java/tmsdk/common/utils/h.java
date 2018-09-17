package tmsdk.common.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.List;
import tmsdkobf.mb;

public class h {
    public static String G(Context context) {
        String str = "";
        try {
            str = H(context);
            mb.n("MacUtil", "getMacOld, mac: " + str);
            if (!TextUtils.isEmpty(str) && !"02:00:00:00:00:00".equals(str)) {
                return str;
            }
            String cD = cD("wifi.interface");
            mb.n("MacUtil", "interfaceName: " + cD);
            if (TextUtils.isEmpty(cD)) {
                cD = "wlan0";
            }
            str = cB(cD);
            mb.n("MacUtil", "getMacByAPI, mac: " + str);
            if (!TextUtils.isEmpty(str) && !"02:00:00:00:00:00".equals(str)) {
                return str;
            }
            str = cC(cD);
            mb.n("MacUtil", "getMacFromFile, mac: " + str);
            return str;
        } catch (Throwable th) {
            mb.b("MacUtil", "getMac: " + th, th);
            return str;
        }
    }

    private static String H(Context context) {
        String str = "";
        try {
            WifiInfo connectionInfo = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo();
            if (connectionInfo != null) {
                str = connectionInfo.getMacAddress();
            }
        } catch (Throwable th) {
            mb.b("MacUtil", "getMac exception: " + th, th);
        }
        return str == null ? "" : str;
    }

    private static String cB(String str) {
        String str2 = "";
        try {
            byte[] hardwareAddress = NetworkInterface.getByName(str).getHardwareAddress();
            if (hardwareAddress == null) {
                return str2;
            }
            StringBuilder stringBuilder = new StringBuilder();
            byte[] bArr = hardwareAddress;
            int length = hardwareAddress.length;
            for (int i = 0; i < length; i++) {
                stringBuilder.append(String.format("%02x:", new Object[]{Byte.valueOf(hardwareAddress[i])}));
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            str2 = stringBuilder.toString();
            return str2;
        } catch (Throwable th) {
            mb.b("MacUtil", "getMacByAPI: " + th, th);
        }
    }

    private static String cC(String str) {
        String str2 = "";
        try {
            List g = g(new File(String.format("/sys/class/net/%s/address", new Object[]{str})));
            if (g == null || g.size() != 1) {
                return str2;
            }
            str2 = (String) g.get(0);
            return !TextUtils.isEmpty(str2) ? str2.trim() : str2;
        } catch (Throwable th) {
            mb.b("MacUtil", "getMacFromFile: " + th, th);
            return str2;
        }
    }

    public static String cD(String str) {
        String str2 = "";
        try {
            Method method = Class.forName("android.os.SystemProperties").getMethod("get", new Class[]{String.class});
            method.setAccessible(true);
            str2 = (String) method.invoke(null, new Object[]{str});
            return str2 != null ? str2 : "";
        } catch (Throwable th) {
            mb.s("MacUtil", " getSysPropByReflect: " + th);
            return str2;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a A:{SYNTHETIC, Splitter: B:40:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006e A:{Catch:{ Throwable -> 0x0054 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050 A:{SYNTHETIC, Splitter: B:29:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007a A:{SYNTHETIC, Splitter: B:48:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0083 A:{SYNTHETIC, Splitter: B:52:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0087 A:{Catch:{ Throwable -> 0x007e }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a A:{SYNTHETIC, Splitter: B:40:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006e A:{Catch:{ Throwable -> 0x0054 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050 A:{SYNTHETIC, Splitter: B:29:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007a A:{SYNTHETIC, Splitter: B:48:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0083 A:{SYNTHETIC, Splitter: B:52:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0087 A:{Catch:{ Throwable -> 0x007e }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a A:{SYNTHETIC, Splitter: B:40:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006e A:{Catch:{ Throwable -> 0x0054 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050 A:{SYNTHETIC, Splitter: B:29:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007a A:{SYNTHETIC, Splitter: B:48:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0083 A:{SYNTHETIC, Splitter: B:52:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0087 A:{Catch:{ Throwable -> 0x007e }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a A:{SYNTHETIC, Splitter: B:40:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006e A:{Catch:{ Throwable -> 0x0054 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050 A:{SYNTHETIC, Splitter: B:29:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007a A:{SYNTHETIC, Splitter: B:48:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0083 A:{SYNTHETIC, Splitter: B:52:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0087 A:{Catch:{ Throwable -> 0x007e }} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x006a A:{SYNTHETIC, Splitter: B:40:0x006a} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x006e A:{Catch:{ Throwable -> 0x0054 }} */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0050 A:{SYNTHETIC, Splitter: B:29:0x0050} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x007a A:{SYNTHETIC, Splitter: B:48:0x007a} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0083 A:{SYNTHETIC, Splitter: B:52:0x0083} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0087 A:{Catch:{ Throwable -> 0x007e }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> g(File file) {
        Throwable th;
        InputStream inputStream;
        Throwable th2;
        Reader reader;
        List<String> arrayList = new ArrayList();
        FileInputStream inputStream2 = null;
        InputStreamReader reader2 = null;
        BufferedReader bufferedReader = null;
        try {
            Reader inputStreamReader;
            InputStream fileInputStream = new FileInputStream(file);
            try {
                try {
                    inputStreamReader = new InputStreamReader(fileInputStream);
                } catch (Throwable th3) {
                    th2 = th3;
                    inputStream2 = fileInputStream;
                    if (inputStream2 != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (bufferedReader != null) {
                    }
                    throw th2;
                }
            } catch (Throwable th4) {
                th2 = th4;
                inputStream2 = fileInputStream;
                if (inputStream2 != null) {
                }
                if (reader2 != null) {
                }
                if (bufferedReader != null) {
                }
                throw th2;
            }
            try {
                try {
                    BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader);
                    while (true) {
                        try {
                            String readLine = bufferedReader2.readLine();
                            if (readLine == null) {
                                break;
                            }
                            arrayList.add(readLine);
                        } catch (Throwable th5) {
                            th2 = th5;
                            bufferedReader = bufferedReader2;
                            reader2 = inputStreamReader;
                            inputStream2 = fileInputStream;
                            if (inputStream2 != null) {
                                try {
                                    inputStream2.close();
                                } catch (Throwable th6) {
                                    th6.printStackTrace();
                                }
                            }
                            if (reader2 != null) {
                                reader2.close();
                            }
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                            throw th2;
                        }
                    }
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (Throwable th7) {
                            th7.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (bufferedReader2 != null) {
                        bufferedReader2.close();
                    }
                    bufferedReader = bufferedReader2;
                    reader2 = inputStreamReader;
                    inputStream2 = fileInputStream;
                } catch (Throwable th8) {
                    th2 = th8;
                    reader2 = inputStreamReader;
                    inputStream2 = fileInputStream;
                    if (inputStream2 != null) {
                    }
                    if (reader2 != null) {
                    }
                    if (bufferedReader != null) {
                    }
                    throw th2;
                }
            } catch (Throwable th9) {
                th2 = th9;
                reader2 = inputStreamReader;
                inputStream2 = fileInputStream;
                if (inputStream2 != null) {
                }
                if (reader2 != null) {
                }
                if (bufferedReader != null) {
                }
                throw th2;
            }
        } catch (Throwable th10) {
            th7 = th10;
            mb.b("MacUtil", "readLines：" + th7, th7);
            if (inputStream2 != null) {
            }
            if (reader2 != null) {
            }
            if (bufferedReader != null) {
            }
            return arrayList;
        }
        return arrayList;
    }
}
