package com.android.server.wifi;

import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.ncdft.HwWifiDFTConnManager;
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
    private static int isRedirect = 0;
    private static boolean mEnableCheck = false;
    private static boolean mFirstDetect = false;
    private int errorCode = 0;
    private int mReason = 0;

    public HwCHRWebDetectThread(int reason) {
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
            wcsm.updateAccessWebException(this.mReason, this.errorCode == 2 ? "ERROR_PORTAL" : "OTHER");
        }
    }

    public boolean isInternetConnected() {
        boolean z = true;
        boolean ret = false;
        int IsRedirect = 1;
        String[] srvUrls = new String[]{"http://www.baidu.com", BACKUP_SERVER_URL};
        if (!mEnableCheck) {
            return false;
        }
        boolean isBeta = !HwWifiDFTConnManager.getInstance().isCommercialUser();
        if (isBeta) {
            dumpWifiEnv("/data/log/wifi/wifi_env_0.dump");
        }
        this.errorCode = 0;
        for (int i = 0; i < srvUrls.length; i++) {
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
            if (respCode == 200) {
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
        if (this.errorCode != 2) {
            z = false;
        }
        hwWifiStatStore.incrAccessWebRecord(i2, ret, z);
        return ret;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    private int doHttpRequest(String urlAddr) {
        int resCode = -1;
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = (HttpURLConnection) new URL(urlAddr).openConnection();
            httpURLConnection.setConnectTimeout(4000);
            httpURLConnection.setReadTimeout(4000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setInstanceFollowRedirects(false);
            resCode = httpURLConnection.getResponseCode();
            Log.d(TAG, "browse the web " + urlAddr + ",  returns responsecode =" + resCode);
            if (isRedirectedRespCode(resCode)) {
                String str;
                this.errorCode = 2;
                String connHeadLocation = httpURLConnection.getHeaderField("Location");
                String str2 = TAG;
                StringBuilder append = new StringBuilder().append("Location=");
                if (connHeadLocation == null) {
                    str = "null";
                } else {
                    str = connHeadLocation;
                }
                Log.d(str2, append.append(str).toString());
                String host = WifiProCommonUtils.parseHostByUrlLocation(connHeadLocation);
                if (host != null && (host.contains("baidu") || host.contains("youku"))) {
                    this.errorCode = 200;
                }
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
        if (respCode < 300 || respCode > 307) {
            i = 0;
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
