package tmsdkobf;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import com.qq.taf.jce.JceStruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.i;
import tmsdk.common.utils.n;

public class lg {

    static class a {
        public String go = null;
        public String gp = null;
        int level = -1;
        public int yj = 0;
        public int yk = -1;
        public int yl = 3;
        int ym = -1;
        int yn = 0;

        a() {
        }
    }

    private static int a(ScanResult scanResult) {
        return (scanResult == null || scanResult.capabilities == null) ? -1 : !scanResult.capabilities.contains("WEP") ? !scanResult.capabilities.contains("PSK") ? !scanResult.capabilities.contains("EAP") ? 0 : 3 : 2 : 1;
    }

    private static String a(InputStream inputStream) {
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
        return str;
    }

    private static String a(String str, HttpURLConnection httpURLConnection) {
        String str2 = null;
        InputStream inputStream = null;
        try {
            if (!new URL(str).getHost().equals(httpURLConnection.getURL().getHost())) {
                str2 = httpURLConnection.getURL().toExternalForm();
            }
            if (str2 == null && httpURLConnection.getHeaderField("Location") != null) {
                str2 = httpURLConnection.getHeaderField("Location");
            }
            if (str2 == null && httpURLConnection.getHeaderField("Refresh") != null) {
                String[] split = httpURLConnection.getHeaderField("Refresh").split(";");
                if (split.length == 2) {
                    str2 = split[1].trim();
                }
            }
            if (str2 == null) {
                inputStream = httpURLConnection.getInputStream();
                if (inputStream != null) {
                    str2 = a(inputStream);
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                }
            }
        }
        return str2;
    }

    static synchronized void a(a aVar) {
        synchronized (lg.class) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("maincode", String.valueOf(5));
                jSONObject.put("time", String.valueOf(System.currentTimeMillis()));
                jSONObject.put("bssid", aVar.gp);
                jSONObject.put("ssid", aVar.go);
                jSONObject.put("reportWifiType", String.valueOf(aVar.yj));
                jSONObject.put("reportSecurityType", String.valueOf(aVar.yk));
                jSONObject.put("subType", String.valueOf(aVar.yl));
                jSONObject.put("wifiGradeLevel", String.valueOf(aVar.ym));
                jSONObject.put("remark", String.valueOf("http://tools.3g.qq.com/wifi/cw.html"));
                jSONObject.put("sessionkey", String.valueOf(-1));
                jSONObject.put("connectsource", String.valueOf(4));
                jSONObject.put("wifiType", String.valueOf(aVar.yn));
                la.a(jSONObject.toString(), getPath(), 33);
            } catch (Throwable th) {
            }
        }
    }

    static int aK(int i) {
        WifiInfo connectionInfo = ((WifiManager) TMSDKContext.getApplicaionContext().getSystemService("wifi")).getConnectionInfo();
        return connectionInfo == null ? -1 : WifiManager.calculateSignalLevel(connectionInfo.getRssi(), i);
    }

    static int aL(int i) {
        if (((WifiManager) TMSDKContext.getApplicaionContext().getApplicationContext().getSystemService("wifi")) != null) {
            try {
                return WifiManager.calculateSignalLevel(i, 100) + 1;
            } catch (Throwable th) {
            }
        }
        return -1;
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
                    }
                } else {
                    stringBuilder.append(readLine);
                }
            } catch (IOException e2) {
                try {
                    bufferedReader.close();
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    bufferedReader.close();
                } catch (IOException e4) {
                }
                throw th;
            }
        }
        return stringBuilder.toString();
    }

    public static void eq() {
        try {
            ArrayList bD = la.bD(getPath());
            if (bD != null && !bD.isEmpty()) {
                JceStruct aoVar = new ao(33, new ArrayList());
                Iterator it = bD.iterator();
                while (it.hasNext()) {
                    JSONObject jSONObject = new JSONObject((String) it.next());
                    ap apVar = new ap(new HashMap());
                    apVar.bG.put(Integer.valueOf(1), jSONObject.getString("maincode"));
                    apVar.bG.put(Integer.valueOf(2), jSONObject.getString("time"));
                    apVar.bG.put(Integer.valueOf(3), jSONObject.getString("bssid"));
                    apVar.bG.put(Integer.valueOf(4), jSONObject.getString("ssid"));
                    apVar.bG.put(Integer.valueOf(5), jSONObject.getString("reportWifiType"));
                    apVar.bG.put(Integer.valueOf(6), jSONObject.getString("reportSecurityType"));
                    apVar.bG.put(Integer.valueOf(7), jSONObject.getString("subType"));
                    apVar.bG.put(Integer.valueOf(8), jSONObject.getString("wifiGradeLevel"));
                    apVar.bG.put(Integer.valueOf(9), jSONObject.getString("remark"));
                    apVar.bG.put(Integer.valueOf(13), jSONObject.getString("sessionkey"));
                    apVar.bG.put(Integer.valueOf(14), jSONObject.getString("connectsource"));
                    apVar.bG.put(Integer.valueOf(15), jSONObject.getString("wifiType"));
                    aoVar.bD.add(apVar);
                }
                ob bK = im.bK();
                if (aoVar.bD.size() > 0 && bK != null) {
                    bK.a(4060, aoVar, null, 0, new jy() {
                        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                            if (i3 == 0 && i4 == 0) {
                                la.bF(lg.getPath());
                            }
                        }
                    });
                }
            }
        } catch (Throwable th) {
        }
    }

    public static synchronized void ev() {
        synchronized (lg.class) {
            try {
                final a ew = ew();
                if (ew != null) {
                    JceStruct aoVar = new ao(33, new ArrayList());
                    ap apVar = new ap(new HashMap());
                    apVar.bG.put(Integer.valueOf(1), String.valueOf(5));
                    apVar.bG.put(Integer.valueOf(2), String.valueOf(System.currentTimeMillis()));
                    apVar.bG.put(Integer.valueOf(3), ew.gp);
                    apVar.bG.put(Integer.valueOf(4), ew.go);
                    apVar.bG.put(Integer.valueOf(5), String.valueOf(ew.yj));
                    apVar.bG.put(Integer.valueOf(6), String.valueOf(ew.yk));
                    apVar.bG.put(Integer.valueOf(7), String.valueOf(ew.yl));
                    apVar.bG.put(Integer.valueOf(8), String.valueOf(ew.ym));
                    apVar.bG.put(Integer.valueOf(9), String.valueOf("Meri"));
                    apVar.bG.put(Integer.valueOf(13), String.valueOf(-1));
                    apVar.bG.put(Integer.valueOf(14), String.valueOf(4));
                    apVar.bG.put(Integer.valueOf(15), String.valueOf(ew.yn));
                    aoVar.bD.add(apVar);
                    ob bK = im.bK();
                    if (aoVar.bD.size() > 0 && bK != null) {
                        bK.a(4060, aoVar, null, 0, new jy() {
                            public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                                if (i3 != 0 || i4 != 0) {
                                    lg.a(ew);
                                }
                            }
                        });
                    }
                } else {
                    return;
                }
            } catch (Throwable th) {
            }
        }
    }

    static a ew() {
        if (!i.K(TMSDKContext.getApplicaionContext())) {
            return null;
        }
        WifiManager wifiManager = (WifiManager) TMSDKContext.getApplicaionContext().getSystemService("wifi");
        WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        Object bssid = connectionInfo.getBSSID();
        Object ssid = connectionInfo.getSSID();
        if (TextUtils.isEmpty(ssid) || TextUtils.isEmpty(bssid)) {
            return null;
        }
        int i = -1;
        int ex = ex();
        int i2 = -1;
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults != null) {
            for (ScanResult scanResult : scanResults) {
                if (connectionInfo.getBSSID().compareTo(scanResult.BSSID) == 0) {
                    i = a(scanResult);
                    i2 = aK(scanResult.level) + 1;
                }
            }
        }
        int aL = aL(i2);
        a aVar = new a();
        aVar.gp = bssid;
        aVar.go = ssid;
        aVar.yj = 0;
        aVar.yk = i;
        aVar.yl = ex;
        aVar.ym = aL;
        aVar.yn = 0;
        return aVar;
    }

    /* JADX WARNING: Missing block: B:23:0x0086, code:
            if (r10.equals("Meri") != false) goto L_0x005d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int ex() {
        String str = null;
        Object obj = null;
        HttpURLConnection httpURLConnection = null;
        try {
            long currentTimeMillis = System.currentTimeMillis();
            httpURLConnection = (HttpURLConnection) new URL("http://tools.3g.qq.com/wifi/cw.html").openConnection();
            if (n.iX() < 8) {
                System.setProperty("http.keepAlive", "false");
            }
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestProperty("Pragma", "no-cache");
            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(30000);
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == SmsCheckResult.ESCT_200 || (responseCode >= SmsCheckResult.ESCT_301 && responseCode <= SmsCheckResult.ESCT_305)) {
                String headerField = httpURLConnection.getHeaderField("Meri");
                if (headerField != null) {
                }
                str = a("http://tools.3g.qq.com/wifi/cw.html", httpURLConnection);
                obj = 1;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (obj == null || str == null) {
                return (obj != null && str == null) ? 4 : 3;
            } else {
                return 1;
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public static String getPath() {
        return TMSDKContext.getApplicaionContext().getFilesDir().getAbsolutePath() + File.separator + "d_" + String.valueOf(33);
    }
}
