package com.android.server.connectivity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import java.util.Objects;

public class ProxyTracker {
    private static final boolean DBG = true;
    private static final String TAG = ProxyTracker.class.getSimpleName();
    private final Context mContext;
    @GuardedBy({"mProxyLock"})
    private volatile ProxyInfo mDefaultProxy = null;
    @GuardedBy({"mProxyLock"})
    private boolean mDefaultProxyEnabled = true;
    @GuardedBy({"mProxyLock"})
    private ProxyInfo mGlobalProxy = null;
    private final PacManager mPacManager;
    private final Object mProxyLock = new Object();

    public ProxyTracker(Context context, Handler connectivityServiceInternalHandler, int pacChangedEvent) {
        this.mContext = context;
        this.mPacManager = new PacManager(context, connectivityServiceInternalHandler, pacChangedEvent);
    }

    private static ProxyInfo canonicalizeProxyInfo(ProxyInfo proxy) {
        if (proxy == null || !TextUtils.isEmpty(proxy.getHost()) || !Uri.EMPTY.equals(proxy.getPacFileUrl())) {
            return proxy;
        }
        return null;
    }

    public static boolean proxyInfoEqual(ProxyInfo a, ProxyInfo b) {
        ProxyInfo pa = canonicalizeProxyInfo(a);
        ProxyInfo pb = canonicalizeProxyInfo(b);
        return Objects.equals(pa, pb) && (pa == null || Objects.equals(pa.getHost(), pb.getHost()));
    }

    public ProxyInfo getDefaultProxy() {
        synchronized (this.mProxyLock) {
            if (this.mGlobalProxy != null) {
                return this.mGlobalProxy;
            } else if (!this.mDefaultProxyEnabled) {
                return null;
            } else {
                return this.mDefaultProxy;
            }
        }
    }

    public ProxyInfo getGlobalProxy() {
        ProxyInfo proxyInfo;
        synchronized (this.mProxyLock) {
            proxyInfo = this.mGlobalProxy;
        }
        return proxyInfo;
    }

    public void loadGlobalProxy() {
        ProxyInfo proxyProperties;
        ContentResolver res = this.mContext.getContentResolver();
        String host = Settings.Global.getString(res, "global_http_proxy_host");
        int port = Settings.Global.getInt(res, "global_http_proxy_port", 0);
        String exclList = Settings.Global.getString(res, "global_http_proxy_exclusion_list");
        String pacFileUrl = Settings.Global.getString(res, "global_proxy_pac_url");
        if (!TextUtils.isEmpty(host) || !TextUtils.isEmpty(pacFileUrl)) {
            if (!TextUtils.isEmpty(pacFileUrl)) {
                proxyProperties = new ProxyInfo(pacFileUrl);
            } else {
                proxyProperties = new ProxyInfo(host, port, exclList);
            }
            if (!proxyProperties.isValid()) {
                String str = TAG;
                Slog.d(str, "Invalid proxy properties, ignoring: " + proxyProperties);
                return;
            }
            synchronized (this.mProxyLock) {
                this.mGlobalProxy = proxyProperties;
            }
        }
        loadDeprecatedGlobalHttpProxy();
    }

    public void loadDeprecatedGlobalHttpProxy() {
        String proxy = Settings.Global.getString(this.mContext.getContentResolver(), "http_proxy");
        if (!TextUtils.isEmpty(proxy)) {
            String[] data = proxy.split(":");
            if (data.length != 0) {
                String proxyHost = data[0];
                int proxyPort = 8080;
                if (data.length > 1) {
                    try {
                        proxyPort = Integer.parseInt(data[1]);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                setGlobalProxy(new ProxyInfo(proxyHost, proxyPort, ""));
            }
        }
    }

    public void sendProxyBroadcast() {
        ProxyInfo defaultProxy = getDefaultProxy();
        ProxyInfo proxyInfo = defaultProxy != null ? defaultProxy : new ProxyInfo("", 0, "");
        if (this.mPacManager.setCurrentProxyScriptUrl(proxyInfo)) {
            String str = TAG;
            Slog.d(str, "sending Proxy Broadcast for " + proxyInfo);
            Intent intent = new Intent("android.intent.action.PROXY_CHANGE");
            intent.addFlags(603979776);
            intent.putExtra("android.intent.extra.PROXY_INFO", proxyInfo);
            long ident = Binder.clearCallingIdentity();
            try {
                this.mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public void setGlobalProxy(ProxyInfo proxyInfo) {
        String pacFileUrl;
        String exclList;
        int port;
        String host;
        synchronized (this.mProxyLock) {
            if (proxyInfo != this.mGlobalProxy) {
                if (proxyInfo != null && proxyInfo.equals(this.mGlobalProxy)) {
                    return;
                }
                if (this.mGlobalProxy == null || !this.mGlobalProxy.equals(proxyInfo)) {
                    if (proxyInfo == null || (TextUtils.isEmpty(proxyInfo.getHost()) && Uri.EMPTY.equals(proxyInfo.getPacFileUrl()))) {
                        host = "";
                        port = 0;
                        exclList = "";
                        pacFileUrl = "";
                        this.mGlobalProxy = null;
                    } else if (!proxyInfo.isValid()) {
                        String str = TAG;
                        Slog.d(str, "Invalid proxy properties, ignoring: " + proxyInfo);
                        return;
                    } else {
                        this.mGlobalProxy = new ProxyInfo(proxyInfo);
                        host = this.mGlobalProxy.getHost();
                        port = this.mGlobalProxy.getPort();
                        exclList = this.mGlobalProxy.getExclusionListAsString();
                        pacFileUrl = Uri.EMPTY.equals(proxyInfo.getPacFileUrl()) ? "" : proxyInfo.getPacFileUrl().toString();
                    }
                    ContentResolver res = this.mContext.getContentResolver();
                    long token = Binder.clearCallingIdentity();
                    try {
                        Settings.Global.putString(res, "global_http_proxy_host", host);
                        Settings.Global.putInt(res, "global_http_proxy_port", port);
                        Settings.Global.putString(res, "global_http_proxy_exclusion_list", exclList);
                        Settings.Global.putString(res, "global_proxy_pac_url", pacFileUrl);
                        Binder.restoreCallingIdentity(token);
                        sendProxyBroadcast();
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                }
            }
        }
    }

    public void setDefaultProxy(ProxyInfo proxyInfo) {
        synchronized (this.mProxyLock) {
            if (!Objects.equals(this.mDefaultProxy, proxyInfo)) {
                if (proxyInfo != null && !proxyInfo.isValid()) {
                    String str = TAG;
                    Slog.d(str, "Invalid proxy properties, ignoring: " + proxyInfo);
                } else if (this.mGlobalProxy == null || proxyInfo == null || Uri.EMPTY.equals(proxyInfo.getPacFileUrl()) || !proxyInfo.getPacFileUrl().equals(this.mGlobalProxy.getPacFileUrl())) {
                    this.mDefaultProxy = proxyInfo;
                    if (this.mGlobalProxy == null) {
                        if (this.mDefaultProxyEnabled) {
                            sendProxyBroadcast();
                        }
                    }
                } else {
                    this.mGlobalProxy = proxyInfo;
                    sendProxyBroadcast();
                }
            }
        }
    }
}
