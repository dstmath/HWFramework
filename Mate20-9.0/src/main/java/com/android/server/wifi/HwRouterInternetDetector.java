package com.android.server.wifi;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Network;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.HwServiceFactory;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class HwRouterInternetDetector {
    private static final int CMD_HTTP_GET_GATEWAY_DETECT = 101;
    private static final int CMD_NOTIFY_CONNECTION_DISCONNECTED = 104;
    private static final int CMD_NOTIFY_ROUTER_INTERNET_RECOVERY = 103;
    private static final int CMD_NOTIFY_ROUTER_NO_INTERNET = 102;
    public static final String HTTP_GET_HEAD = "http://";
    private static final int ROUTER_HAS_INTERNET = 102;
    private static final int ROUTER_NO_INTERNET = 101;
    private static final int ROUTER_UNKNOWN = 100;
    private static final int SOCKET_TIMEOUT_MS = 6000;
    private static final String TAG = "HwRouterInternetDetector";
    private static final int WAIT_TIMEOUT_MS = 1000;
    public static final String WIFIPRO_ROUTER_NO_INTERNET_FLAG = "WIFIPRO_ROUTER_NO_INTERNET_FLAG";
    private static HwRouterInternetDetector hwRouterInternetDetector = null;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver = null;
    private Context mContext = null;
    private int mCurrentSessionId;
    /* access modifiers changed from: private */
    public AtomicBoolean mDisconnected = new AtomicBoolean(true);
    /* access modifiers changed from: private */
    public String mGatewayAddr;
    private Handler mHandler;
    private HwSelfCureEngine mHwSCE = null;
    private Network mNetwork;
    private AtomicBoolean mNoInternetOnSettings = new AtomicBoolean(false);

    public HwRouterInternetDetector(Context context, HwSelfCureEngine hwSCE) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mHwSCE = hwSCE;
        init();
    }

    public static synchronized HwRouterInternetDetector getInstance(Context context, HwSelfCureEngine hwSCE) {
        HwRouterInternetDetector hwRouterInternetDetector2;
        synchronized (HwRouterInternetDetector.class) {
            if (hwRouterInternetDetector == null) {
                hwRouterInternetDetector = new HwRouterInternetDetector(context, hwSCE);
            }
            hwRouterInternetDetector2 = hwRouterInternetDetector;
        }
        return hwRouterInternetDetector2;
    }

    private void init() {
        Looper looper;
        if (this.mHwSCE.getHandler() != null) {
            looper = this.mHwSCE.getHandler().getLooper();
        } else {
            LOGW("looper null, force create single thread");
            HandlerThread handlerThread = new HandlerThread("wifipro_router_internet_detector_handler_thread");
            handlerThread.start();
            looper = handlerThread.getLooper();
        }
        this.mHandler = new Handler(looper) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:
                        HwRouterInternetDetector.this.handleHttpGetGatewayDetect(msg.arg1);
                        break;
                    case 102:
                        HwRouterInternetDetector.this.notifyRouterInternetStatus(false);
                        break;
                    case 103:
                        HwRouterInternetDetector.this.notifyRouterInternetStatus(true);
                        break;
                    case 104:
                        HwRouterInternetDetector.this.mDisconnected.set(true);
                        String unused = HwRouterInternetDetector.this.mGatewayAddr = null;
                        Settings.Secure.putInt(HwRouterInternetDetector.this.mContentResolver, HwRouterInternetDetector.WIFIPRO_ROUTER_NO_INTERNET_FLAG, 100);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    public void handleHttpGetGatewayDetect(int sessionId) {
        LOGD("handleHttpGetGatewayDetect sessionId = " + sessionId + " disconnected :" + this.mDisconnected.get());
        if (isGatewayReachable(sessionId)) {
            this.mHandler.sendEmptyMessage(102);
        } else if (!this.mDisconnected.get() && this.mGatewayAddr != null && WifiProCommonUtils.isNetworkReachableByICMP(this.mGatewayAddr, 1000)) {
            this.mHandler.sendEmptyMessage(102);
        } else if (!this.mDisconnected.get()) {
            this.mHwSCE.notifyRouterGatewayUnreachable();
        }
    }

    /* JADX WARNING: type inference failed for: r4v9, types: [java.net.URLConnection] */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x006c, code lost:
        if (r2 != null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0083, code lost:
        if (r2 == null) goto L_0x008c;
     */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean isGatewayReachable(int sessionId) {
        this.mNetwork = HwServiceFactory.getHwConnectivityManager().getNetworkForTypeWifi();
        if (this.mNetwork == null) {
            LOGD("isGatewayReachable, mNetwork = null");
            return false;
        }
        int httpResponseCode = 599;
        HttpURLConnection urlConnection = null;
        if (!this.mDisconnected.get() && this.mGatewayAddr != null) {
            try {
                ? openConnection = this.mNetwork.openConnection(new URL(HTTP_GET_HEAD + this.mGatewayAddr));
                if (!(openConnection instanceof HttpURLConnection)) {
                    LOGW("isGatewayReachable, openConnection doesn't return HttpURLConnection instance.");
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    return false;
                }
                urlConnection = openConnection;
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
                urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
                urlConnection.setUseCaches(false);
                urlConnection.getInputStream();
                httpResponseCode = urlConnection.getResponseCode();
            } catch (IOException e) {
                LOGW("isGatewayReachable, IOException, unable to HTTP gateway.");
            } catch (SecurityException e2) {
                LOGW("isGatewayReachable, SecurityException, unable to HTTP gateway.");
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            } catch (Throwable th) {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                throw th;
            }
        }
        LOGD("isGatewayReachable, httpResponseCode " + httpResponseCode + " sessionId =" + sessionId + " mCurrentSessionId = " + this.mCurrentSessionId);
        if (this.mDisconnected.get() || httpResponseCode < 200 || httpResponseCode >= 400 || sessionId != this.mCurrentSessionId) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    public void notifyRouterInternetStatus(boolean routerHasInternet) {
        LOGD("notifyRouterInternetStatus, routerHasInternet " + routerHasInternet + " mDisconnected.get() " + this.mDisconnected.get());
        if (this.mDisconnected.get()) {
            LOGD("Disconnected, do nothing.");
            return;
        }
        if (routerHasInternet) {
            this.mNoInternetOnSettings.set(false);
        } else {
            this.mHwSCE.notifySettingsDisplayNoInternet();
            this.mNoInternetOnSettings.set(true);
        }
        Settings.Secure.putInt(this.mContentResolver, WIFIPRO_ROUTER_NO_INTERNET_FLAG, routerHasInternet ? 102 : 101);
    }

    public synchronized void notifyNoInternetAfterCure(String gatewayAddr, int authType, boolean isMobileHotspot) {
        if (!this.mNoInternetOnSettings.get() && !TextUtils.isEmpty(gatewayAddr) && !isMobileHotspot && ((authType == 1 || authType == 4) && (WifiProCommonUtils.isWifiProSwitchOn(this.mContext) || WifiProCommonUtils.isWifiProLitePropertyEnabled(this.mContext)))) {
            this.mDisconnected.set(false);
            this.mGatewayAddr = gatewayAddr;
            this.mCurrentSessionId = new SecureRandom().nextInt(100000);
            LOGD("notifyNoInternetAfterCure, Start test gatewayAddr " + gatewayAddr + " mCurrentSessionId " + this.mCurrentSessionId);
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 101, this.mCurrentSessionId, -1));
        }
    }

    public synchronized void notifyInternetAccessRecovery() {
        if (this.mNoInternetOnSettings.get()) {
            this.mHandler.sendEmptyMessage(103);
        }
    }

    public synchronized void notifyDisconnected() {
        this.mNoInternetOnSettings.set(false);
        this.mHandler.sendEmptyMessage(104);
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }

    public void LOGW(String msg) {
        Log.w(TAG, msg);
    }
}
