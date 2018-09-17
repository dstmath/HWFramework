package com.android.server.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Binder;
import android.os.Environment;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class WifiApConfigStore {
    private static final int AP_CONFIG_FILE_VERSION = 2;
    private static final String DEFAULT_AP_CONFIG_FILE = (Environment.getDataDirectory() + "/misc/wifi/softap.conf");
    private static final int RAND_SSID_INT_MAX = 9999;
    private static final int RAND_SSID_INT_MIN = 1000;
    private static final String TAG = "WifiApConfigStore";
    private static final int WAPI_CERT_CONST_VALUE_IN_L = 5;
    private static final int WAPI_PSK_CONST_VALUE_IN_L = 4;
    private static final int WPA2_PSK_CONST_VALUE_IN_L = 6;
    private static String mMarketingName = SystemProperties.get("ro.config.marketing_name");
    private ArrayList<Integer> mAllowed2GChannel;
    private final String mApConfigFile;
    private final BackupManagerProxy mBackupManagerProxy;
    private final Context mContext;
    private WifiConfiguration mWifiApConfig;

    WifiApConfigStore(Context context, BackupManagerProxy backupManagerProxy) {
        this(context, backupManagerProxy, DEFAULT_AP_CONFIG_FILE);
    }

    WifiApConfigStore(Context context, BackupManagerProxy backupManagerProxy, String apConfigFile) {
        this.mWifiApConfig = null;
        this.mAllowed2GChannel = null;
        this.mContext = context;
        this.mBackupManagerProxy = backupManagerProxy;
        this.mApConfigFile = apConfigFile;
        String ap2GChannelListStr = this.mContext.getResources().getString(17039814);
        Log.d(TAG, "2G band allowed channels are:" + ap2GChannelListStr);
        if (ap2GChannelListStr != null) {
            this.mAllowed2GChannel = new ArrayList();
            for (String tmp : ap2GChannelListStr.split(",")) {
                this.mAllowed2GChannel.add(Integer.valueOf(Integer.parseInt(tmp)));
            }
        }
        this.mWifiApConfig = loadApConfiguration(this.mApConfigFile);
        if (this.mWifiApConfig == null) {
            Log.d(TAG, "Fallback to use default AP configuration");
            this.mWifiApConfig = getDefaultApConfiguration();
            writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        }
    }

    public synchronized WifiConfiguration getApConfiguration() {
        return this.mWifiApConfig;
    }

    public synchronized void setApConfiguration(WifiConfiguration config) {
        if (config == null) {
            this.mWifiApConfig = getDefaultApConfiguration();
        } else {
            this.mWifiApConfig = config;
        }
        writeApConfiguration(this.mApConfigFile, this.mWifiApConfig);
        if (!TextUtils.isEmpty(mMarketingName)) {
            long ident = Binder.clearCallingIdentity();
            try {
                if (mMarketingName.equals(this.mWifiApConfig.SSID)) {
                    System.putInt(this.mContext.getContentResolver(), "softap_name_changed", 0);
                } else {
                    System.putInt(this.mContext.getContentResolver(), "softap_name_changed", 1);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
        this.mBackupManagerProxy.notifyDataChanged();
    }

    public ArrayList<Integer> getAllowed2GChannel() {
        return this.mAllowed2GChannel;
    }

    /* JADX WARNING: Removed duplicated region for block: B:54:0x0104 A:{SYNTHETIC, Splitter: B:54:0x0104} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00c5 A:{SYNTHETIC, Splitter: B:46:0x00c5} */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0104 A:{SYNTHETIC, Splitter: B:54:0x0104} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private WifiConfiguration loadApConfiguration(String filename) {
        IOException e;
        WifiConfiguration wifiConfiguration;
        Throwable th;
        DataInputStream in = null;
        try {
            DataInputStream in2;
            WifiConfiguration config = new WifiConfiguration();
            try {
                in2 = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
            } catch (IOException e2) {
                e = e2;
                try {
                    Log.e(TAG, "Error reading hotspot configuration " + e);
                    wifiConfiguration = null;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "Error closing hotspot configuration during read" + e3);
                        }
                    }
                    return wifiConfiguration;
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e32) {
                            Log.e(TAG, "Error closing hotspot configuration during read" + e32);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                if (in != null) {
                }
                throw th;
            }
            long ident;
            try {
                int version = in2.readInt();
                if (version == 1 || version == 2) {
                    config.SSID = in2.readUTF();
                    ident = Binder.clearCallingIdentity();
                    if (!TextUtils.isEmpty(mMarketingName) && System.getInt(this.mContext.getContentResolver(), "softap_name_changed", 0) == 0) {
                        config.SSID = mMarketingName;
                    }
                    Binder.restoreCallingIdentity(ident);
                    if (version >= 2) {
                        config.apBand = in2.readInt();
                        config.apChannel = in2.readInt();
                    }
                    int authType = mapApAuth(in2.readInt(), version);
                    config.allowedKeyManagement.set(authType);
                    if (authType != 0) {
                        config.preSharedKey = in2.readUTF();
                    }
                    if (in2 != null) {
                        try {
                            in2.close();
                        } catch (IOException e322) {
                            Log.e(TAG, "Error closing hotspot configuration during read" + e322);
                        }
                    }
                    wifiConfiguration = config;
                    return wifiConfiguration;
                }
                Log.e(TAG, "Bad version on hotspot configuration file");
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (IOException e3222) {
                        Log.e(TAG, "Error closing hotspot configuration during read" + e3222);
                    }
                }
                return null;
            } catch (IOException e4) {
                e3222 = e4;
                in = in2;
                wifiConfiguration = config;
            } catch (Throwable th4) {
                th = th4;
                in = in2;
                if (in != null) {
                }
                throw th;
            }
        } catch (IOException e5) {
            e3222 = e5;
            Log.e(TAG, "Error reading hotspot configuration " + e3222);
            wifiConfiguration = null;
            if (in != null) {
            }
            return wifiConfiguration;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0067 A:{SYNTHETIC, Splitter: B:27:0x0067} */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x007a A:{Catch:{ IOException -> 0x006d }} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x006c A:{SYNTHETIC, Splitter: B:30:0x006c} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void writeApConfiguration(String filename, WifiConfiguration config) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        DataOutputStream out = null;
        try {
            DataOutputStream out2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            try {
                out2.writeInt(2);
                out2.writeUTF(config.SSID);
                out2.writeInt(config.apBand);
                out2.writeInt(config.apChannel);
                int authType = config.getAuthType();
                out2.writeInt(authType);
                if (!(authType == 0 || config.preSharedKey == null)) {
                    out2.writeUTF(config.preSharedKey);
                }
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        out = out2;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                out = out2;
                if (out != null) {
                    try {
                        out.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Log.e(TAG, "Error writing hotspot configuration" + e);
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (out != null) {
            }
            if (th2 == null) {
            }
        }
    }

    private WifiConfiguration getDefaultApConfiguration() {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = this.mContext.getResources().getString(17041262) + "_" + getRandomIntForDefaultSsid();
        config.allowedKeyManagement.set(4);
        String randomUUID = UUID.randomUUID().toString();
        config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        config.SSID = HwWifiServiceFactory.getHwWifiServiceManager().getCustWifiApDefaultName(config);
        return config;
    }

    private static int getRandomIntForDefaultSsid() {
        return new Random().nextInt(9000) + RAND_SSID_INT_MIN;
    }

    private int mapApAuth(int softApAuthType, int softApversion) {
        if (softApversion == 2) {
            return softApAuthType;
        }
        String connectivity_chipType = SystemProperties.get("ro.connectivity.chiptype");
        Log.d(TAG, "connectivity_chipType = " + connectivity_chipType);
        if (softApversion != 1 || !"Qualcomm".equalsIgnoreCase(connectivity_chipType)) {
            return softApAuthType;
        }
        switch (softApAuthType) {
            case 4:
                return 8;
            case 5:
                return 9;
            case 6:
                return 4;
            default:
                return softApAuthType;
        }
    }

    public static WifiConfiguration generateLocalOnlyHotspotConfig(Context context) {
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = context.getResources().getString(17041247) + "_" + getRandomIntForDefaultSsid();
        config.allowedKeyManagement.set(4);
        config.networkId = -2;
        String randomUUID = UUID.randomUUID().toString();
        config.preSharedKey = randomUUID.substring(0, 8) + randomUUID.substring(9, 13);
        return config;
    }
}
