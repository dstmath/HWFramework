package com.android.server;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.net.NetworkScorerAppData;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@VisibleForTesting
public class NetworkScorerAppManager {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String TAG = "NetworkScorerAppManager";
    private static final boolean VERBOSE = Log.isLoggable(TAG, 2);
    private final Context mContext;
    private final SettingsFacade mSettingsFacade;

    public NetworkScorerAppManager(Context context) {
        this(context, new SettingsFacade());
    }

    @VisibleForTesting
    public NetworkScorerAppManager(Context context, SettingsFacade settingsFacade) {
        this.mContext = context;
        this.mSettingsFacade = settingsFacade;
    }

    @VisibleForTesting
    public List<NetworkScorerAppData> getAllValidScorers() {
        NetworkScorerAppManager networkScorerAppManager = this;
        if (VERBOSE) {
            Log.v(TAG, "getAllValidScorers()");
        }
        PackageManager pm = networkScorerAppManager.mContext.getPackageManager();
        Intent serviceIntent = new Intent("android.net.action.RECOMMEND_NETWORKS");
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(serviceIntent, 128);
        if (resolveInfos == null || resolveInfos.isEmpty()) {
            if (DEBUG) {
                Log.d(TAG, "Found 0 Services able to handle " + serviceIntent);
            }
            return Collections.emptyList();
        }
        List<NetworkScorerAppData> appDataList = new ArrayList<>();
        int i = 0;
        while (i < resolveInfos.size()) {
            ServiceInfo serviceInfo = resolveInfos.get(i).serviceInfo;
            if (networkScorerAppManager.hasPermissions(serviceInfo.applicationInfo.uid, serviceInfo.packageName)) {
                if (VERBOSE) {
                    Log.v(TAG, serviceInfo.packageName + " is a valid scorer/recommender.");
                }
                appDataList.add(new NetworkScorerAppData(serviceInfo.applicationInfo.uid, new ComponentName(serviceInfo.packageName, serviceInfo.name), networkScorerAppManager.getRecommendationServiceLabel(serviceInfo, pm), networkScorerAppManager.findUseOpenWifiNetworksActivity(serviceInfo), getNetworkAvailableNotificationChannelId(serviceInfo)));
            } else if (VERBOSE) {
                Log.v(TAG, serviceInfo.packageName + " is NOT a valid scorer/recommender.");
            }
            i++;
            networkScorerAppManager = this;
        }
        return appDataList;
    }

    private String getRecommendationServiceLabel(ServiceInfo serviceInfo, PackageManager pm) {
        if (serviceInfo.metaData != null) {
            String label = serviceInfo.metaData.getString("android.net.scoring.recommendation_service_label");
            if (!TextUtils.isEmpty(label)) {
                return label;
            }
        }
        CharSequence label2 = serviceInfo.loadLabel(pm);
        if (label2 == null) {
            return null;
        }
        return label2.toString();
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
        if (!DEBUG) {
            return null;
        }
        Log.d(TAG, "No metadata found on " + serviceInfo.getComponentName());
        return null;
    }

    @VisibleForTesting
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
            NetworkScorerAppData app = apps.get(i);
            if (app.getRecommendationServicePackageName().equals(packageName)) {
                return app;
            }
        }
        return null;
    }

    private boolean hasPermissions(int uid, String packageName) {
        return hasScoreNetworksPermission(packageName) && canAccessLocation(uid, packageName);
    }

    private boolean hasScoreNetworksPermission(String packageName) {
        return this.mContext.getPackageManager().checkPermission("android.permission.SCORE_NETWORKS", packageName) == 0;
    }

    private boolean canAccessLocation(int uid, String packageName) {
        PackageManager pm = this.mContext.getPackageManager();
        AppOpsManager appOpsManager = (AppOpsManager) this.mContext.getSystemService("appops");
        if (isLocationModeEnabled() && pm.checkPermission("android.permission.ACCESS_COARSE_LOCATION", packageName) == 0 && appOpsManager.noteOp(0, uid, packageName) == 0) {
            return true;
        }
        return false;
    }

    private boolean isLocationModeEnabled() {
        return this.mSettingsFacade.getSecureInt(this.mContext, "location_mode", 0) != 0;
    }

    @VisibleForTesting
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

    @VisibleForTesting
    public void updateState() {
        if (getNetworkRecommendationsEnabledSetting() != -1) {
            String currentPackageName = getNetworkRecommendationsPackage();
            if (getScorer(currentPackageName) != null) {
                if (VERBOSE) {
                    Log.v(TAG, currentPackageName + " is the active scorer.");
                }
                setNetworkRecommendationsEnabledSetting(1);
                return;
            }
            int newEnabledSetting = 0;
            String defaultPackageName = getDefaultPackageSetting();
            if (!TextUtils.equals(currentPackageName, defaultPackageName) && getScorer(defaultPackageName) != null) {
                if (DEBUG) {
                    Log.d(TAG, "Defaulting the network recommendations app to: " + defaultPackageName);
                }
                setNetworkRecommendationsPackage(defaultPackageName);
                newEnabledSetting = 1;
            }
            setNetworkRecommendationsEnabledSetting(newEnabledSetting);
        } else if (DEBUG) {
            Log.d(TAG, "Recommendations forced off.");
        }
    }

    @VisibleForTesting
    public void migrateNetworkScorerAppSettingIfNeeded() {
        NetworkScorerAppData currentAppData;
        String scorerAppPkgNameSetting = this.mSettingsFacade.getString(this.mContext, "network_scorer_app");
        if (!TextUtils.isEmpty(scorerAppPkgNameSetting) && (currentAppData = getActiveScorer()) != null) {
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
                String setting = this.mSettingsFacade.getString(this.mContext, "use_open_wifi_package");
                Log.d(TAG, "Settings.Global.USE_OPEN_WIFI_PACKAGE is: '" + setting + "'.");
            }
        }
    }

    private String getDefaultPackageSetting() {
        return this.mContext.getResources().getString(17039825);
    }

    private String getNetworkRecommendationsPackage() {
        return this.mSettingsFacade.getString(this.mContext, "network_recommendations_package");
    }

    private void setNetworkRecommendationsPackage(String packageName) {
        this.mSettingsFacade.putString(this.mContext, "network_recommendations_package", packageName);
        if (VERBOSE) {
            Log.d(TAG, "network_recommendations_package set to " + packageName);
        }
    }

    private int getNetworkRecommendationsEnabledSetting() {
        return this.mSettingsFacade.getInt(this.mContext, "network_recommendations_enabled", 0);
    }

    private void setNetworkRecommendationsEnabledSetting(int value) {
        this.mSettingsFacade.putInt(this.mContext, "network_recommendations_enabled", value);
        if (VERBOSE) {
            Log.d(TAG, "network_recommendations_enabled set to " + value);
        }
    }

    public static class SettingsFacade {
        public boolean putString(Context context, String name, String value) {
            return Settings.Global.putString(context.getContentResolver(), name, value);
        }

        public String getString(Context context, String name) {
            return Settings.Global.getString(context.getContentResolver(), name);
        }

        public boolean putInt(Context context, String name, int value) {
            return Settings.Global.putInt(context.getContentResolver(), name, value);
        }

        public int getInt(Context context, String name, int defaultValue) {
            return Settings.Global.getInt(context.getContentResolver(), name, defaultValue);
        }

        public int getSecureInt(Context context, String name, int defaultValue) {
            return Settings.Secure.getInt(context.getContentResolver(), name, defaultValue);
        }
    }
}
