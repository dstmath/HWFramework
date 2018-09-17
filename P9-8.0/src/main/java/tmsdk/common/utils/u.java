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

public class u {
    public static String Mg;

    public interface a {
        void d(boolean z, boolean z2);
    }

    private static String a(InputStream inputStream) throws WifiApproveException {
        String str = null;
        CharSequence b = b(inputStream);
        String[] strArr = new String[]{"http-equiv\\s*=\\s*[\"']*refresh[\"']*\\s*content\\s*=\\s*[\"']*[^;]*;\\s*url\\s*=\\s*[\"']*([^\"'\\s>]+)", "[^\\w](?:location.href\\s*=|location\\s*=|location.replace\\s*\\()\\s*[\"']*([^\"'>]+)", "<NextURL>([^<]+)", "\\s+action\\s*=\\s*[\"']*([^\"'>]+)[\"'>\\s]*.*submit", "<LoginURL>([^<]+)"};
        int length = strArr.length;
        for (int i = 0; i < length && str == null; i++) {
            Matcher matcher = Pattern.compile(strArr[i], 2).matcher(b);
            while (matcher.find() && str == null) {
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

    private static String a(HttpURLConnection httpURLConnection) throws WifiApproveException {
        String str = null;
        InputStream inputStream = null;
        try {
            if (!new URL("http://tools.3g.qq.com/cw.html").getHost().equals(httpURLConnection.getURL().getHost())) {
                str = httpURLConnection.getURL().toExternalForm();
            }
            if (str == null && httpURLConnection.getHeaderField("Location") != null) {
                str = httpURLConnection.getHeaderField("Location");
            }
            if (str == null && httpURLConnection.getHeaderField("Refresh") != null) {
                String[] split = httpURLConnection.getHeaderField("Refresh").split(";");
                if (split.length == 2) {
                    str = split[1].trim();
                }
            }
            if (str == null) {
                inputStream = httpURLConnection.getInputStream();
                if (inputStream != null) {
                    String a = a(inputStream);
                    if (a != null) {
                        str = a;
                    }
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (IOException e2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (WifiApproveException e4) {
            throw e4;
        } catch (Exception e5) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e7) {
                }
            }
        }
        return str;
    }

    /* JADX WARNING: Missing block: B:26:0x0089, code:
            if (r6.equals("Meri") != false) goto L_0x0058;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String a(a aVar) throws WifiApproveException {
        String str = null;
        boolean z = false;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL("http://tools.3g.qq.com/cw.html").openConnection();
            if (n.iX() < 8) {
                System.setProperty("http.keepAlive", "false");
            }
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Pragma", "no-cache");
            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(5000);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode != SmsCheckResult.ESCT_200 && (responseCode < SmsCheckResult.ESCT_301 || responseCode > SmsCheckResult.ESCT_305)) {
                z = true;
            } else {
                String headerField = httpURLConnection.getHeaderField("Meri");
                if (headerField != null) {
                }
                str = a(httpURLConnection);
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (str != null) {
                Mg = str;
                aVar.d(true, z);
            } else {
                aVar.d(false, z);
            }
        } catch (IOException e) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (null != null) {
                Mg = null;
                aVar.d(true, false);
            } else {
                aVar.d(false, false);
            }
        } catch (WifiApproveException e2) {
            throw e2;
        } catch (Exception e3) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (null != null) {
                Mg = null;
                aVar.d(true, false);
            } else {
                aVar.d(false, false);
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (null != null) {
                Mg = null;
                aVar.d(true, false);
            } else {
                aVar.d(false, false);
            }
        }
        return str;
    }

    public static int aK(int i) {
        if (!jh()) {
            return -1;
        }
        WifiInfo connectionInfo = ((WifiManager) TMSDKContext.getApplicaionContext().getSystemService("wifi")).getConnectionInfo();
        return connectionInfo == null ? -1 : WifiManager.calculateSignalLevel(connectionInfo.getRssi(), i);
    }

    private static String b(InputStream inputStream) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        while (true) {
            try {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    try {
                        bufferedReader.close();
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    stringBuilder.append(readLine);
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                try {
                    bufferedReader.close();
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            } catch (Throwable th) {
                try {
                    bufferedReader.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
                throw th;
            }
        }
        return stringBuilder.toString();
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
            f.e("WifiUtil", "getSSID: " + e);
        }
        return "";
    }

    public static boolean jh() {
        NetworkInfo networkInfo = null;
        try {
            networkInfo = TMServiceFactory.getSystemInfoService().getActiveNetworkInfo();
        } catch (NullPointerException e) {
            f.g("getActiveNetworkInfo", " getActiveNetworkInfo NullPointerException--- \n" + e.getMessage());
        }
        return networkInfo != null && networkInfo.getType() == 1;
    }
}
