package com.android.server.wifi;

import android.util.Log;
import com.huawei.connectivitylog.LogManager;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

public class HwCHRWebDetectThread extends Thread {
    private static final String BACKUP_SERVER_URL = "http://www.youku.com";
    private static final int ERROR_PORTAL = 2;
    private static final int ERROR_UNKNOW = 0;
    private static final String MAIN_SERVER_URL = "http://www.baidu.com";
    private static final int MY_HTTP_ERR = -1;
    private static final int MY_HTTP_OK = 200;
    private static final int SOCKET_TIMEOUT_MS = 4000;
    private static final String TAG = "HwCHRWebDetectThread";
    private static int isRedirect;
    private static boolean mEnableCheck;
    private static boolean mFirstDetect;
    private int errorCode;
    private int mReason;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwCHRWebDetectThread.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwCHRWebDetectThread.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwCHRWebDetectThread.<clinit>():void");
    }

    public HwCHRWebDetectThread(int reason) {
        this.mReason = ERROR_UNKNOW;
        this.errorCode = ERROR_UNKNOW;
        this.mReason = reason;
    }

    public static void setEnableCheck(boolean enableCheck) {
        mEnableCheck = enableCheck;
    }

    public static void setFirstDetect(boolean firstDetect) {
        mFirstDetect = firstDetect;
    }

    private static void dumpWifiEnv(String fileName) {
        try {
            new HwWifiDFTUtilImpl().checkAndCreatWifiLogDir();
            Runtime.getRuntime().exec("wifichrdump -s wifi -f " + fileName + " -t arp").waitFor();
        } catch (InterruptedException e) {
        } catch (IOException e2) {
        }
    }

    public void run() {
        boolean ret = isInternetConnected();
        HwWifiCHRStateManager wcsm = HwWifiCHRStateManagerImpl.getDefault();
        if (!ret && wcsm != null) {
            wcsm.updateAccessWebException(this.mReason, this.errorCode == ERROR_PORTAL ? "ERROR_PORTAL" : "OTHER");
        }
    }

    public boolean isInternetConnected() {
        boolean z = true;
        boolean ret = false;
        int IsRedirect = 1;
        String[] srvUrls = new String[ERROR_PORTAL];
        srvUrls[ERROR_UNKNOW] = MAIN_SERVER_URL;
        srvUrls[1] = BACKUP_SERVER_URL;
        if (!mEnableCheck) {
            return false;
        }
        boolean isBeta;
        if (LogManager.getInstance().isCommercialUserFromCache()) {
            isBeta = false;
        } else {
            isBeta = true;
        }
        if (isBeta) {
            dumpWifiEnv("/data/log/wifi/wifi_env_0.dump");
        }
        this.errorCode = ERROR_UNKNOW;
        for (int i = ERROR_UNKNOW; i < srvUrls.length; i++) {
            int respCode;
            if (i > 0) {
                HwWiFiLogUtils logUtils = HwWiFiLogUtils.getDefault();
                logUtils.startLinkLayerLog();
                respCode = doHttpRequest(srvUrls[i]);
                logUtils.stopLinkLayerLog();
            } else {
                respCode = doHttpRequest(srvUrls[i]);
            }
            IsRedirect &= isRedirect();
            if (respCode == MY_HTTP_OK) {
                ret = true;
                break;
            }
        }
        if (mFirstDetect) {
            Log.d(TAG, "first Detect after connect, IsPortalConnection=" + IsRedirect);
            updatePortalConnection(IsRedirect);
        }
        Log.d(TAG, "connect web mReason=" + this.mReason + " ret=" + ret);
        if (!ret && isBeta) {
            dumpWifiEnv("/data/log/wifi/wifi_env_1.dump");
        }
        HwWifiStatStore hwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        int i2 = this.mReason;
        if (this.errorCode != ERROR_PORTAL) {
            z = false;
        }
        hwWifiStatStore.incrAccessWebRecord(i2, ret, z);
        return ret;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    private int doHttpRequest(String urlAddr) {
        int resCode = MY_HTTP_ERR;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(urlAddr).openConnection();
            httpURLConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            httpURLConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(false);
            resCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "browse the web " + urlAddr + ",  returns responsecode =" + resCode);
            if (isRedirectedRespCode(resCode)) {
                this.errorCode = ERROR_PORTAL;
                String connHeadLocation = httpURLConnection.getHeaderField("Location");
                String str = TAG;
                StringBuilder append = new StringBuilder().append("Location=");
                if (connHeadLocation == null) {
                    connHeadLocation = "null";
                }
                Log.d(str, append.append(connHeadLocation).toString());
            }
            if (httpURLConnection != null) {
                Log.d(TAG, "doHttpRequest: Disconnect URLConnection");
                httpURLConnection.disconnect();
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "URL " + urlAddr + " is not available.");
            if (httpURLConnection != null) {
                Log.d(TAG, "doHttpRequest: Disconnect URLConnection");
                httpURLConnection.disconnect();
            }
        } catch (UnknownHostException e2) {
            Log.d(TAG, "doHttpRequest: UnknownHostException: " + e2);
            e2.printStackTrace();
            if (httpURLConnection != null) {
                Log.d(TAG, "doHttpRequest: Disconnect URLConnection");
                httpURLConnection.disconnect();
            }
        } catch (IOException e3) {
            Log.d(TAG, "doHttpRequest: IOException: " + e3);
            e3.printStackTrace();
            if (httpURLConnection != null) {
                Log.d(TAG, "doHttpRequest: Disconnect URLConnection");
                httpURLConnection.disconnect();
            }
        } catch (Exception e4) {
            e4.printStackTrace();
            if (httpURLConnection != null) {
                Log.d(TAG, "doHttpRequest: Disconnect URLConnection");
                httpURLConnection.disconnect();
            }
        } catch (Throwable th) {
            if (httpURLConnection != null) {
                Log.d(TAG, "doHttpRequest: Disconnect URLConnection");
                httpURLConnection.disconnect();
            }
        }
        return resCode;
    }

    private static boolean isRedirectedRespCode(int respCode) {
        int i;
        if (respCode < TCPIpqRtt.RTT_FINE_5 || respCode > 307) {
            i = ERROR_UNKNOW;
        } else {
            i = 1;
        }
        isRedirect = i;
        return isRedirect == 1;
    }

    public static int isRedirect() {
        return isRedirect;
    }

    private void updatePortalConnection(int isPortalconnection) {
        HwWifiCHRStateManagerImpl.getDefaultImpl().updatePortalConnection(isPortalconnection);
    }
}
