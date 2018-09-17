package com.android.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.NetworkScorerAppData;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkScorerAppManager {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "NetworkScorerAppManager";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private final Context mContext;
    private final SettingsFacade mSettingsFacade;

    public static class SettingsFacade {
        public boolean putString(Context context, String name, String value) {
            return Global.putString(context.getContentResolver(), name, value);
        }

        public String getString(Context context, String name) {
            return Global.getString(context.getContentResolver(), name);
        }

        public boolean putInt(Context context, String name, int value) {
            return Global.putInt(context.getContentResolver(), name, value);
        }

        public int getInt(Context context, String name, int defaultValue) {
            return Global.getInt(context.getContentResolver(), name, defaultValue);
        }
    }

    public NetworkScorerAppManager(Context context) {
        this(context, new SettingsFacade());
    }

    public NetworkScorerAppManager(Context context, SettingsFacade settingsFacade) {
        this.mContext = context;
        this.mSettingsFacade = settingsFacade;
    }

    public List<NetworkScorerAppData> getAllValidScorers() {
        if (VERBOSE) {
            Log.v(TAG, "getAllValidScorers()");
        }
        PackageManager pm = this.mContext.getPackageManager();
        Intent serviceIntent = new Intent("android.net.action.RECOMMEND_NETWORKS");
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(serviceIntent, 128);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "Found 0 Services able to handle " + serviceIntent);
            }
            return Collections.emptyList();
        }
        List<NetworkScorerAppData> appDataList = new ArrayList();
        for (int i = 0; i < resolveInfos.size(); i++) {
            ServiceInfo serviceInfo = ((ResolveInfo) resolveInfos.get(i)).serviceInfo;
            if (hasPermissions(serviceInfo.packageName)) {
                if (VERBOSE) {
                    Log.v(TAG, serviceInfo.packageName + " is a valid scorer/recommender.");
                }
                appDataList.add(new NetworkScorerAppData(serviceInfo.applicationInfo.uid, new ComponentName(serviceInfo.packageName, serviceInfo.name), getRecommendationServiceLabel(serviceInfo, pm), findUseOpenWifiNetworksActivity(serviceInfo), getNetworkAvailableNotificationChannelId(serviceInfo)));
            } else if (VERBOSE) {
                Log.v(TAG, serviceInfo.packageName + " is NOT a valid scorer/recommender.");
            }
        }
        return appDataList;
    }

    private String getRecommendationServiceLabel(ServiceInfo serviceInfo, PackageManager pm) {
        String str = null;
        if (serviceInfo.metaData != null) {
            String label = serviceInfo.metaData.getString("android.net.scoring.recommendation_service_label");
            if (!TextUtils.isEmpty(label)) {
                return label;
            }
        }
        CharSequence label2 = serviceInfo.loadLabel(pm);
        if (label2 != null) {
            str = label2.toString();
        }
        return str;
    }

    private ComponentName findUseOpenWifiNetworksActivity(ServiceInfo serviceInfo) {
        if (serviceInfo.metaData == null) {
            if (DEBUG) {
                Log.d(TAG, "No metadata found on " + serviceInfo.getComponentName());
            }
            return null;
        }
        String useOpenWifiPackage = serviceInfo.metaData.getString("android.net.wifi.use_open_wifi_package");
        if (TextUtils.isEmpty(useOpenWifiPackage)) {
            if (DEBUG) {
                Log.d(TAG, "No use_open_wifi_package metadata found on " + serviceInfo.getComponentName());
            }
            return null;
        }
        Intent enableUseOpenWifiIntent = new Intent("android.net.scoring.CUSTOM_ENABLE").setPackage(useOpenWifiPackage);
        ResolveInfo resolveActivityInfo = this.mContext.getPackageManager().resolveActivity(enableUseOpenWifiIntent, 0);
        if (VERBOSE) {
            Log.d(TAG, "Resolved " + enableUseOpenWifiIntent + " to " + resolveActivityInfo);
        }
        if (resolveActivityInfo == null || resolveActivityInfo.activityInfo == null) {
            return null;
        }
        return resolveActivityInfo.activityInfo.getComponentName();
    }

    private static String getNetworkAvailableNotificationChannelId(ServiceInfo serviceInfo) {
        if (serviceInfo.metaData != null) {
            return serviceInfo.metaData.getString("android.net.wifi.notification_channel_id_network_available");
        }
        if (DEBUG) {
            Log.d(TAG, "No metadata found on " + serviceInfo.getComponentName());
        }
        return null;
    }

    public NetworkScorerAppData getActiveScorer() {
        if (getNetworkRecommendationsEnabledSetting() == -1) {
            return null;
        }
        return getScorer(getNetworkRecommendationsPackage());
    }

    private NetworkScorerAppData getScorer(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        List<NetworkScorerAppData> apps = getAllValidScorers();
        for (int i = 0; i < apps.size(); i++) {
            NetworkScorerAppData app = (NetworkScorerAppData) apps.get(i);
            if (app.getRecommendationServicePackageName().equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    private boolean hasPermissions(String packageName) {
        if (this.mContext.getPackageManager().checkPermission("android.permission.SCORE_NETWORKS", packageName) == 0) {
            return true;
        }
        return false;
    }

    public boolean setActiveScorer(String packageName) {
        String oldPackageName = getNetworkRecommendationsPackage();
        if (TextUtils.equals(oldPackageName, packageName)) {
            return true;
        }
        if (TextUtils.isEmpty(packageName)) {
            Log.i(TAG, "Network scorer forced off, was: " + oldPackageName);
            setNetworkRecommendationsPackage(null);
            setNetworkRecommendationsEnabledSetting(-1);
            return true;
        } else if (getScorer(packageName) != null) {
            Log.i(TAG, "Changing network scorer from " + oldPackageName + " to " + packageName);
            setNetworkRecommendationsPackage(packageName);
            setNetworkRecommendationsEnabledSetting(1);
            return true;
        } else {
            Log.w(TAG, "Requested network scorer is not valid: " + packageName);
            return false;
        }
    }

    public void updateState() {
        if (getNetworkRecommendationsEnabledSetting() == -1) {
            if (DEBUG) {
                Log.d(TAG, "Recommendations forced off.");
            }
            return;
        }
        String currentPackageName = getNetworkRecommendationsPackage();
        if (getScorer(currentPackageName) != null) {
            if (VERBOSE) {
                Log.v(TAG, currentPackageName + " is the active scorer.");
            }
            setNetworkRecommendationsEnabledSetting(1);
            return;
        }
        String defaultPackageName = getDefaultPackageSetting();
        if (!TextUtils.equals(currentPackageName, defaultPackageName)) {
            setNetworkRecommendationsPackage(defaultPackageName);
            if (DEBUG) {
                Log.d(TAG, "Defaulted the network recommendations app to: " + defaultPackageName);
            }
            if (getScorer(defaultPackageName) != null) {
                if (DEBUG) {
                    Log.d(TAG, defaultPackageName + " is now the active scorer.");
                }
                setNetworkRecommendationsEnabledSetting(1);
            } else {
                if (DEBUG) {
                    Log.d(TAG, defaultPackageName + " is not an active scorer.");
                }
                setNetworkRecommendationsEnabledSetting(0);
            }
        }
    }

    public void migrateNetworkScorerAppSettingIfNeeded() {
        String scorerAppPkgNameSetting = this.mSettingsFacade.getString(this.mContext, "network_scorer_app");
        if (!TextUtils.isEmpty(scorerAppPkgNameSetting)) {
            NetworkScorerAppData currentAppData = getActiveScorer();
            if (currentAppData != null) {
                if (DEBUG) {
                    Log.d(TAG, "Migrating Settings.Global.NETWORK_SCORER_APP (" + scorerAppPkgNameSetting + ")...");
                }
                ComponentName enableUseOpenWifiActivity = currentAppData.getEnableUseOpenWifiActivity();
                if (TextUtils.isEmpty(this.mSettingsFacade.getString(this.mContext, "use_open_wifi_package")) && enableUseOpenWifiActivity != null && scorerAppPkgNameSetting.equals(enableUseOpenWifiActivity.getPackageName())) {
                    this.mSettingsFacade.putString(this.mContext, "use_open_wifi_package", scorerAppPkgNameSetting);
                    if (DEBUG) {
                        Log.d(TAG, "Settings.Global.USE_OPEN_WIFI_PACKAGE set to '" + scorerAppPkgNameSetting + "'.");
                    }
                }
                this.mSettingsFacade.putString(this.mContext, "network_scorer_app", null);
                if (DEBUG) {
                    Log.d(TAG, "Settings.Global.NETWORK_SCORER_APP migration complete.");
                    Log.d(TAG, "Settings.Global.USE_OPEN_WIFI_PACKAGE is: '" + this.mSettingsFacade.getString(this.mContext, "use_open_wifi_package") + "'.");
                }
            }
        }
    }

    private String getDefaultPackageSetting() {
        return this.mContext.getResources().getString(17039770);
    }

    private String getNetworkRecommendationsPackage() {
        return this.mSettingsFacade.getString(this.mContext, "network_recommendations_package");
    }

    private void setNetworkRecommendationsPackage(String packageName) {
        this.mSettingsFacade.putString(this.mContext, "network_recommendations_package", packageName);
    }

    private int getNetworkRecommendationsEnabledSetting() {
        return this.mSettingsFacade.getInt(this.mContext, "network_recommendations_enabled", 0);
    }

    private void setNetworkRecommendationsEnabledSetting(int value) {
        this.mSettingsFacade.putInt(this.mContext, "network_recommendations_enabled", value);
    }
}
