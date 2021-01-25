package com.android.server.net;

import android.net.Network;
import android.net.NetworkTemplate;
import android.telephony.SubscriptionPlan;
import java.util.Set;

public abstract class NetworkPolicyManagerInternal {
    public static final int QUOTA_TYPE_JOBS = 1;
    public static final int QUOTA_TYPE_MULTIPATH = 2;

    public abstract long getSubscriptionOpportunisticQuota(Network network, int i);

    public abstract SubscriptionPlan getSubscriptionPlan(Network network);

    public abstract SubscriptionPlan getSubscriptionPlan(NetworkTemplate networkTemplate);

    public abstract boolean isUidNetworkingBlocked(int i, String str);

    public abstract boolean isUidRestrictedOnMeteredNetworks(int i);

    public abstract void onAdminDataAvailable();

    public abstract void onTempPowerSaveWhitelistChange(int i, boolean z);

    public abstract void resetUserState(int i);

    public abstract void setAppIdleWhitelist(int i, boolean z);

    public abstract void setMeteredRestrictedPackages(Set<String> set, int i);

    public abstract void setMeteredRestrictedPackagesAsync(Set<String> set, int i);

    public static boolean isUidNetworkingBlocked(int uid, int uidRules, boolean isNetworkMetered, boolean isBackgroundRestricted) {
        return NetworkPolicyManagerService.isUidNetworkingBlockedInternal(uid, uidRules, isNetworkMetered, isBackgroundRestricted, null);
    }
}
