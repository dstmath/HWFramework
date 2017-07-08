package tmsdk.common.utils;

import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.exception.WifiApproveException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public class p {
    public static String Lq;

    /* compiled from: Unknown */
    public interface a {
        void b(boolean z, boolean z2);
    }

    private static String a(InputStream inputStream) throws WifiApproveException {
        CharSequence b = b(inputStream);
        String[] strArr = new String[]{"http-equiv\\s*=\\s*[\"']*refresh[\"']*\\s*content\\s*=\\s*[\"']*[^;]*;\\s*url\\s*=\\s*[\"']*([^\"'\\s>]+)", "[^\\w](?:location.href\\s*=|location\\s*=|location.replace\\s*\\()\\s*[\"']*([^\"'>]+)", "<NextURL>([^<]+)", "\\s+action\\s*=\\s*[\"']*([^\"'>]+)[\"'>\\s]*.*submit", "<LoginURL>([^<]+)"};
        int length = strArr.length;
        String str = null;
        for (int i = 0; i < length && str == null; i++) {
            Matcher matcher = Pattern.compile(strArr[i], 2).matcher(b);
            while (matcher.find() && r0 == null) {
                str = matcher.group(matcher.groupCount());
                if (!(str == null || str.trim().toLowerCase().startsWith("http"))) {
                    str = null;
                }
            }
        }
        if (str != null) {
            return str;
        }
        throw new WifiApproveException("0725SSID:" + getSSID() + " page head content: " + b);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String a(HttpURLConnection httpURLConnection) throws WifiApproveException {
        String toExternalForm;
        InputStream inputStream;
        WifiApproveException wifiApproveException;
        Throwable th;
        InputStream inputStream2 = null;
        try {
            toExternalForm = new URL("http://tools.3g.qq.com/cw.html").getHost().equals(httpURLConnection.getURL().getHost()) ? inputStream2 : httpURLConnection.getURL().toExternalForm();
            if (toExternalForm == null) {
                try {
                    if (httpURLConnection.getHeaderField("Location") != null) {
                        toExternalForm = httpURLConnection.getHeaderField("Location");
                    }
                } catch (IOException e) {
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e2) {
                        }
                    }
                    return toExternalForm;
                } catch (WifiApproveException e3) {
                    WifiApproveException wifiApproveException2 = e3;
                    inputStream = inputStream2;
                    wifiApproveException = wifiApproveException2;
                    try {
                        throw wifiApproveException;
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Exception e4) {
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e5) {
                        }
                    }
                    return toExternalForm;
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    inputStream = inputStream2;
                    th = th4;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                    throw th;
                }
            }
            if (toExternalForm == null && httpURLConnection.getHeaderField("Refresh") != null) {
                String[] split = httpURLConnection.getHeaderField("Refresh").split(";");
                if (split.length == 2) {
                    toExternalForm = split[1].trim();
                }
            }
            if (toExternalForm == null) {
                inputStream2 = httpURLConnection.getInputStream();
                if (inputStream2 != null) {
                    String a = a(inputStream2);
                    if (a != null) {
                        toExternalForm = a;
                    }
                }
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e7) {
                }
            }
        } catch (IOException e8) {
            toExternalForm = inputStream2;
            if (inputStream2 != null) {
                inputStream2.close();
            }
            return toExternalForm;
        } catch (WifiApproveException e32) {
            wifiApproveException2 = e32;
            inputStream = inputStream2;
            wifiApproveException = wifiApproveException2;
            throw wifiApproveException;
        } catch (Exception e9) {
            toExternalForm = inputStream2;
            if (inputStream2 != null) {
                inputStream2.close();
            }
            return toExternalForm;
        } catch (Throwable th32) {
            th4 = th32;
            inputStream = inputStream2;
            th = th4;
            if (inputStream != null) {
                inputStream.close();
            }
            throw th;
        }
        return toExternalForm;
    }

    public static String a(a aVar) throws WifiApproveException {
        String a;
        WifiApproveException wifiApproveException;
        Throwable th;
        HttpURLConnection httpURLConnection = null;
        HttpURLConnection httpURLConnection2;
        try {
            httpURLConnection2 = (HttpURLConnection) new URL("http://tools.3g.qq.com/cw.html").openConnection();
            try {
                boolean z;
                if (j.iM() < 8) {
                    System.setProperty("http.keepAlive", "false");
                }
                httpURLConnection2.setUseCaches(false);
                httpURLConnection2.setRequestProperty("Pragma", "no-cache");
                httpURLConnection2.setRequestProperty("Cache-Control", "no-cache");
                httpURLConnection2.setInstanceFollowRedirects(false);
                httpURLConnection2.setRequestMethod("GET");
                httpURLConnection2.setReadTimeout(5000);
                int responseCode = httpURLConnection2.getResponseCode();
                if (responseCode != SmsCheckResult.ESCT_200) {
                    if (responseCode >= SmsCheckResult.ESCT_301) {
                        if (responseCode > SmsCheckResult.ESCT_305) {
                        }
                    }
                    z = true;
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    if (a != null) {
                        Lq = a;
                        aVar.b(true, z);
                    } else {
                        aVar.b(false, z);
                    }
                    return a;
                }
                String headerField = httpURLConnection2.getHeaderField("Meri");
                if (headerField != null) {
                    if (headerField.equals("Meri")) {
                        z = false;
                        if (httpURLConnection2 != null) {
                            httpURLConnection2.disconnect();
                        }
                        if (a != null) {
                            aVar.b(false, z);
                        } else {
                            Lq = a;
                            aVar.b(true, z);
                        }
                        return a;
                    }
                }
                a = a(httpURLConnection2);
                z = false;
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                if (a != null) {
                    Lq = a;
                    aVar.b(true, z);
                } else {
                    aVar.b(false, z);
                }
            } catch (IOException e) {
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                aVar.b(false, false);
                return a;
            } catch (WifiApproveException e2) {
                WifiApproveException wifiApproveException2 = e2;
                httpURLConnection = httpURLConnection2;
                wifiApproveException = wifiApproveException2;
                try {
                    throw wifiApproveException;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e3) {
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                aVar.b(false, false);
                return a;
            } catch (Throwable th3) {
                Throwable th4 = th3;
                httpURLConnection = httpURLConnection2;
                th = th4;
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                aVar.b(false, false);
                throw th;
            }
        } catch (IOException e4) {
            httpURLConnection2 = null;
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
            aVar.b(false, false);
            return a;
        } catch (WifiApproveException e5) {
            wifiApproveException = e5;
            throw wifiApproveException;
        } catch (Exception e6) {
            httpURLConnection2 = null;
            if (httpURLConnection2 != null) {
                httpURLConnection2.disconnect();
            }
            aVar.b(false, false);
            return a;
        }
        return a;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String b(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            String readLine = bufferedReader.readLine();
            if (readLine == null) {
                try {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    stringBuilder.append(readLine);
                } catch (IOException e2) {
                    e2.printStackTrace();
                } catch (Throwable th) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    public static int cD(int i) {
        if (!iX()) {
            return -1;
        }
        WifiInfo connectionInfo = ((WifiManager) TMSDKContext.getApplicaionContext().getSystemService("wifi")).getConnectionInfo();
        return connectionInfo == null ? -1 : WifiManager.calculateSignalLevel(connectionInfo.getRssi(), i);
    }

    public static String getSSID() {
        try {
            WifiManager wifiManager = (WifiManager) TMSDKContext.getApplicaionContext().getSystemService("wifi");
            if (wifiManager != null) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                if (connectionInfo != null) {
                    return connectionInfo.getSSID();
                }
            }
        } catch (Exception e) {
            d.c("WifiUtil", "getSSID: " + e);
        }
        return "";
    }

    public static boolean iX() {
        NetworkInfo activeNetworkInfo;
        try {
            activeNetworkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            d.f("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
            activeNetworkInfo = null;
        }
        return activeNetworkInfo != null && activeNetworkInfo.getType() == 1;
    }
}
