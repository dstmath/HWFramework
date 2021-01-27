package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.hwUtil.StringUtilEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarrierNetworkConfig {
    private static final int CONFIG_ELEMENT_SIZE = 2;
    private static final Uri CONTENT_URI = Uri.parse("content://carrier_information/carrier");
    private static final int EAP_TYPE_INDEX = 1;
    private static final int ENCODED_SSID_INDEX = 0;
    private static final String NETWORK_CONFIG_SEPARATOR = ",";
    private static final String TAG = "CarrierNetworkConfig";
    private final Map<String, NetworkInfo> mCarrierNetworkMap = new HashMap();
    private boolean mDbg = false;
    private boolean mIsCarrierImsiEncryptionInfoAvailable = false;
    private ImsiEncryptionInfo mLastImsiEncryptionInfo = null;

    public void enableVerboseLogging(int verbose) {
        this.mDbg = verbose > 0;
    }

    public CarrierNetworkConfig(final Context context, Looper looper, FrameworkFacade framework) {
        updateNetworkConfig(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            /* class com.android.server.wifi.CarrierNetworkConfig.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                CarrierNetworkConfig.this.updateNetworkConfig(context);
            }
        }, filter);
        framework.registerContentObserver(context, CONTENT_URI, false, new ContentObserver(new Handler(looper)) {
            /* class com.android.server.wifi.CarrierNetworkConfig.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                CarrierNetworkConfig.this.updateNetworkConfig(context);
            }
        });
    }

    public boolean isCarrierNetwork(String ssid) {
        return this.mCarrierNetworkMap.containsKey(ssid);
    }

    public int getNetworkEapType(String ssid) {
        NetworkInfo info = this.mCarrierNetworkMap.get(ssid);
        if (info == null) {
            return -1;
        }
        return info.mEapType;
    }

    public String getCarrierName(String ssid) {
        NetworkInfo info = this.mCarrierNetworkMap.get(ssid);
        if (info == null) {
            return null;
        }
        return info.mCarrierName;
    }

    public boolean isCarrierEncryptionInfoAvailable() {
        return this.mIsCarrierImsiEncryptionInfoAvailable;
    }

    private boolean verifyCarrierImsiEncryptionInfoIsAvailable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
        if (telephonyManager == null) {
            return false;
        }
        try {
            this.mLastImsiEncryptionInfo = telephonyManager.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId()).getCarrierInfoForImsiEncryption(2);
            if (this.mLastImsiEncryptionInfo == null) {
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to get imsi encryption info: " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static class NetworkInfo {
        final String mCarrierName;
        final int mEapType;

        NetworkInfo(int eapType, String carrierName) {
            this.mEapType = eapType;
            this.mCarrierName = carrierName;
        }

        public String toString() {
            StringBuffer stringBuffer = new StringBuffer("NetworkInfo: eap=");
            stringBuffer.append(this.mEapType);
            stringBuffer.append(", carrier=");
            stringBuffer.append(this.mCarrierName);
            return stringBuffer.toString();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkConfig(Context context) {
        SubscriptionManager subscriptionManager;
        List<SubscriptionInfo> subInfoList;
        this.mIsCarrierImsiEncryptionInfoAvailable = verifyCarrierImsiEncryptionInfoIsAvailable(context);
        this.mCarrierNetworkMap.clear();
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (!(carrierConfigManager == null || (subscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service")) == null || (subInfoList = subscriptionManager.getActiveSubscriptionInfoList()) == null)) {
            for (SubscriptionInfo subInfo : subInfoList) {
                CharSequence displayNameCs = subInfo.getDisplayName();
                processNetworkConfig(carrierConfigManager.getConfigForSubId(subInfo.getSubscriptionId()), displayNameCs == null ? "" : displayNameCs.toString());
            }
        }
    }

    private void processNetworkConfig(PersistableBundle carrierConfig, String carrierName) {
        if (carrierConfig != null) {
            String[] networkConfigs = carrierConfig.getStringArray("carrier_wifi_string_array");
            if (this.mDbg) {
                Log.v(TAG, "processNetworkConfig: networkConfigs=" + Arrays.deepToString(networkConfigs));
            }
            if (networkConfigs != null) {
                for (String networkConfig : networkConfigs) {
                    String[] configArr = networkConfig.split(NETWORK_CONFIG_SEPARATOR);
                    if (configArr.length != 2) {
                        Log.e(TAG, "Ignore invalid config: " + networkConfig);
                    } else {
                        try {
                            String ssid = new String(Base64.decode(configArr[0], 2));
                            int eapType = parseEapType(Integer.parseInt(configArr[1]));
                            if (eapType == -1) {
                                Log.e(TAG, "Invalid EAP type: " + configArr[1]);
                            } else {
                                this.mCarrierNetworkMap.put(ssid, new NetworkInfo(eapType, carrierName));
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Failed to parse EAP type: '" + configArr[1] + "' " + e.getMessage());
                        } catch (IllegalArgumentException e2) {
                            Log.e(TAG, "Failed to decode SSID: '" + StringUtilEx.safeDisplaySsid(configArr[0]) + "' " + e2.getMessage());
                        }
                    }
                }
            }
        }
    }

    private static int parseEapType(int eapType) {
        if (eapType == 18) {
            return 4;
        }
        if (eapType == 23) {
            return 5;
        }
        if (eapType == 50) {
            return 6;
        }
        return -1;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CarrierNetworkConfig: ");
        pw.println("mCarrierNetworkMap=" + this.mCarrierNetworkMap);
        pw.println("mIsCarrierImsiEncryptionInfoAvailable=" + this.mIsCarrierImsiEncryptionInfoAvailable);
        pw.println("mLastImsiEncryptionInfo=" + this.mLastImsiEncryptionInfo);
    }
}
