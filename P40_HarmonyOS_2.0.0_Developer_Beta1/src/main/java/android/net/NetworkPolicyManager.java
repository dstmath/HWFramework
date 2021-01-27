package android.net;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.net.INetworkPolicyListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.RemoteException;
import android.os.UserHandle;
import android.security.keystore.KeyProperties;
import android.util.DebugUtils;
import android.util.Pair;
import android.util.Range;
import java.time.ZonedDateTime;
import java.util.Iterator;

public class NetworkPolicyManager {
    private static final boolean ALLOW_PLATFORM_APP_POLICY = true;
    public static final String EXTRA_NETWORK_TEMPLATE = "android.net.NETWORK_TEMPLATE";
    public static final String FIREWALL_CHAIN_NAME_DOZABLE = "dozable";
    public static final String FIREWALL_CHAIN_NAME_NONE = "none";
    public static final String FIREWALL_CHAIN_NAME_POWERSAVE = "powersave";
    public static final String FIREWALL_CHAIN_NAME_STANDBY = "standby";
    public static final int FIREWALL_RULE_DEFAULT = 0;
    public static final int FOREGROUND_THRESHOLD_STATE = 6;
    public static final int MASK_ALL_NETWORKS = 240;
    public static final int MASK_METERED_NETWORKS = 15;
    public static final int OVERRIDE_CONGESTED = 2;
    public static final int OVERRIDE_UNMETERED = 1;
    public static final int POLICY_ALLOW_METERED_BACKGROUND = 4;
    public static final int POLICY_NONE = 0;
    public static final int POLICY_REJECT_METERED_BACKGROUND = 1;
    public static final int RULE_ALLOW_ALL = 32;
    public static final int RULE_ALLOW_METERED = 1;
    public static final int RULE_NONE = 0;
    public static final int RULE_REJECT_ALL = 64;
    public static final int RULE_REJECT_METERED = 4;
    public static final int RULE_TEMPORARY_ALLOW_METERED = 2;
    private final Context mContext;
    @UnsupportedAppUsage
    private INetworkPolicyManager mService;

    public NetworkPolicyManager(Context context, INetworkPolicyManager service) {
        if (service != null) {
            this.mContext = context;
            this.mService = service;
            return;
        }
        throw new IllegalArgumentException("missing INetworkPolicyManager");
    }

    @UnsupportedAppUsage
    public static NetworkPolicyManager from(Context context) {
        return (NetworkPolicyManager) context.getSystemService(Context.NETWORK_POLICY_SERVICE);
    }

    @UnsupportedAppUsage
    public void setUidPolicy(int uid, int policy) {
        try {
            this.mService.setUidPolicy(uid, policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void addUidPolicy(int uid, int policy) {
        try {
            this.mService.addUidPolicy(uid, policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeUidPolicy(int uid, int policy) {
        try {
            this.mService.removeUidPolicy(uid, policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public int getUidPolicy(int uid) {
        try {
            return this.mService.getUidPolicy(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public int[] getUidsWithPolicy(int policy) {
        try {
            return this.mService.getUidsWithPolicy(policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void registerListener(INetworkPolicyListener listener) {
        try {
            this.mService.registerListener(listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void unregisterListener(INetworkPolicyListener listener) {
        try {
            this.mService.unregisterListener(listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setNetworkPolicies(NetworkPolicy[] policies) {
        try {
            this.mService.setNetworkPolicies(policies);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public NetworkPolicy[] getNetworkPolicies() {
        try {
            return this.mService.getNetworkPolicies(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public void setRestrictBackground(boolean restrictBackground) {
        try {
            this.mService.setRestrictBackground(restrictBackground);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @UnsupportedAppUsage
    public boolean getRestrictBackground() {
        try {
            return this.mService.getRestrictBackground();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void factoryReset(String subscriber) {
        try {
            this.mService.factoryReset(subscriber);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Deprecated
    public static Iterator<Pair<ZonedDateTime, ZonedDateTime>> cycleIterator(NetworkPolicy policy) {
        final Iterator<Range<ZonedDateTime>> it = policy.cycleIterator();
        return new Iterator<Pair<ZonedDateTime, ZonedDateTime>>() {
            /* class android.net.NetworkPolicyManager.AnonymousClass1 */

            @Override // java.util.Iterator
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override // java.util.Iterator
            public Pair<ZonedDateTime, ZonedDateTime> next() {
                if (!hasNext()) {
                    return Pair.create(null, null);
                }
                Range<ZonedDateTime> r = (Range) it.next();
                return Pair.create(r.getLower(), r.getUpper());
            }
        };
    }

    @Deprecated
    public static boolean isUidValidForPolicy(Context context, int uid) {
        if (!UserHandle.isApp(uid)) {
            return false;
        }
        return true;
    }

    public static String uidRulesToString(int uidRules) {
        StringBuilder sb = new StringBuilder();
        sb.append(uidRules);
        StringBuilder string = sb.append(" (");
        if (uidRules == 0) {
            string.append(KeyProperties.DIGEST_NONE);
        } else {
            string.append(DebugUtils.flagsToString(NetworkPolicyManager.class, "RULE_", uidRules));
        }
        string.append(")");
        return string.toString();
    }

    public static String uidPoliciesToString(int uidPolicies) {
        StringBuilder sb = new StringBuilder();
        sb.append(uidPolicies);
        StringBuilder string = sb.append(" (");
        if (uidPolicies == 0) {
            string.append(KeyProperties.DIGEST_NONE);
        } else {
            string.append(DebugUtils.flagsToString(NetworkPolicyManager.class, "POLICY_", uidPolicies));
        }
        string.append(")");
        return string.toString();
    }

    public static boolean isProcStateAllowedWhileIdleOrPowerSaveMode(int procState) {
        return procState <= 6;
    }

    public static boolean isProcStateAllowedWhileOnRestrictBackground(int procState) {
        return procState <= 6;
    }

    public static String resolveNetworkId(WifiConfiguration config) {
        return WifiInfo.removeDoubleQuotes(config.isPasspoint() ? config.providerFriendlyName : config.SSID);
    }

    public static String resolveNetworkId(String ssid) {
        return WifiInfo.removeDoubleQuotes(ssid);
    }

    public static class Listener extends INetworkPolicyListener.Stub {
        @Override // android.net.INetworkPolicyListener
        public void onUidRulesChanged(int uid, int uidRules) {
        }

        @Override // android.net.INetworkPolicyListener
        public void onMeteredIfacesChanged(String[] meteredIfaces) {
        }

        @Override // android.net.INetworkPolicyListener
        public void onRestrictBackgroundChanged(boolean restrictBackground) {
        }

        @Override // android.net.INetworkPolicyListener
        public void onUidPoliciesChanged(int uid, int uidPolicies) {
        }

        @Override // android.net.INetworkPolicyListener
        public void onSubscriptionOverride(int subId, int overrideMask, int overrideValue) {
        }
    }
}
