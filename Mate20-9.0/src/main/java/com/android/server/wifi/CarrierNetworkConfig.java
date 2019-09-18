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
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
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
    private boolean mIsCarrierImsiEncryptionInfoAvailable = false;

    private static class NetworkInfo {
        final String mCarrierName;
        final int mEapType;

        NetworkInfo(int eapType, String carrierName) {
            this.mEapType = eapType;
            this.mCarrierName = carrierName;
        }
    }

    public CarrierNetworkConfig(final Context context, Looper looper, FrameworkFacade framework) {
        updateNetworkConfig(context);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.telephony.action.CARRIER_CONFIG_CHANGED");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                CarrierNetworkConfig.this.updateNetworkConfig(context);
            }
        }, filter);
        framework.registerContentObserver(context, CONTENT_URI, false, new ContentObserver(new Handler(looper)) {
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
            if (telephonyManager.getCarrierInfoForImsiEncryption(2) == null) {
                return false;
            }
            return true;
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to get imsi encryption info: " + e.getMessage());
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void updateNetworkConfig(Context context) {
        this.mIsCarrierImsiEncryptionInfoAvailable = verifyCarrierImsiEncryptionInfoIsAvailable(context);
        this.mCarrierNetworkMap.clear();
        CarrierConfigManager carrierConfigManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        if (carrierConfigManager != null) {
            SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService("telephony_subscription_service");
            if (subscriptionManager != null) {
                List<SubscriptionInfo> subInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                if (subInfoList != null) {
                    for (SubscriptionInfo subInfo : subInfoList) {
                        CharSequence displayName = subInfo.getDisplayName();
                        if (displayName != null) {
                            processNetworkConfig(carrierConfigManager.getConfigForSubId(subInfo.getSubscriptionId()), displayName.toString());
                        } else {
                            processNetworkConfig(carrierConfigManager.getConfigForSubId(subInfo.getSubscriptionId()), "");
                            Log.e(TAG, "displayName is null with SubscriptionId:" + subInfo.getSubscriptionId());
                        }
                    }
                }
            }
        }
    }

    private void processNetworkConfig(PersistableBundle carrierConfig, String carrierName) {
        if (carrierConfig != null) {
            String[] networkConfigs = carrierConfig.getStringArray("carrier_wifi_string_array");
            if (networkConfigs != null) {
                for (String networkConfig : networkConfigs) {
                    String[] configArr = networkConfig.split(NETWORK_CONFIG_SEPARATOR);
                    if (configArr.length != 2) {
                        Log.e(TAG, "Ignore invalid config: " + networkConfig);
                    } else {
                        try {
                            String ssid = new String(Base64.decode(configArr[0], 0));
                            int eapType = parseEapType(Integer.parseInt(configArr[1]));
                            if (eapType == -1) {
                                Log.e(TAG, "Invalid EAP type: " + configArr[1]);
                            } else {
                                this.mCarrierNetworkMap.put(ssid, new NetworkInfo(eapType, carrierName));
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Failed to parse EAP type: " + e.getMessage());
                        } catch (IllegalArgumentException e2) {
                            Log.e(TAG, "Failed to decode SSID: " + e2.getMessage());
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
}
