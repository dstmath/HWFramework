package android.net;

import android.content.Context;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.DebugUtils;
import java.util.Calendar;
import java.util.TimeZone;

public class NetworkPolicyManager {
    private static final boolean ALLOW_PLATFORM_APP_POLICY = true;
    public static final String EXTRA_NETWORK_TEMPLATE = "android.net.NETWORK_TEMPLATE";
    public static final int FIREWALL_CHAIN_DOZABLE = 1;
    public static final String FIREWALL_CHAIN_NAME_DOZABLE = "dozable";
    public static final String FIREWALL_CHAIN_NAME_NONE = "none";
    public static final String FIREWALL_CHAIN_NAME_POWERSAVE = "powersave";
    public static final String FIREWALL_CHAIN_NAME_STANDBY = "standby";
    public static final int FIREWALL_CHAIN_NONE = 0;
    public static final int FIREWALL_CHAIN_POWERSAVE = 3;
    public static final int FIREWALL_CHAIN_STANDBY = 2;
    public static final int FIREWALL_RULE_ALLOW = 1;
    public static final int FIREWALL_RULE_DEFAULT = 0;
    public static final int FIREWALL_RULE_DENY = 2;
    public static final int FIREWALL_TYPE_BLACKLIST = 1;
    public static final int FIREWALL_TYPE_WHITELIST = 0;
    public static final int MASK_ALL_NETWORKS = 240;
    public static final int MASK_METERED_NETWORKS = 15;
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
    private INetworkPolicyManager mService;

    public NetworkPolicyManager(Context context, INetworkPolicyManager service) {
        if (service == null) {
            throw new IllegalArgumentException("missing INetworkPolicyManager");
        }
        this.mContext = context;
        this.mService = service;
    }

    public static NetworkPolicyManager from(Context context) {
        return (NetworkPolicyManager) context.getSystemService(Context.NETWORK_POLICY_SERVICE);
    }

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

    public int getUidPolicy(int uid) {
        try {
            return this.mService.getUidPolicy(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int[] getUidsWithPolicy(int policy) {
        try {
            return this.mService.getUidsWithPolicy(policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void registerListener(INetworkPolicyListener listener) {
        try {
            this.mService.registerListener(listener);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public NetworkPolicy[] getNetworkPolicies() {
        try {
            return this.mService.getNetworkPolicies(this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setRestrictBackground(boolean restrictBackground) {
        try {
            this.mService.setRestrictBackground(restrictBackground);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

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

    public static long computeLastCycleBoundary(long currentTime, NetworkPolicy policy) {
        if (policy.cycleDay == -1) {
            throw new IllegalArgumentException("Unable to compute boundary without cycleDay");
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(policy.cycleTimezone));
        cal.setTimeInMillis(currentTime);
        snapToCycleDay(cal, policy.cycleDay);
        if (cal.getTimeInMillis() >= currentTime) {
            cal.set(5, 1);
            cal.add(2, -1);
            snapToCycleDay(cal, policy.cycleDay);
        }
        return cal.getTimeInMillis();
    }

    public static long computeNextCycleBoundary(long currentTime, NetworkPolicy policy) {
        if (policy.cycleDay == -1) {
            throw new IllegalArgumentException("Unable to compute boundary without cycleDay");
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(policy.cycleTimezone));
        cal.setTimeInMillis(currentTime);
        snapToCycleDay(cal, policy.cycleDay);
        if (cal.getTimeInMillis() <= currentTime) {
            cal.set(5, 1);
            cal.add(2, 1);
            snapToCycleDay(cal, policy.cycleDay);
        }
        return cal.getTimeInMillis();
    }

    public static void snapToCycleDay(Calendar cal, int cycleDay) {
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        if (cycleDay > cal.getActualMaximum(5)) {
            cal.add(2, 1);
            cal.set(5, 1);
            cal.set(11, 0);
            cal.set(12, 0);
            cal.set(13, 0);
            cal.add(13, -1);
            return;
        }
        cal.set(5, cycleDay);
    }

    @Deprecated
    public static boolean isUidValidForPolicy(Context context, int uid) {
        if (UserHandle.isApp(uid)) {
            return true;
        }
        return false;
    }

    public static String uidRulesToString(int uidRules) {
        StringBuilder string = new StringBuilder().append(uidRules).append(" (");
        if (uidRules == 0) {
            string.append("NONE");
        } else {
            string.append(DebugUtils.flagsToString(NetworkPolicyManager.class, "RULE_", uidRules));
        }
        string.append(")");
        return string.toString();
    }

    public static String uidPoliciesToString(int uidPolicies) {
        StringBuilder string = new StringBuilder().append(uidPolicies).append(" (");
        if (uidPolicies == 0) {
            string.append("NONE");
        } else {
            string.append(DebugUtils.flagsToString(NetworkPolicyManager.class, "POLICY_", uidPolicies));
        }
        string.append(")");
        return string.toString();
    }

    public static boolean isProcStateAllowedWhileIdleOrPowerSaveMode(int procState) {
        return procState <= 4;
    }

    public static boolean isProcStateAllowedWhileOnRestrictBackground(int procState) {
        return procState <= 4;
    }
}
