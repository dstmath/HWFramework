package com.huawei.hwwifiproservice;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Network;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.hwUtil.StringUtilEx;
import com.android.server.wifipro.WifiProCommonUtils;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
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
    private ContentResolver mContentResolver = null;
    private Context mContext = null;
    private int mCurrentSessionId;
    private AtomicBoolean mDisconnected = new AtomicBoolean(true);
    private String mGatewayAddr;
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
            HwHiLog.w(TAG, false, "looper null, force create single thread", new Object[0]);
            HandlerThread handlerThread = new HandlerThread("wifipro_router_internet_detector_handler_thread");
            handlerThread.start();
            looper = handlerThread.getLooper();
        }
        this.mHandler = new Handler(looper) {
            /* class com.huawei.hwwifiproservice.HwRouterInternetDetector.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_WIFI /* 101 */:
                        HwRouterInternetDetector.this.handleHttpGetGatewayDetect(msg.arg1);
                        break;
                    case WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_MOBILE /* 102 */:
                        HwRouterInternetDetector.this.notifyRouterInternetStatus(false);
                        break;
                    case HwRouterInternetDetector.CMD_NOTIFY_ROUTER_INTERNET_RECOVERY /* 103 */:
                        HwRouterInternetDetector.this.notifyRouterInternetStatus(true);
                        break;
                    case HwRouterInternetDetector.CMD_NOTIFY_CONNECTION_DISCONNECTED /* 104 */:
                        HwRouterInternetDetector.this.mDisconnected.set(true);
                        HwRouterInternetDetector.this.mGatewayAddr = null;
                        Settings.Secure.putInt(HwRouterInternetDetector.this.mContentResolver, HwRouterInternetDetector.WIFIPRO_ROUTER_NO_INTERNET_FLAG, 100);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHttpGetGatewayDetect(int sessionId) {
        String str;
        HwHiLog.d(TAG, false, "handleHttpGetGatewayDetect sessionId = %{private}d disconnected :%{public}s", new Object[]{Integer.valueOf(sessionId), String.valueOf(this.mDisconnected.get())});
        if (isGatewayReachable(sessionId)) {
            this.mHandler.sendEmptyMessage(WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_MOBILE);
        } else if (!this.mDisconnected.get() && (str = this.mGatewayAddr) != null && WifiProCommonUtils.isNetworkReachableByIcmp(str, (int) WAIT_TIMEOUT_MS)) {
            this.mHandler.sendEmptyMessage(WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_MOBILE);
        } else if (!this.mDisconnected.get()) {
            this.mHwSCE.notifyRouterGatewayUnreachable();
        } else {
            HwHiLog.w(TAG, false, "invaild Gateway detect", new Object[0]);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0099, code lost:
        if (0 == 0) goto L_0x00a2;
     */
    private boolean isGatewayReachable(int sessionId) {
        this.mNetwork = (Network) WifiProManagerEx.ctrlHwWifiNetwork(WifiProManagerEx.SERVICE_NAME, 14, new Bundle()).getParcelable(NetworkMonitor.KEY_NETWORK_NAME);
        if (this.mNetwork == null) {
            HwHiLog.e(TAG, false, "isGatewayReachable, mNetwork = null", new Object[0]);
            return false;
        }
        int httpResponseCode = CaptivePortalProbeResult.FAILED_CODE;
        HttpURLConnection urlConnection = null;
        if (!this.mDisconnected.get() && this.mGatewayAddr != null) {
            try {
                URLConnection connection = this.mNetwork.openConnection(new URL(HTTP_GET_HEAD + this.mGatewayAddr));
                if (!(connection instanceof HttpURLConnection)) {
                    HwHiLog.e(TAG, false, "isGatewayReachable, openConnection doesn't return HttpURLConnection instance.", new Object[0]);
                    if (0 != 0) {
                        urlConnection.disconnect();
                    }
                    return false;
                }
                urlConnection = (HttpURLConnection) connection;
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setConnectTimeout(SOCKET_TIMEOUT_MS);
                urlConnection.setReadTimeout(SOCKET_TIMEOUT_MS);
                urlConnection.setUseCaches(false);
                urlConnection.getInputStream();
                httpResponseCode = urlConnection.getResponseCode();
                urlConnection.disconnect();
            } catch (IOException e) {
                HwHiLog.e(TAG, false, "isGatewayReachable, unable to HTTP gateway.", new Object[0]);
            } catch (SecurityException e2) {
                HwHiLog.e(TAG, false, "Exception happened in isGatewayReachable", new Object[0]);
                if (0 != 0) {
                }
            } catch (Throwable th) {
                if (0 != 0) {
                    urlConnection.disconnect();
                }
                throw th;
            }
        }
        HwHiLog.d(TAG, false, "isGatewayReachable, httpResponseCode is %{public}d sessionId = %{private}d mCurrentSessionId = %{private}d", new Object[]{Integer.valueOf(httpResponseCode), Integer.valueOf(sessionId), Integer.valueOf(this.mCurrentSessionId)});
        if (this.mDisconnected.get() || httpResponseCode < 200 || httpResponseCode >= 400 || sessionId != this.mCurrentSessionId) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyRouterInternetStatus(boolean routerHasInternet) {
        HwHiLog.d(TAG, false, "notifyRouterInternetStatus, routerHasInternet %{public}s mDisconnected.get() %{public}s", new Object[]{String.valueOf(routerHasInternet), String.valueOf(this.mDisconnected.get())});
        if (this.mDisconnected.get()) {
            HwHiLog.w(TAG, false, "Disconnected, do nothing.", new Object[0]);
            return;
        }
        if (routerHasInternet) {
            this.mNoInternetOnSettings.set(false);
        } else {
            this.mHwSCE.notifySettingsDisplayNoInternet();
            this.mNoInternetOnSettings.set(true);
        }
        Settings.Secure.putInt(this.mContentResolver, WIFIPRO_ROUTER_NO_INTERNET_FLAG, routerHasInternet ? WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_MOBILE : WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_WIFI);
    }

    public synchronized void notifyNoInternetAfterCure(String gatewayAddr, int authType, boolean isMobileHotspot) {
        if (!this.mNoInternetOnSettings.get() && !TextUtils.isEmpty(gatewayAddr) && !isMobileHotspot && WifiProCommonUtils.isEncryptedAuthType(authType) && (WifiProCommonUtils.isWifiProSwitchOn(this.mContext) || WifiProCommonUtils.isWifiProLitePropertyEnabled(this.mContext))) {
            this.mDisconnected.set(false);
            this.mGatewayAddr = gatewayAddr;
            this.mCurrentSessionId = new SecureRandom().nextInt(100000);
            HwHiLog.d(TAG, false, "notifyNoInternetAfterCure, Start test gatewayAddr %{public}s mCurrentSessionId %{private}d", new Object[]{StringUtilEx.safeDisplayIpAddress(gatewayAddr), Integer.valueOf(this.mCurrentSessionId)});
            this.mHandler.sendMessage(Message.obtain(this.mHandler, WifiProUIDisplayManager.DIALOG_TYPE_WIFI_HANDOVER_WIFI, this.mCurrentSessionId, -1));
        }
    }

    public synchronized void notifyInternetAccessRecovery() {
        if (this.mNoInternetOnSettings.get()) {
            this.mHandler.sendEmptyMessage(CMD_NOTIFY_ROUTER_INTERNET_RECOVERY);
        }
    }

    public synchronized void notifyDisconnected() {
        this.mNoInternetOnSettings.set(false);
        this.mHandler.sendEmptyMessage(CMD_NOTIFY_CONNECTION_DISCONNECTED);
    }
}
