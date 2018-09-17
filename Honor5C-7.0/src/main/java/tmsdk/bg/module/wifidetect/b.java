package tmsdk.bg.module.wifidetect;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.d;
import tmsdk.common.utils.j;
import tmsdk.common.utils.l;
import tmsdk.common.utils.o;
import tmsdkobf.cl;
import tmsdkobf.cm;
import tmsdkobf.cn;
import tmsdkobf.fs;
import tmsdkobf.jq;
import tmsdkobf.lg;
import tmsdkobf.ly;
import tmsdkobf.ma;
import tmsdkobf.pf;

/* compiled from: Unknown */
class b extends BaseManagerB {
    private Context mContext;
    private Handler yO;
    private pf yP;
    private WifiManager zG;

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.wifidetect.b.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ b zH;

        AnonymousClass1(b bVar, Looper looper) {
            this.zH = bVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 4097) {
                d.e("WifiDetectManager", "onResult-CLOUND_CHECK:[" + message.arg1 + "]");
                IWifiDetectListener iWifiDetectListener = (IWifiDetectListener) message.obj;
                if (iWifiDetectListener != null) {
                    iWifiDetectListener.onResult(message.arg1);
                }
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.wifidetect.b.2 */
    class AnonymousClass2 implements lg {
        final /* synthetic */ b zH;
        final /* synthetic */ IWifiDetectListener zI;

        AnonymousClass2(b bVar, IWifiDetectListener iWifiDetectListener) {
            this.zH = bVar;
            this.zI = iWifiDetectListener;
        }

        public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
            int i5;
            d.e("WifiDetectManager", "onFinish retCode:[" + i3 + "]");
            Message obtainMessage = this.zH.yO.obtainMessage(4097);
            if (i3 == 0 && fsVar != null && (fsVar instanceof cm)) {
                cm cmVar = (cm) fsVar;
                if (cmVar.fr == 2) {
                    d.e("WifiDetectManager", "[CLOUND_CHECK_DNS_FAKE]ESafeType.EST_DnsException");
                    ma.d(29968, "DNS_FAKE");
                    i5 = 18;
                } else if (cmVar.fr != 3) {
                    d.e("WifiDetectManager", "[CLOUND_CHECK_NO_FAKE]safeType:[" + cmVar.fr + "]");
                    i5 = 17;
                } else {
                    d.e("WifiDetectManager", "[CLOUND_CHECK_PHISHING_FAKE]ESafeType.EST_Phishing");
                    ma.d(29968, "PHISHING_FAKE");
                    i5 = 19;
                }
            } else {
                d.e("WifiDetectManager", "[CLOUND_CHECK_NETWORK_ERROR]");
                i5 = 16;
            }
            obtainMessage.arg1 = i5;
            obtainMessage.obj = this.zI;
            this.zH.yO.sendMessage(obtainMessage);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdk.bg.module.wifidetect.b.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ b zH;
        final /* synthetic */ a zJ;
        final /* synthetic */ String zK;

        AnonymousClass3(b bVar, a aVar, String str) {
            this.zH = bVar;
            this.zJ = aVar;
            this.zK = str;
        }

        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.zJ.cn(this.zK);
        }
    }

    b() {
    }

    private String a(InputStream inputStream) {
        CharSequence b = b(inputStream);
        d.e("WifiDetectManager", "parsePage-pageContent:[" + b + "]");
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
        d.e("WifiDetectManager", "parsePage-location:[" + str + "]");
        return str;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String a(String str, HttpURLConnection httpURLConnection) {
        String host;
        InputStream inputStream;
        InputStream inputStream2 = null;
        try {
            host = new URL(str).getHost();
            String host2 = httpURLConnection.getURL().getHost();
            d.e("WifiDetectManager", "urlHost:[" + host + "]httpHost:[" + host2 + "]");
            host = host.equals(host2) ? inputStream2 : httpURLConnection.getURL().toExternalForm();
            if (host == null) {
                try {
                    if (httpURLConnection.getHeaderField("Location") != null) {
                        host = httpURLConnection.getHeaderField("Location");
                        d.e("WifiDetectManager", "111location:[" + host + "]");
                    }
                } catch (IOException e) {
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e2) {
                        }
                    }
                    return host;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    inputStream = inputStream2;
                    Throwable th3 = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                    throw th3;
                }
            }
            if (host == null) {
                if (httpURLConnection.getHeaderField("Refresh") != null) {
                    String[] split = httpURLConnection.getHeaderField("Refresh").split(";");
                    if (split.length == 2) {
                        host = split[1].trim();
                    }
                    d.e("WifiDetectManager", "222location:[" + host + "]");
                }
            }
            if (host == null) {
                inputStream2 = httpURLConnection.getInputStream();
                if (inputStream2 != null) {
                    host = a(inputStream2);
                }
                d.e("WifiDetectManager", "333location:[" + host + "]");
            }
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e4) {
                }
            }
        } catch (IOException e5) {
            if (inputStream2 != null) {
                try {
                    inputStream2.close();
                } catch (IOException e22) {
                }
            }
            return host;
        } catch (Throwable th4) {
            th2 = th4;
            inputStream = inputStream2;
            th3 = th2;
            if (inputStream != null) {
                inputStream.close();
            }
            throw th3;
        }
        return host;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String b(InputStream inputStream) {
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

    private void p(String str, String str2) {
        InputStream open;
        IOException e;
        Throwable th;
        FileOutputStream fileOutputStream = null;
        try {
            open = this.mContext.getAssets().open(str, 1);
            try {
                fileOutputStream = this.mContext.openFileOutput(str2, 0);
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = open.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                if (open != null) {
                    try {
                        open.close();
                    } catch (IOException e2) {
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (IOException e4) {
                e = e4;
                try {
                    d.e("WifiDetectManager", "IOException:[" + e + "]");
                    e.printStackTrace();
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e5) {
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e6) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (open != null) {
                        try {
                            open.close();
                        } catch (IOException e7) {
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e8) {
                        }
                    }
                    throw th;
                }
            }
        } catch (IOException e9) {
            e = e9;
            Object obj = fileOutputStream;
            d.e("WifiDetectManager", "IOException:[" + e + "]");
            e.printStackTrace();
            if (open != null) {
                open.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        } catch (Throwable th3) {
            th = th3;
            open = fileOutputStream;
            if (open != null) {
                open.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            throw th;
        }
    }

    public int detectARP(String str) {
        boolean z = false;
        ma.bx(29978);
        d.e("WifiDetectManager", "[Beg]detectARP-binaryName:[" + str + "]");
        if (l.dm(str)) {
            d.e("WifiDetectManager", "[End]detectARP \u53c2\u6570\u4e0d\u5bf9");
            return -2;
        }
        int em;
        d.e("WifiDetectManager", "[Beg]ScriptHelper.acquireRoot");
        if (ScriptHelper.acquireRoot() == 0) {
            z = true;
        }
        d.e("WifiDetectManager", "[End]ScriptHelper.acquireRoot:[" + z + "]");
        if (z) {
            a el = a.el();
            File file = new File(this.mContext.getFilesDir(), str);
            if (!file.exists()) {
                d.g("WifiDetectManager", "\u4ece\u5305\u7684asset\u4e2dcopy[" + str + "]to[" + file.getAbsolutePath() + "]");
                p(str, str);
            }
            String absolutePath = file.getAbsolutePath();
            if (file.exists()) {
                Thread thread = new Thread(new AnonymousClass3(this, el, absolutePath));
                thread.setName("nativeArp");
                thread.start();
                em = el.em();
            } else {
                d.g("WifiDetectManager", "binaryFile not exist:[" + absolutePath + "]");
                return -3;
            }
        }
        em = SmsCheckResult.ESCT_261;
        d.e("WifiDetectManager", "[End]detectARP-nRetState:[" + em + "]");
        if (em == SmsCheckResult.ESCT_262) {
            ma.d(29968, "ARP_FAKE");
        }
        return em;
    }

    public int detectDnsAndPhishing(IWifiDetectListener iWifiDetectListener) {
        String str;
        String B;
        fs clVar;
        String str2 = null;
        ma.bx(29979);
        d.e("WifiDetectManager", "[Beg]detectDnsAndPhishinglistener:[" + iWifiDetectListener + "]");
        WifiInfo connectionInfo = ((WifiManager) this.mContext.getSystemService("wifi")).getConnectionInfo();
        if (connectionInfo == null) {
            return -2;
        }
        String ssid = connectionInfo.getSSID();
        String bssid = connectionInfo.getBSSID();
        d.e("WifiDetectManager", "ssid:[" + ssid + "]bssid:[" + bssid + "]");
        if (l.dm(ssid) || l.dm(bssid)) {
            d.e("WifiDetectManager", "[End]detectDnsAndPhishing \u53c2\u6570\u4e0d\u5bf9");
            return -3;
        }
        try {
            DhcpInfo dhcpInfo = this.zG.getDhcpInfo();
            if (dhcpInfo == null) {
                str = null;
            } else {
                B = o.B((long) dhcpInfo.dns1);
                try {
                    str = o.B((long) dhcpInfo.dns2);
                    str2 = B;
                } catch (Exception e) {
                    str = null;
                    str2 = B;
                    d.e("WifiDetectManager", "dns1:[" + str2 + "]dns2:[" + str + "]");
                    clVar = new cl();
                    clVar.fn = new cn();
                    clVar.fn.fs = ssid;
                    clVar.fn.ft = bssid;
                    clVar.fo = new ArrayList();
                    clVar.fo.add(str2);
                    clVar.fo.add(str);
                    this.yP.a(794, clVar, new cm(), 0, new AnonymousClass2(this, iWifiDetectListener));
                    ly.ep();
                    d.e("WifiDetectManager", "[End]detectDnsAndPhishing-ssid:[" + ssid + "]bssid:[" + bssid + "]");
                    return 0;
                }
            }
        } catch (Exception e2) {
            B = null;
            str = null;
            str2 = B;
            d.e("WifiDetectManager", "dns1:[" + str2 + "]dns2:[" + str + "]");
            clVar = new cl();
            clVar.fn = new cn();
            clVar.fn.fs = ssid;
            clVar.fn.ft = bssid;
            clVar.fo = new ArrayList();
            clVar.fo.add(str2);
            clVar.fo.add(str);
            this.yP.a(794, clVar, new cm(), 0, new AnonymousClass2(this, iWifiDetectListener));
            ly.ep();
            d.e("WifiDetectManager", "[End]detectDnsAndPhishing-ssid:[" + ssid + "]bssid:[" + bssid + "]");
            return 0;
        }
        d.e("WifiDetectManager", "dns1:[" + str2 + "]dns2:[" + str + "]");
        clVar = new cl();
        clVar.fn = new cn();
        clVar.fn.fs = ssid;
        clVar.fn.ft = bssid;
        clVar.fo = new ArrayList();
        if (str2 != null && str2.length() > 0) {
            clVar.fo.add(str2);
        }
        if (str != null && str.length() > 0) {
            clVar.fo.add(str);
        }
        this.yP.a(794, clVar, new cm(), 0, new AnonymousClass2(this, iWifiDetectListener));
        ly.ep();
        d.e("WifiDetectManager", "[End]detectDnsAndPhishing-ssid:[" + ssid + "]bssid:[" + bssid + "]");
        return 0;
    }

    public int detectSecurity(ScanResult scanResult) {
        ma.bx(29980);
        int i = WifiDetectManager.SECURITY_NONE;
        d.e("WifiDetectManager", "[Beg]detectSecurity-AP:[" + scanResult + "]");
        if (scanResult == null || scanResult.capabilities == null) {
            return -2;
        }
        if (scanResult.capabilities.contains("WEP")) {
            i = WifiDetectManager.SECURITY_WEP;
        } else if (scanResult.capabilities.contains("PSK")) {
            i = WifiDetectManager.SECURITY_PSK;
        } else if (scanResult.capabilities.contains("EAP")) {
            i = WifiDetectManager.SECURITY_EAP;
        }
        d.e("WifiDetectManager", "[End]detectSecurity-Ret:[" + i + "]");
        return i;
    }

    public int getSingletonType() {
        return 2;
    }

    public int o(String str, String str2) {
        int i;
        IOException iOException;
        Throwable th;
        Throwable th2;
        HttpURLConnection httpURLConnection = null;
        ma.bx(29981);
        d.e("WifiDetectManager", "[Beg]detectNetworkState-urlApprove:[" + str + "]customHeader:[" + str2 + "]");
        try {
            long currentTimeMillis = System.currentTimeMillis();
            d.e("WifiDetectManager", "openConnection");
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) new URL(str).openConnection();
            try {
                Object obj;
                if (j.iM() < 8) {
                    System.setProperty("http.keepAlive", "false");
                }
                httpURLConnection2.setUseCaches(false);
                httpURLConnection2.setRequestProperty("Pragma", "no-cache");
                httpURLConnection2.setRequestProperty("Cache-Control", "no-cache");
                httpURLConnection2.setInstanceFollowRedirects(false);
                httpURLConnection2.setRequestMethod("GET");
                httpURLConnection2.setReadTimeout(30000);
                int responseCode = httpURLConnection2.getResponseCode();
                d.e("WifiDetectManager", "getResponseCode:[" + responseCode + "]timeMillis:[" + (System.currentTimeMillis() - currentTimeMillis) + "]");
                if (responseCode != SmsCheckResult.ESCT_200) {
                    if (responseCode >= SmsCheckResult.ESCT_301) {
                        if (responseCode > SmsCheckResult.ESCT_305) {
                        }
                    }
                    Object obj2 = null;
                    obj = null;
                    if (httpURLConnection2 != null) {
                        httpURLConnection2.disconnect();
                    }
                    if (obj == null || r4 == null) {
                        if (obj != null && r4 == null) {
                            i = 1;
                            d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                            return i;
                        }
                        i = 2;
                        d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                        return i;
                    }
                    i = 3;
                    d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                    return i;
                }
                String headerField = httpURLConnection2.getHeaderField(str2);
                d.e("WifiDetectManager", "customHeader: " + headerField);
                if (headerField != null) {
                    if (headerField.equals(str2)) {
                        headerField = r1;
                        obj = 1;
                        if (httpURLConnection2 != null) {
                            httpURLConnection2.disconnect();
                        }
                        if (obj == null) {
                            i = 3;
                            d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                            return i;
                        }
                        i = 1;
                        d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                        return i;
                    }
                }
                String a = a(str, httpURLConnection2);
                headerField = a;
                obj = 1;
                if (httpURLConnection2 != null) {
                    httpURLConnection2.disconnect();
                }
                if (obj == null) {
                    i = 3;
                    d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                    return i;
                }
                i = 1;
            } catch (IOException e) {
                IOException iOException2 = e;
                httpURLConnection = httpURLConnection2;
                iOException = iOException2;
                try {
                    d.e("WifiDetectManager", "IOException:[" + iOException + "]");
                    iOException.printStackTrace();
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    i = 2;
                    d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
                    return i;
                } catch (Throwable th3) {
                    th = th3;
                    if (httpURLConnection != null) {
                        httpURLConnection.disconnect();
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th2 = th4;
                httpURLConnection = httpURLConnection2;
                th = th2;
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
                throw th;
            }
        } catch (IOException e2) {
            iOException = e2;
            d.e("WifiDetectManager", "IOException:[" + iOException + "]");
            iOException.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            i = 2;
            d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
            return i;
        } catch (Throwable th5) {
            th = th5;
            d.e("WifiDetectManager", "Throwable:[" + th + "]");
            th.printStackTrace();
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
            i = 2;
            d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
            return i;
        }
        d.e("WifiDetectManager", "[End]detectNetworkState-nRet:[" + i + "]");
        return i;
    }

    public void onCreate(Context context) {
        d.e("WifiDetectManager", "OnCreate-context:[" + context + "]");
        this.mContext = context;
        this.zG = (WifiManager) context.getSystemService("wifi");
        this.yP = jq.cu();
        this.yO = new AnonymousClass1(this, Looper.getMainLooper());
    }
}
