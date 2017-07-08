package com.android.server;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.INetworkPolicyManager;
import android.net.INetworkStatsService;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.os.INetworkManagementService;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.connectivity.NetworkMonitor;
import com.android.server.connectivity.Tethering;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.huawei.deliver.info.HwDeliverInfo;

public class HwConnectivityManagerImpl implements HwConnectivityManager {
    private static final String COUNTRY_CODE_CN = "460";
    protected static final boolean HWFLOW;
    private static final boolean IS_CHINA;
    static final Uri MSIM_TELEPHONY_CARRIERS_URI;
    private static final String P2P_TETHER_IFAC = "p2p-wlan0-";
    private static final String P2P_TETHER_IFAC_110x = "p2p-p2p0-";
    protected static final String PROPERTY_BTHOTSPOT_ON = "sys.isbthotspoton";
    protected static final String PROPERTY_USBTETHERING_ON = "sys.isusbtetheringon";
    protected static final String PROPERTY_WIFIHOTSPOT_ON = "sys.iswifihotspoton";
    private static final String TAG = null;
    private static final boolean VDBG = false;
    private static final String WIFI_AP_MANUAL_CONNECT = "wifi_ap_manual_connect";
    private static HwConnectivityManager mInstance;
    private HwConnectivityService mHwConnectivityService;

    public boolean checkDunExisted(android.content.Context r19) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0198 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:42)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:58)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r18 = this;
        r2 = "ro.config.enable.gdun";
        r6 = 0;
        r2 = android.os.SystemProperties.getBoolean(r2, r6);
        if (r2 == 0) goto L_0x000a;
    L_0x000a:
        r0 = r18;
        r2 = r0.mHwConnectivityService;
        if (r2 != 0) goto L_0x001a;
    L_0x0010:
        r2 = TAG;
        r6 = "mHwConnectivityService == null ,return false";
        android.util.Log.d(r2, r6);
        r2 = 0;
        return r2;
    L_0x001a:
        r2 = TAG;
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "isSystemBootComplete =";
        r6 = r6.append(r7);
        r0 = r18;
        r7 = r0.mHwConnectivityService;
        r7 = r7.isSystemBootComplete();
        r6 = r6.append(r7);
        r6 = r6.toString();
        android.util.Log.d(r2, r6);
        r0 = r18;
        r2 = r0.mHwConnectivityService;
        r2 = r2.isSystemBootComplete();
        if (r2 != 0) goto L_0x0047;
    L_0x0045:
        r2 = 0;
        return r2;
    L_0x0047:
        r15 = android.telephony.SubscriptionManager.getDefaultDataSubscriptionId();
        r2 = android.telephony.TelephonyManager.getDefault();
        r16 = r2.getCurrentPhoneType(r15);
        r14 = 0;
        r2 = TAG;
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = " type:";
        r6 = r6.append(r7);
        r0 = r16;
        r6 = r6.append(r0);
        r7 = " subId = ";
        r6 = r6.append(r7);
        r6 = r6.append(r15);
        r6 = r6.toString();
        android.util.Log.d(r2, r6);
        r2 = 1;
        r0 = r16;
        if (r0 != r2) goto L_0x0134;
    L_0x007f:
        r2 = android.telephony.TelephonyManager.getDefault();
        r14 = r2.getSimOperator(r15);
        r2 = TAG;
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = " operator:";
        r6 = r6.append(r7);
        r6 = r6.append(r14);
        r6 = r6.toString();
        android.util.Log.d(r2, r6);
    L_0x00a0:
        if (r14 == 0) goto L_0x0198;
    L_0x00a2:
        r2 = 3;
        r4 = new java.lang.String[r2];
        r2 = "type";
        r6 = 0;
        r4[r6] = r2;
        r2 = "proxy";
        r6 = 1;
        r4[r6] = r2;
        r2 = "port";
        r6 = 2;
        r4[r6] = r2;
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r6 = "numeric = '";
        r2 = r2.append(r6);
        r2 = r2.append(r14);
        r6 = "' and carrier_enabled = 1";
        r2 = r2.append(r6);
        r5 = r2.toString();
        r12 = 0;
        r2 = android.telephony.TelephonyManager.getDefault();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = r2.isMultiSimEnabled();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r2 == 0) goto L_0x013e;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
    L_0x00dd:
        r2 = MSIM_TELEPHONY_CARRIERS_URI;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = (long) r15;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = java.lang.Long.toString(r6);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r3 = android.net.Uri.withAppendedPath(r2, r6);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = r19.getContentResolver();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = 0;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = 0;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r12 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = HWFLOW;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r2 == 0) goto L_0x010f;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
    L_0x00f6:
        r2 = TAG;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6.<init>();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = "Read DB '";	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r3);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        android.util.Log.d(r2, r6);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
    L_0x010f:
        if (r12 == 0) goto L_0x01a0;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
    L_0x0111:
        r2 = r12.moveToFirst();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r2 == 0) goto L_0x01a0;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
    L_0x0117:
        r2 = "type";	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = r12.getColumnIndexOrThrow(r2);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r17 = r12.getString(r2);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = "dun";	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r0 = r17;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = r0.contains(r2);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r2 == 0) goto L_0x019a;
    L_0x012d:
        r2 = 1;
        if (r12 == 0) goto L_0x0133;
    L_0x0130:
        r12.close();
    L_0x0133:
        return r2;
    L_0x0134:
        r2 = android.telephony.HwInnerTelephonyManagerImpl.getDefault();
        r14 = r2.getOperatorNumeric();
        goto L_0x00a0;
    L_0x013e:
        r6 = r19.getContentResolver();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = android.provider.Telephony.Carriers.CONTENT_URI;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r10 = 0;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r11 = 0;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r8 = r4;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r9 = r5;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r12 = r6.query(r7, r8, r9, r10, r11);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r2 = HWFLOW;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r2 == 0) goto L_0x010f;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
    L_0x0150:
        r2 = TAG;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6.<init>();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = "Read DB '";	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = android.provider.Telephony.Carriers.CONTENT_URI;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        android.util.Log.d(r2, r6);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        goto L_0x010f;
    L_0x016c:
        r13 = move-exception;
        r2 = TAG;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6.<init>();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = "Read DB '";	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = android.provider.Telephony.Carriers.CONTENT_URI;	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r7 = "' failed: ";	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r7);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.append(r13);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        r6 = r6.toString();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        android.util.Log.d(r2, r6);	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r12 == 0) goto L_0x0198;
    L_0x0195:
        r12.close();
    L_0x0198:
        r2 = 0;
        return r2;
    L_0x019a:
        r2 = r12.moveToNext();	 Catch:{ Exception -> 0x016c, all -> 0x01a6 }
        if (r2 != 0) goto L_0x0117;
    L_0x01a0:
        if (r12 == 0) goto L_0x0198;
    L_0x01a2:
        r12.close();
        goto L_0x0198;
    L_0x01a6:
        r2 = move-exception;
        if (r12 == 0) goto L_0x01ac;
    L_0x01a9:
        r12.close();
    L_0x01ac:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.HwConnectivityManagerImpl.checkDunExisted(android.content.Context):boolean");
    }

    public HwConnectivityManagerImpl() {
        this.mHwConnectivityService = null;
    }

    static {
        MSIM_TELEPHONY_CARRIERS_URI = Uri.parse("content://telephony/carriers/subId");
        mInstance = new HwConnectivityManagerImpl();
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : VDBG : true;
        HWFLOW = isLoggable;
        IS_CHINA = "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", AppHibernateCst.INVALID_PKG));
    }

    public ConnectivityService createHwConnectivityService(Context context, INetworkManagementService netd, INetworkStatsService statsService, INetworkPolicyManager policyManager) {
        this.mHwConnectivityService = new HwConnectivityService(context, netd, statsService, policyManager);
        return this.mHwConnectivityService;
    }

    public static HwConnectivityManager getDefault() {
        return mInstance;
    }

    public void setPushServicePowerNormalMode() {
    }

    public boolean setPushServicePowerSaveMode(NetworkInfo networkInfo) {
        return true;
    }

    public void setTetheringProp(Tethering tetheringService, boolean tethering, boolean usb, String ifaceName) {
        Log.d(TAG, "enter setTetheringProp");
        String prop = tethering ? "true" : "false";
        if (usb) {
            try {
                SystemProperties.set(PROPERTY_USBTETHERING_ON, prop);
                Log.d(TAG, "set PROPERTY_USBTETHERING_ON: " + prop);
            } catch (RuntimeException e) {
                Log.e(TAG, "when setTetheringProp ,error =" + e + "  ifaceNmae =" + ifaceName);
            }
        } else if (tetheringService.isWifi(ifaceName)) {
            SystemProperties.set(PROPERTY_WIFIHOTSPOT_ON, prop);
            Log.d(TAG, "set iswifihotspoton = " + prop);
        } else if (tetheringService.isBluetooth(ifaceName)) {
            SystemProperties.set(PROPERTY_BTHOTSPOT_ON, prop);
            Log.d(TAG, "set isbthotspoton = " + prop);
        }
    }

    public boolean setUsbFunctionForTethering(Context context, UsbManager usbManager, boolean enable) {
        if (!HwDeliverInfo.isIOTVersion() || !SystemProperties.getBoolean("ro.config.persist_usb_tethering", VDBG)) {
            return VDBG;
        }
        Log.d(TAG, "tethering setCurrentFunction rndis,serial " + enable);
        if (enable) {
            if (usbManager != null) {
                usbManager.setCurrentFunction("rndis,serial");
            }
            Secure.putInt(context.getContentResolver(), "usb_tethering_on", 1);
        } else {
            Secure.putInt(context.getContentResolver(), "usb_tethering_on", 0);
        }
        return true;
    }

    public void captivePortalCheckCompleted(Context context, boolean isCaptivePortal) {
        if (!isCaptivePortal && 1 == System.getInt(context.getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0)) {
            System.putInt(context.getContentResolver(), WIFI_AP_MANUAL_CONNECT, 0);
            Log.d("HwConnectivityManagerImpl", "not portal ap manual connect");
        }
    }

    public void startBrowserOnClickNotification(Context context, String url) {
        Notification notification = new Notification();
        if (IS_CHINA) {
            String operator = TelephonyManager.getDefault().getNetworkOperator();
            if (!(operator == null || operator.length() == 0 || !operator.startsWith(COUNTRY_CODE_CN))) {
                url = HwNetworkPropertyChecker.CHINA_MAINLAND_MAIN_SERVER;
            }
        }
        Log.d("HwConnectivityManagerImpl", "startBrowserOnClickNotification url: " + url);
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        intent.setFlags(272629760);
        notification.contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
        try {
            intent.setClassName("com.android.browser", "com.android.browser.BrowserActivity");
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                Log.d("HwConnectivityManagerImpl", "default browser not exist..");
                notification.contentIntent.send();
            } catch (CanceledException e2) {
                Log.e("HwConnectivityManagerImpl", "Sending contentIntent failed: " + e2);
            }
        }
    }

    public NetworkMonitor createHwNetworkMonitor(Context context, Handler handler, NetworkAgentInfo nai, NetworkRequest defaultRequest) {
        return new HwNetworkMonitor(context, handler, nai, defaultRequest);
    }

    public Network getNetworkForTypeWifi() {
        if (this.mHwConnectivityService != null) {
            return this.mHwConnectivityService.getNetworkForTypeWifi();
        }
        Log.e("HwConnectivityManagerImpl", "getNetworkForTypeWifi, mHwConnectivityService = " + this.mHwConnectivityService);
        return null;
    }

    public NetworkInfo getNetworkInfoForWifi() {
        if (this.mHwConnectivityService != null) {
            return this.mHwConnectivityService.getNetworkInfoForWifi();
        }
        Log.e("HwConnectivityManagerImpl", "getNetworkInfoForWifi, mHwConnectivityService = " + this.mHwConnectivityService);
        return null;
    }

    public boolean isP2pTether(String iface) {
        if (iface == null) {
            return VDBG;
        }
        return !iface.startsWith(P2P_TETHER_IFAC) ? iface.startsWith(P2P_TETHER_IFAC_110x) : true;
    }

    public void stopP2pTether(Context context) {
        if (context != null) {
            Channel channel = null;
            ActionListener mWifiP2pBridgeCreateListener = new ActionListener() {
                public void onSuccess() {
                    Log.d(HwConnectivityManagerImpl.TAG, " Stop p2p tether success");
                }

                public void onFailure(int reason) {
                    Log.e(HwConnectivityManagerImpl.TAG, " Stop p2p tether fail:" + reason);
                }
            };
            WifiP2pManager wifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
            if (wifiP2pManager != null) {
                channel = wifiP2pManager.initialize(context, context.getMainLooper(), null);
            }
            if (channel != null) {
                wifiP2pManager.removeGroup(channel, mWifiP2pBridgeCreateListener);
            }
        }
    }
}
