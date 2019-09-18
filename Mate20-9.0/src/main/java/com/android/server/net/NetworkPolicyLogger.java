package com.android.server.net;

import android.app.ActivityManager;
import android.net.NetworkPolicyManager;
import android.util.Log;
import android.util.Slog;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.RingBuffer;
import com.android.server.BatteryService;
import com.android.server.am.ProcessList;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

public class NetworkPolicyLogger {
    private static final int EVENT_APP_IDLE_STATE_CHANGED = 8;
    private static final int EVENT_DEVICE_IDLE_MODE_ENABLED = 7;
    private static final int EVENT_FIREWALL_CHAIN_ENABLED = 12;
    private static final int EVENT_METEREDNESS_CHANGED = 4;
    private static final int EVENT_NETWORK_BLOCKED = 1;
    private static final int EVENT_PAROLE_STATE_CHANGED = 9;
    private static final int EVENT_POLICIES_CHANGED = 3;
    private static final int EVENT_RESTRICT_BG_CHANGED = 6;
    private static final int EVENT_TEMP_POWER_SAVE_WL_CHANGED = 10;
    private static final int EVENT_TYPE_GENERIC = 0;
    private static final int EVENT_UID_FIREWALL_RULE_CHANGED = 11;
    private static final int EVENT_UID_STATE_CHANGED = 2;
    private static final int EVENT_UPDATE_METERED_RESTRICTED_PKGS = 13;
    private static final int EVENT_USER_STATE_REMOVED = 5;
    static final boolean LOGD = Log.isLoggable(TAG, 3);
    static final boolean LOGV = Log.isLoggable(TAG, 2);
    private static final int MAX_LOG_SIZE = (ActivityManager.isLowRamDeviceStatic() ? 20 : 50);
    private static final int MAX_NETWORK_BLOCKED_LOG_SIZE;
    static final int NTWK_ALLOWED_DEFAULT = 6;
    static final int NTWK_ALLOWED_NON_METERED = 1;
    static final int NTWK_ALLOWED_TMP_WHITELIST = 4;
    static final int NTWK_ALLOWED_WHITELIST = 3;
    static final int NTWK_BLOCKED_BG_RESTRICT = 5;
    static final int NTWK_BLOCKED_BLACKLIST = 2;
    static final int NTWK_BLOCKED_POWER = 0;
    static final String TAG = "NetworkPolicy";
    private final LogBuffer mEventsBuffer = new LogBuffer(MAX_LOG_SIZE);
    private final Object mLock = new Object();
    private final LogBuffer mNetworkBlockedBuffer = new LogBuffer(MAX_NETWORK_BLOCKED_LOG_SIZE);
    private final LogBuffer mUidStateChangeBuffer = new LogBuffer(MAX_LOG_SIZE);

    public static final class Data {
        boolean bfield1;
        boolean bfield2;
        int ifield1;
        int ifield2;
        int ifield3;
        long lfield1;
        String sfield1;
        long timeStamp;
        int type;

        public void reset() {
            this.sfield1 = null;
        }
    }

    private static final class LogBuffer extends RingBuffer<Data> {
        private static final Date sDate = new Date();
        private static final SimpleDateFormat sFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS");

        public LogBuffer(int capacity) {
            super(Data.class, capacity);
        }

        public void uidStateChanged(int uid, int procState, long procStateSeq) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 2;
                data.ifield1 = uid;
                data.ifield2 = procState;
                data.lfield1 = procStateSeq;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void event(String msg) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 0;
                data.sfield1 = msg;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void networkBlocked(int uid, int reason) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 1;
                data.ifield1 = uid;
                data.ifield2 = reason;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void uidPolicyChanged(int uid, int oldPolicy, int newPolicy) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 3;
                data.ifield1 = uid;
                data.ifield2 = oldPolicy;
                data.ifield3 = newPolicy;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void meterednessChanged(int netId, boolean newMetered) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 4;
                data.ifield1 = netId;
                data.bfield1 = newMetered;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void userRemoved(int userId) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 5;
                data.ifield1 = userId;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void restrictBackgroundChanged(boolean oldValue, boolean newValue) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 6;
                data.bfield1 = oldValue;
                data.bfield2 = newValue;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void deviceIdleModeEnabled(boolean enabled) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 7;
                data.bfield1 = enabled;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void appIdleStateChanged(int uid, boolean idle) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 8;
                data.ifield1 = uid;
                data.bfield1 = idle;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void paroleStateChanged(boolean paroleOn) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 9;
                data.bfield1 = paroleOn;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void tempPowerSaveWlChanged(int appId, boolean added) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 10;
                data.ifield1 = appId;
                data.bfield1 = added;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void uidFirewallRuleChanged(int chain, int uid, int rule) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 11;
                data.ifield1 = chain;
                data.ifield2 = uid;
                data.ifield3 = rule;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void firewallChainEnabled(int chain, boolean enabled) {
            Data data = (Data) getNextSlot();
            if (data != null) {
                data.reset();
                data.type = 12;
                data.ifield1 = chain;
                data.bfield1 = enabled;
                data.timeStamp = System.currentTimeMillis();
            }
        }

        public void reverseDump(IndentingPrintWriter pw) {
            Data[] allData = (Data[]) toArray();
            for (int i = allData.length - 1; i >= 0; i--) {
                if (allData[i] == null) {
                    pw.println(BluetoothManagerService.DEFAULT_PACKAGE_NAME);
                } else {
                    pw.print(formatDate(allData[i].timeStamp));
                    pw.print(" - ");
                    pw.println(getContent(allData[i]));
                }
            }
        }

        public String getContent(Data data) {
            switch (data.type) {
                case 0:
                    return data.sfield1;
                case 1:
                    return data.ifield1 + "-" + NetworkPolicyLogger.getBlockedReason(data.ifield2);
                case 2:
                    return data.ifield1 + "-" + ProcessList.makeProcStateString(data.ifield2) + "-" + data.lfield1;
                case 3:
                    return NetworkPolicyLogger.getPolicyChangedLog(data.ifield1, data.ifield2, data.ifield3);
                case 4:
                    return NetworkPolicyLogger.getMeterednessChangedLog(data.ifield1, data.bfield1);
                case 5:
                    return NetworkPolicyLogger.getUserRemovedLog(data.ifield1);
                case 6:
                    return NetworkPolicyLogger.getRestrictBackgroundChangedLog(data.bfield1, data.bfield2);
                case 7:
                    return NetworkPolicyLogger.getDeviceIdleModeEnabled(data.bfield1);
                case 8:
                    return NetworkPolicyLogger.getAppIdleChangedLog(data.ifield1, data.bfield1);
                case 9:
                    return NetworkPolicyLogger.getParoleStateChanged(data.bfield1);
                case 10:
                    return NetworkPolicyLogger.getTempPowerSaveWlChangedLog(data.ifield1, data.bfield1);
                case 11:
                    return NetworkPolicyLogger.getUidFirewallRuleChangedLog(data.ifield1, data.ifield2, data.ifield3);
                case 12:
                    return NetworkPolicyLogger.getFirewallChainEnabledLog(data.ifield1, data.bfield1);
                default:
                    return String.valueOf(data.type);
            }
        }

        private String formatDate(long millis) {
            sDate.setTime(millis);
            return sFormatter.format(sDate);
        }
    }

    static {
        int i = 50;
        if (!ActivityManager.isLowRamDeviceStatic()) {
            i = 100;
        }
        MAX_NETWORK_BLOCKED_LOG_SIZE = i;
    }

    /* access modifiers changed from: package-private */
    public void networkBlocked(int uid, int reason) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, uid + " is " + getBlockedReason(reason));
            }
            this.mNetworkBlockedBuffer.networkBlocked(uid, reason);
        }
    }

    /* access modifiers changed from: package-private */
    public void uidStateChanged(int uid, int procState, long procStateSeq) {
        synchronized (this.mLock) {
            if (LOGV) {
                Slog.v(TAG, uid + " state changed to " + procState + " with seq=" + procStateSeq);
            }
            this.mUidStateChangeBuffer.uidStateChanged(uid, procState, procStateSeq);
        }
    }

    /* access modifiers changed from: package-private */
    public void event(String msg) {
        synchronized (this.mLock) {
            if (LOGV) {
                Slog.v(TAG, msg);
            }
            this.mEventsBuffer.event(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public void uidPolicyChanged(int uid, int oldPolicy, int newPolicy) {
        synchronized (this.mLock) {
            if (LOGV) {
                Slog.v(TAG, getPolicyChangedLog(uid, oldPolicy, newPolicy));
            }
            this.mEventsBuffer.uidPolicyChanged(uid, oldPolicy, newPolicy);
        }
    }

    /* access modifiers changed from: package-private */
    public void meterednessChanged(int netId, boolean newMetered) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getMeterednessChangedLog(netId, newMetered));
            }
            this.mEventsBuffer.meterednessChanged(netId, newMetered);
        }
    }

    /* access modifiers changed from: package-private */
    public void removingUserState(int userId) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getUserRemovedLog(userId));
            }
            this.mEventsBuffer.userRemoved(userId);
        }
    }

    /* access modifiers changed from: package-private */
    public void restrictBackgroundChanged(boolean oldValue, boolean newValue) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getRestrictBackgroundChangedLog(oldValue, newValue));
            }
            this.mEventsBuffer.restrictBackgroundChanged(oldValue, newValue);
        }
    }

    /* access modifiers changed from: package-private */
    public void deviceIdleModeEnabled(boolean enabled) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getDeviceIdleModeEnabled(enabled));
            }
            this.mEventsBuffer.deviceIdleModeEnabled(enabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void appIdleStateChanged(int uid, boolean idle) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getAppIdleChangedLog(uid, idle));
            }
            this.mEventsBuffer.appIdleStateChanged(uid, idle);
        }
    }

    /* access modifiers changed from: package-private */
    public void paroleStateChanged(boolean paroleOn) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getParoleStateChanged(paroleOn));
            }
            this.mEventsBuffer.paroleStateChanged(paroleOn);
        }
    }

    /* access modifiers changed from: package-private */
    public void tempPowerSaveWlChanged(int appId, boolean added) {
        synchronized (this.mLock) {
            if (LOGV) {
                Slog.v(TAG, getTempPowerSaveWlChangedLog(appId, added));
            }
            this.mEventsBuffer.tempPowerSaveWlChanged(appId, added);
        }
    }

    /* access modifiers changed from: package-private */
    public void uidFirewallRuleChanged(int chain, int uid, int rule) {
        synchronized (this.mLock) {
            if (LOGV) {
                Slog.v(TAG, getUidFirewallRuleChangedLog(chain, uid, rule));
            }
            this.mEventsBuffer.uidFirewallRuleChanged(chain, uid, rule);
        }
    }

    /* access modifiers changed from: package-private */
    public void firewallChainEnabled(int chain, boolean enabled) {
        synchronized (this.mLock) {
            if (LOGD) {
                Slog.d(TAG, getFirewallChainEnabledLog(chain, enabled));
            }
            this.mEventsBuffer.firewallChainEnabled(chain, enabled);
        }
    }

    /* access modifiers changed from: package-private */
    public void firewallRulesChanged(int chain, int[] uids, int[] rules) {
        synchronized (this.mLock) {
            String log = "Firewall rules changed for " + getFirewallChainName(chain) + "; uids=" + Arrays.toString(uids) + "; rules=" + Arrays.toString(rules);
            if (LOGD) {
                Slog.d(TAG, log);
            }
            this.mEventsBuffer.event(log);
        }
    }

    /* access modifiers changed from: package-private */
    public void meteredRestrictedPkgsChanged(Set<Integer> restrictedUids) {
        synchronized (this.mLock) {
            String log = "Metered restricted uids: " + restrictedUids;
            if (LOGD) {
                Slog.d(TAG, log);
            }
            this.mEventsBuffer.event(log);
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpLogs(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            pw.println();
            pw.println("mEventLogs (most recent first):");
            pw.increaseIndent();
            this.mEventsBuffer.reverseDump(pw);
            pw.decreaseIndent();
            pw.println();
            pw.println("mNetworkBlockedLogs (most recent first):");
            pw.increaseIndent();
            this.mNetworkBlockedBuffer.reverseDump(pw);
            pw.decreaseIndent();
            pw.println();
            pw.println("mUidStateChangeLogs (most recent first):");
            pw.increaseIndent();
            this.mUidStateChangeBuffer.reverseDump(pw);
            pw.decreaseIndent();
        }
    }

    /* access modifiers changed from: private */
    public static String getBlockedReason(int reason) {
        switch (reason) {
            case 0:
                return "blocked by power restrictions";
            case 1:
                return "allowed on unmetered network";
            case 2:
                return "blacklisted on metered network";
            case 3:
                return "whitelisted on metered network";
            case 4:
                return "temporary whitelisted on metered network";
            case 5:
                return "blocked when background is restricted";
            case 6:
                return "allowed by default";
            default:
                return String.valueOf(reason);
        }
    }

    /* access modifiers changed from: private */
    public static String getPolicyChangedLog(int uid, int oldPolicy, int newPolicy) {
        return "Policy for " + uid + " changed from " + NetworkPolicyManager.uidPoliciesToString(oldPolicy) + " to " + NetworkPolicyManager.uidPoliciesToString(newPolicy);
    }

    /* access modifiers changed from: private */
    public static String getMeterednessChangedLog(int netId, boolean newMetered) {
        return "Meteredness of netId=" + netId + " changed to " + newMetered;
    }

    /* access modifiers changed from: private */
    public static String getUserRemovedLog(int userId) {
        return "Remove state for u" + userId;
    }

    /* access modifiers changed from: private */
    public static String getRestrictBackgroundChangedLog(boolean oldValue, boolean newValue) {
        return "Changed restrictBackground: " + oldValue + "->" + newValue;
    }

    /* access modifiers changed from: private */
    public static String getDeviceIdleModeEnabled(boolean enabled) {
        return "DeviceIdleMode enabled: " + enabled;
    }

    /* access modifiers changed from: private */
    public static String getAppIdleChangedLog(int uid, boolean idle) {
        return "App idle state of uid " + uid + ": " + idle;
    }

    /* access modifiers changed from: private */
    public static String getParoleStateChanged(boolean paroleOn) {
        return "Parole state: " + paroleOn;
    }

    /* access modifiers changed from: private */
    public static String getTempPowerSaveWlChangedLog(int appId, boolean added) {
        return "temp-power-save whitelist for " + appId + " changed to: " + added;
    }

    /* access modifiers changed from: private */
    public static String getUidFirewallRuleChangedLog(int chain, int uid, int rule) {
        return String.format("Firewall rule changed: %d-%s-%s", new Object[]{Integer.valueOf(uid), getFirewallChainName(chain), getFirewallRuleName(rule)});
    }

    /* access modifiers changed from: private */
    public static String getFirewallChainEnabledLog(int chain, boolean enabled) {
        return "Firewall chain " + getFirewallChainName(chain) + " state: " + enabled;
    }

    private static String getFirewallChainName(int chain) {
        switch (chain) {
            case 1:
                return "dozable";
            case 2:
                return "standby";
            case 3:
                return "powersave";
            default:
                return String.valueOf(chain);
        }
    }

    private static String getFirewallRuleName(int rule) {
        switch (rule) {
            case 0:
                return BatteryService.HealthServiceWrapper.INSTANCE_VENDOR;
            case 1:
                return "allow";
            case 2:
                return "deny";
            default:
                return String.valueOf(rule);
        }
    }
}
