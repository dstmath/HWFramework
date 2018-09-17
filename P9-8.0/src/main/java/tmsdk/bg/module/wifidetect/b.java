package tmsdk.bg.module.wifidetect;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.qq.taf.jce.JceStruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tmsdk.bg.creator.BaseManagerB;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.f;
import tmsdk.common.utils.i;
import tmsdk.common.utils.n;
import tmsdk.common.utils.q;
import tmsdk.common.utils.t;
import tmsdkobf.db;
import tmsdkobf.dc;
import tmsdkobf.dd;
import tmsdkobf.im;
import tmsdkobf.jy;
import tmsdkobf.kr;
import tmsdkobf.kt;
import tmsdkobf.ky;
import tmsdkobf.kz;
import tmsdkobf.lf;
import tmsdkobf.lg;
import tmsdkobf.ob;

class b extends BaseManagerB {
    private Context mContext;
    private Handler vW;
    private WifiManager wR;
    private ob wS;

    b() {
    }

    private String a(InputStream inputStream) {
        String str = null;
        CharSequence b = b(inputStream);
        f.d("WifiDetectManager", "parsePage-pageContent:[" + b + "]");
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
        f.d("WifiDetectManager", "parsePage-location:[" + str + "]");
        return str;
    }

    private String a(String str, HttpURLConnection httpURLConnection) {
        String str2 = null;
        InputStream inputStream = null;
        try {
            String host = new URL(str).getHost();
            String host2 = httpURLConnection.getURL().getHost();
            f.d("WifiDetectManager", "urlHost:[" + host + "]httpHost:[" + host2 + "]");
            if (!host.equals(host2)) {
                str2 = httpURLConnection.getURL().toExternalForm();
            }
            if (str2 == null && httpURLConnection.getHeaderField("Location") != null) {
                str2 = httpURLConnection.getHeaderField("Location");
                f.d("WifiDetectManager", "111location:[" + str2 + "]");
            }
            if (str2 == null) {
                if (httpURLConnection.getHeaderField("Refresh") != null) {
                    String[] split = httpURLConnection.getHeaderField("Refresh").split(";");
                    if (split.length == 2) {
                        str2 = split[1].trim();
                    }
                    f.d("WifiDetectManager", "222location:[" + str2 + "]");
                }
            }
            if (str2 == null) {
                inputStream = httpURLConnection.getInputStream();
                if (inputStream != null) {
                    str2 = a(inputStream);
                }
                f.d("WifiDetectManager", "333location:[" + str2 + "]");
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
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
        }
        return str2;
    }

    private String b(InputStream inputStream) {
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

    private void m(String str, String str2) {
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            Context currentContext = TMSDKContext.getCurrentContext();
            if (currentContext == null) {
                currentContext = this.mContext;
            }
            inputStream = currentContext.getAssets().open(str, 1);
            fileOutputStream = currentContext.openFileOutput(str2, 0);
            byte[] bArr = new byte[8192];
            while (true) {
                int read = inputStream.read(bArr);
                if (read <= 0) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
            f.d("WifiDetectManager", "IOException:[" + e3 + "]");
            e3.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                }
            }
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e7) {
                }
            }
        }
    }

    public int detectARP(String str) {
        boolean z = false;
        kt.aE(29978);
        f.d("WifiDetectManager", "[Beg]detectARP-binaryName:[" + str + "]");
        if (q.cK(str)) {
            f.d("WifiDetectManager", "[End]detectARP 参数不对");
            return -2;
        }
        int i = 261;
        f.d("WifiDetectManager", "[Beg]ScriptHelper.acquireRoot");
        if (ScriptHelper.acquireRoot() == 0) {
            z = true;
        }
        f.d("WifiDetectManager", "[End]ScriptHelper.acquireRoot:[" + z + "]");
        if (z) {
            final a dw = a.dw();
            File file = new File(this.mContext.getFilesDir(), str);
            if (!file.exists()) {
                f.h("WifiDetectManager", "从包的asset中copy[" + str + "]to[" + file.getAbsolutePath() + "]");
                m(str, str);
            }
            final String absolutePath = file.getAbsolutePath();
            if (file.exists()) {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        dw.bp(absolutePath);
                    }
                });
                thread.setName("nativeArp");
                thread.start();
                i = dw.dx();
            } else {
                f.h("WifiDetectManager", "binaryFile not exist:[" + absolutePath + "]");
                return -3;
            }
        }
        f.d("WifiDetectManager", "[End]detectARP-nRetState:[" + i + "]");
        if (i == 262) {
            kt.e(1320068, "ARP_FAKE");
        }
        return i;
    }

    public int detectDnsAndPhishing(IWifiDetectListener iWifiDetectListener, long j) {
        kt.aE(29979);
        if (i.K(this.mContext)) {
            f.d("WifiDetectManager", "[Beg]detectDnsAndPhishinglistener:[" + iWifiDetectListener + "]");
            f.d("WifiDetectManager", "[GUID] " + this.wS.b());
            WifiInfo connectionInfo = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo();
            if (connectionInfo == null) {
                return -2;
            }
            String ssid = connectionInfo.getSSID();
            String bssid = connectionInfo.getBSSID();
            f.d("WifiDetectManager", "ssid:[" + ssid + "]bssid:[" + bssid + "]");
            if (q.cK(ssid) || q.cK(bssid)) {
                f.d("WifiDetectManager", "[End]detectDnsAndPhishing 参数不对");
                return -3;
            }
            String str = null;
            String str2 = null;
            try {
                DhcpInfo dhcpInfo = this.wR.getDhcpInfo();
                if (dhcpInfo != null) {
                    str = t.I((long) dhcpInfo.dns1);
                    str2 = t.I((long) dhcpInfo.dns2);
                }
            } catch (Exception e) {
            }
            f.d("WifiDetectManager", "dns1:[" + str + "]dns2:[" + str2 + "]");
            JceStruct dbVar = new db();
            dbVar.gj = new dd();
            dbVar.gj.go = ssid;
            dbVar.gj.gp = bssid;
            dbVar.gk = new ArrayList();
            if (str != null && str.length() > 0) {
                dbVar.gk.add(str);
            }
            if (str2 != null && str2.length() > 0) {
                dbVar.gk.add(str2);
            }
            final IWifiDetectListener iWifiDetectListener2 = iWifiDetectListener;
            this.wS.a(794, dbVar, new dc(), 0, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    int i5;
                    f.d("WifiDetectManager", "onFinish retCode:[" + i3 + "]" + "dataRetCode:[" + i4 + "]");
                    Message obtainMessage = b.this.vW.obtainMessage(4097);
                    if (i3 == 0 && jceStruct != null && (jceStruct instanceof dc)) {
                        dc dcVar = (dc) jceStruct;
                        if (dcVar.gn == 2) {
                            f.d("WifiDetectManager", "[CLOUND_CHECK_DNS_FAKE]ESafeType.EST_DnsException");
                            kt.e(1320068, "DNS_FAKE");
                            i5 = 18;
                        } else if (dcVar.gn != 3) {
                            f.d("WifiDetectManager", "[CLOUND_CHECK_NO_FAKE]safeType:[" + dcVar.gn + "]");
                            i5 = 17;
                        } else {
                            f.d("WifiDetectManager", "[CLOUND_CHECK_PHISHING_FAKE]ESafeType.EST_Phishing");
                            kt.e(1320068, "PHISHING_FAKE");
                            i5 = 19;
                        }
                    } else {
                        f.d("WifiDetectManager", "[CLOUND_CHECK_NETWORK_ERROR]");
                        i5 = 16;
                    }
                    obtainMessage.arg1 = i5;
                    obtainMessage.obj = iWifiDetectListener2;
                    b.this.vW.sendMessage(obtainMessage);
                }
            }, j);
            kr.dz();
            ky aJ = kz.aJ(90);
            if (aJ != null && aJ.xZ) {
                lf.bG(bssid);
            }
            aJ = kz.aJ(33);
            if (aJ != null && aJ.xZ) {
                lg.ev();
                lg.eq();
            }
            f.d("WifiDetectManager", "[End]detectDnsAndPhishing-ssid:[" + ssid + "]bssid:[" + bssid + "]");
            return 0;
        }
        f.d("WifiDetectManager", "[WifiConnected false]");
        iWifiDetectListener.onResult(-1);
        return -1;
    }

    public int detectSecurity(ScanResult scanResult) {
        kt.aE(29980);
        int i = 256;
        f.d("WifiDetectManager", "[Beg]detectSecurity-AP:[" + scanResult + "]");
        if (scanResult == null || scanResult.capabilities == null) {
            return -2;
        }
        if (scanResult.capabilities.contains("WEP")) {
            i = 257;
        } else if (scanResult.capabilities.contains("PSK")) {
            i = 258;
        } else if (scanResult.capabilities.contains("EAP")) {
            i = 259;
        }
        f.d("WifiDetectManager", "[End]detectSecurity-Ret:[" + i + "]");
        return i;
    }

    public int getSingletonType() {
        return 2;
    }

    /* JADX WARNING: Missing block: B:28:0x0153, code:
            if (r2.equals(r18) != false) goto L_0x00e5;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int l(String str, String str2) {
        kt.aE(29981);
        int i = 2;
        f.d("WifiDetectManager", "[Beg]detectNetworkState-urlApprove:[" + str + "]customHeader:[" + str2 + "]");
        String str3 = null;
        Object obj = null;
        HttpURLConnection httpURLConnection = null;
        try {
            long currentTimeMillis = System.currentTimeMillis();
            f.d("WifiDetectManager", "openConnection");
            httpURLConnection = (HttpURLConnection) new URL(str).openConnection();
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
            f.d("WifiDetectManager", "getResponseCode:[" + responseCode + "]timeMillis:[" + (System.currentTimeMillis() - currentTimeMillis) + "]");
            if (responseCode == SmsCheckResult.ESCT_200 || (responseCode >= SmsCheckResult.ESCT_301 && responseCode <= SmsCheckResult.ESCT_305)) {
                String headerField = httpURLConnection.getHeaderField(str2);
                f.d("WifiDetectManager", "customHeader: " + headerField);
                if (headerField != null) {
                }
                str3 = a(str, httpURLConnection);
                obj = 1;
            }
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            if (obj == null || str3 == null) {
                if (obj != null && str3 == null) {
                    i = 1;
                }
                f.d("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                return i;
            }
            i = 3;
            f.d("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
            return i;
        } catch (IOException e) {
            f.d("WifiDetectManager", "IOException:[" + e + "]");
            e.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public void onCreate(Context context) {
        f.d("WifiDetectManager", "OnCreate-context:[" + context + "]");
        this.mContext = context;
        this.wR = (WifiManager) context.getSystemService("wifi");
        this.wS = im.bK();
        this.vW = new Handler(Looper.getMainLooper()) {
            public void handleMessage(Message message) {
                if (message.what == 4097) {
                    f.d("WifiDetectManager", "onResult-CLOUND_CHECK:[" + message.arg1 + "]");
                    IWifiDetectListener iWifiDetectListener = (IWifiDetectListener) message.obj;
                    if (iWifiDetectListener != null) {
                        iWifiDetectListener.onResult(message.arg1);
                    }
                }
            }
        };
    }
}
