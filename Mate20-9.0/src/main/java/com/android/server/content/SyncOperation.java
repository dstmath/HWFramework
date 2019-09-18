package com.android.server.content;

import android.accounts.Account;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Slog;
import com.android.server.content.SyncStorageEngine;
import com.android.server.os.HwBootFail;
import com.android.server.policy.PhoneWindowManager;
import com.android.server.slice.SliceClientPermissions;
import java.util.Iterator;

public class SyncOperation {
    public static final int NO_JOB_ID = -1;
    public static final int REASON_ACCOUNTS_UPDATED = -2;
    public static final int REASON_BACKGROUND_DATA_SETTINGS_CHANGED = -1;
    public static final int REASON_IS_SYNCABLE = -5;
    public static final int REASON_MASTER_SYNC_AUTO = -7;
    private static String[] REASON_NAMES = {"DataSettingsChanged", "AccountsUpdated", "ServiceChanged", "Periodic", "IsSyncable", "AutoSync", "MasterSyncAuto", "UserStart"};
    public static final int REASON_PERIODIC = -4;
    public static final int REASON_SERVICE_CHANGED = -3;
    public static final int REASON_SYNC_AUTO = -6;
    public static final int REASON_USER_START = -8;
    public static final String TAG = "SyncManager";
    public final boolean allowParallelSyncs;
    public long expectedRuntime;
    public final Bundle extras;
    public final long flexMillis;
    public final boolean isPeriodic;
    public int jobId;
    public final String key;
    public final String owningPackage;
    public final int owningUid;
    public final long periodMillis;
    public final int reason;
    int retries;
    public final int sourcePeriodicId;
    public int syncExemptionFlag;
    public final int syncSource;
    public final SyncStorageEngine.EndPoint target;
    public String wakeLockName;

    public SyncOperation(Account account, int userId, int owningUid2, String owningPackage2, int reason2, int source, String provider, Bundle extras2, boolean allowParallelSyncs2, int syncExemptionFlag2) {
        this(new SyncStorageEngine.EndPoint(account, provider, userId), owningUid2, owningPackage2, reason2, source, extras2, allowParallelSyncs2, syncExemptionFlag2);
    }

    private SyncOperation(SyncStorageEngine.EndPoint info, int owningUid2, String owningPackage2, int reason2, int source, Bundle extras2, boolean allowParallelSyncs2, int syncExemptionFlag2) {
        this(info, owningUid2, owningPackage2, reason2, source, extras2, allowParallelSyncs2, false, -1, 0, 0, syncExemptionFlag2);
    }

    /* JADX WARNING: Illegal instructions before constructor call */
    public SyncOperation(SyncOperation op, long periodMillis2, long flexMillis2) {
        this(r0.target, r0.owningUid, r0.owningPackage, r0.reason, r0.syncSource, new Bundle(r0.extras), r0.allowParallelSyncs, r0.isPeriodic, r0.sourcePeriodicId, periodMillis2, flexMillis2, 0);
        SyncOperation syncOperation = op;
    }

    public SyncOperation(SyncStorageEngine.EndPoint info, int owningUid2, String owningPackage2, int reason2, int source, Bundle extras2, boolean allowParallelSyncs2, boolean isPeriodic2, int sourcePeriodicId2, long periodMillis2, long flexMillis2, int syncExemptionFlag2) {
        this.target = info;
        this.owningUid = owningUid2;
        this.owningPackage = owningPackage2;
        this.reason = reason2;
        this.syncSource = source;
        this.extras = new Bundle(extras2);
        this.allowParallelSyncs = allowParallelSyncs2;
        this.isPeriodic = isPeriodic2;
        this.sourcePeriodicId = sourcePeriodicId2;
        this.periodMillis = periodMillis2;
        this.flexMillis = flexMillis2;
        this.jobId = -1;
        this.key = toKey();
        this.syncExemptionFlag = syncExemptionFlag2;
    }

    public SyncOperation createOneTimeSyncOperation() {
        if (!this.isPeriodic) {
            return null;
        }
        SyncOperation syncOperation = new SyncOperation(this.target, this.owningUid, this.owningPackage, this.reason, this.syncSource, new Bundle(this.extras), this.allowParallelSyncs, false, this.jobId, this.periodMillis, this.flexMillis, 0);
        return syncOperation;
    }

    public SyncOperation(SyncOperation other) {
        this.target = other.target;
        this.owningUid = other.owningUid;
        this.owningPackage = other.owningPackage;
        this.reason = other.reason;
        this.syncSource = other.syncSource;
        this.allowParallelSyncs = other.allowParallelSyncs;
        this.extras = new Bundle(other.extras);
        this.wakeLockName = other.wakeLockName();
        this.isPeriodic = other.isPeriodic;
        this.sourcePeriodicId = other.sourcePeriodicId;
        this.periodMillis = other.periodMillis;
        this.flexMillis = other.flexMillis;
        this.key = other.key;
        this.syncExemptionFlag = other.syncExemptionFlag;
    }

    /* access modifiers changed from: package-private */
    public PersistableBundle toJobInfoExtras() {
        PersistableBundle jobInfoExtras = new PersistableBundle();
        PersistableBundle syncExtrasBundle = new PersistableBundle();
        for (String key2 : this.extras.keySet()) {
            Object value = this.extras.get(key2);
            if (value instanceof Account) {
                Account account = (Account) value;
                PersistableBundle accountBundle = new PersistableBundle();
                accountBundle.putString("accountName", account.name);
                accountBundle.putString("accountType", account.type);
                jobInfoExtras.putPersistableBundle("ACCOUNT:" + key2, accountBundle);
            } else if (value instanceof Long) {
                syncExtrasBundle.putLong(key2, ((Long) value).longValue());
            } else if (value instanceof Integer) {
                syncExtrasBundle.putInt(key2, ((Integer) value).intValue());
            } else if (value instanceof Boolean) {
                syncExtrasBundle.putBoolean(key2, ((Boolean) value).booleanValue());
            } else if (value instanceof Float) {
                syncExtrasBundle.putDouble(key2, (double) ((Float) value).floatValue());
            } else if (value instanceof Double) {
                syncExtrasBundle.putDouble(key2, ((Double) value).doubleValue());
            } else if (value instanceof String) {
                syncExtrasBundle.putString(key2, (String) value);
            } else if (value == null) {
                syncExtrasBundle.putString(key2, null);
            } else {
                Slog.e(TAG, "Unknown extra type.");
            }
        }
        jobInfoExtras.putPersistableBundle("syncExtras", syncExtrasBundle);
        jobInfoExtras.putBoolean("SyncManagerJob", true);
        jobInfoExtras.putString("provider", this.target.provider);
        jobInfoExtras.putString("accountName", this.target.account.name);
        jobInfoExtras.putString("accountType", this.target.account.type);
        jobInfoExtras.putInt("userId", this.target.userId);
        jobInfoExtras.putInt("owningUid", this.owningUid);
        jobInfoExtras.putString("owningPackage", this.owningPackage);
        jobInfoExtras.putInt(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, this.reason);
        jobInfoExtras.putInt("source", this.syncSource);
        jobInfoExtras.putBoolean("allowParallelSyncs", this.allowParallelSyncs);
        jobInfoExtras.putInt("jobId", this.jobId);
        jobInfoExtras.putBoolean("isPeriodic", this.isPeriodic);
        jobInfoExtras.putInt("sourcePeriodicId", this.sourcePeriodicId);
        jobInfoExtras.putLong("periodMillis", this.periodMillis);
        jobInfoExtras.putLong("flexMillis", this.flexMillis);
        jobInfoExtras.putLong("expectedRuntime", this.expectedRuntime);
        jobInfoExtras.putInt("retries", this.retries);
        jobInfoExtras.putInt("syncExemptionFlag", this.syncExemptionFlag);
        return jobInfoExtras;
    }

    static SyncOperation maybeCreateFromJobExtras(PersistableBundle jobExtras) {
        Iterator it;
        PersistableBundle persistableBundle = jobExtras;
        if (persistableBundle == null || !persistableBundle.getBoolean("SyncManagerJob", false)) {
            return null;
        }
        String accountName = persistableBundle.getString("accountName");
        String accountType = persistableBundle.getString("accountType");
        String provider = persistableBundle.getString("provider");
        int userId = persistableBundle.getInt("userId", HwBootFail.STAGE_BOOT_SUCCESS);
        int owningUid2 = persistableBundle.getInt("owningUid");
        String owningPackage2 = persistableBundle.getString("owningPackage");
        int reason2 = persistableBundle.getInt(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY, HwBootFail.STAGE_BOOT_SUCCESS);
        int source = persistableBundle.getInt("source", HwBootFail.STAGE_BOOT_SUCCESS);
        boolean allowParallelSyncs2 = persistableBundle.getBoolean("allowParallelSyncs", false);
        boolean isPeriodic2 = persistableBundle.getBoolean("isPeriodic", false);
        int initiatedBy = persistableBundle.getInt("sourcePeriodicId", -1);
        long periodMillis2 = persistableBundle.getLong("periodMillis");
        long flexMillis2 = persistableBundle.getLong("flexMillis");
        int syncExemptionFlag2 = persistableBundle.getInt("syncExemptionFlag", 0);
        Bundle extras2 = new Bundle();
        PersistableBundle syncExtras = persistableBundle.getPersistableBundle("syncExtras");
        if (syncExtras != null) {
            extras2.putAll(syncExtras);
        }
        Iterator it2 = jobExtras.keySet().iterator();
        while (it2.hasNext()) {
            String key2 = (String) it2.next();
            if (key2 == null || !key2.startsWith("ACCOUNT:")) {
                it = it2;
            } else {
                String newKey = key2.substring(8);
                PersistableBundle accountsBundle = persistableBundle.getPersistableBundle(key2);
                it = it2;
                extras2.putParcelable(newKey, new Account(accountsBundle.getString("accountName"), accountsBundle.getString("accountType")));
            }
            it2 = it;
        }
        Account account = new Account(accountName, accountType);
        Account account2 = account;
        PersistableBundle persistableBundle2 = syncExtras;
        Bundle bundle = extras2;
        SyncOperation op = new SyncOperation(new SyncStorageEngine.EndPoint(account, provider, userId), owningUid2, owningPackage2, reason2, source, extras2, allowParallelSyncs2, isPeriodic2, initiatedBy, periodMillis2, flexMillis2, syncExemptionFlag2);
        op.jobId = persistableBundle.getInt("jobId");
        op.expectedRuntime = persistableBundle.getLong("expectedRuntime");
        op.retries = persistableBundle.getInt("retries");
        return op;
    }

    /* access modifiers changed from: package-private */
    public boolean isConflict(SyncOperation toRun) {
        SyncStorageEngine.EndPoint other = toRun.target;
        return this.target.account.type.equals(other.account.type) && this.target.provider.equals(other.provider) && this.target.userId == other.userId && (!this.allowParallelSyncs || this.target.account.name.equals(other.account.name));
    }

    /* access modifiers changed from: package-private */
    public boolean isReasonPeriodic() {
        return this.reason == -4;
    }

    /* access modifiers changed from: package-private */
    public boolean matchesPeriodicOperation(SyncOperation other) {
        if (!this.target.matchesSpec(other.target) || !SyncManager.syncExtrasEquals(this.extras, other.extras, true) || this.periodMillis != other.periodMillis || this.flexMillis != other.flexMillis) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isDerivedFromFailedPeriodicSync() {
        return this.sourcePeriodicId != -1;
    }

    /* access modifiers changed from: package-private */
    public int findPriority() {
        if (isInitialization()) {
            return 20;
        }
        if (isExpedited()) {
            return 10;
        }
        return 0;
    }

    private String toKey() {
        StringBuilder sb = new StringBuilder();
        sb.append("provider: ");
        sb.append(this.target.provider);
        sb.append(" account {name=" + this.target.account.name + ", user=" + this.target.userId + ", type=" + this.target.account.type + "}");
        sb.append(" isPeriodic: ");
        sb.append(this.isPeriodic);
        sb.append(" period: ");
        sb.append(this.periodMillis);
        sb.append(" flex: ");
        sb.append(this.flexMillis);
        sb.append(" extras: ");
        extrasToStringBuilder(this.extras, sb);
        return sb.toString();
    }

    public String toString() {
        return dump(null, true, null);
    }

    /* access modifiers changed from: package-private */
    public String dump(PackageManager pm, boolean shorter, SyncAdapterStateFetcher appStates) {
        StringBuilder sb = new StringBuilder();
        sb.append("JobId: ");
        sb.append(this.jobId);
        sb.append(", ");
        sb.append("XXXXXXXXX");
        sb.append(" u");
        sb.append(this.target.userId);
        sb.append(" (");
        sb.append(this.target.account.type);
        sb.append(" u");
        sb.append(this.target.userId);
        sb.append(" [");
        sb.append(this.target.provider);
        sb.append("] ");
        sb.append(SyncStorageEngine.SOURCES[this.syncSource]);
        if (this.expectedRuntime != 0) {
            sb.append(" ExpectedIn=");
            SyncManager.formatDurationHMS(sb, this.expectedRuntime - SystemClock.elapsedRealtime());
        }
        if (this.extras.getBoolean("expedited", false)) {
            sb.append(" EXPEDITED");
        }
        switch (this.syncExemptionFlag) {
            case 0:
                break;
            case 1:
                sb.append(" STANDBY-EXEMPTED");
                break;
            case 2:
                sb.append(" STANDBY-EXEMPTED(TOP)");
                break;
            default:
                sb.append(" ExemptionFlag=" + this.syncExemptionFlag);
                break;
        }
        sb.append(" Reason=");
        sb.append(reasonToString(pm, this.reason));
        if (this.isPeriodic) {
            sb.append(" (period=");
            SyncManager.formatDurationHMS(sb, this.periodMillis);
            sb.append(" flex=");
            SyncManager.formatDurationHMS(sb, this.flexMillis);
            sb.append(")");
        }
        if (this.retries > 0) {
            sb.append(" Retries=");
            sb.append(this.retries);
        }
        if (!shorter) {
            sb.append(" Owner={");
            UserHandle.formatUid(sb, this.owningUid);
            sb.append(" ");
            sb.append(this.owningPackage);
            if (appStates != null) {
                sb.append(" [");
                sb.append(appStates.getStandbyBucket(UserHandle.getUserId(this.owningUid), this.owningPackage));
                sb.append("]");
                if (appStates.isAppActive(this.owningUid)) {
                    sb.append(" [ACTIVE]");
                }
            }
            sb.append("}");
            if (!this.extras.keySet().isEmpty()) {
                sb.append(" ");
                extrasToStringBuilder(this.extras, sb);
            }
        }
        return sb.toString();
    }

    static String reasonToString(PackageManager pm, int reason2) {
        if (reason2 < 0) {
            int index = (-reason2) - 1;
            if (index >= REASON_NAMES.length) {
                return String.valueOf(reason2);
            }
            return REASON_NAMES[index];
        } else if (pm == null) {
            return String.valueOf(reason2);
        } else {
            String[] packages = pm.getPackagesForUid(reason2);
            if (packages != null && packages.length == 1) {
                return packages[0];
            }
            String name = pm.getNameForUid(reason2);
            if (name != null) {
                return name;
            }
            return String.valueOf(reason2);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInitialization() {
        return this.extras.getBoolean("initialize", false);
    }

    /* access modifiers changed from: package-private */
    public boolean isExpedited() {
        return this.extras.getBoolean("expedited", false);
    }

    /* access modifiers changed from: package-private */
    public boolean ignoreBackoff() {
        return this.extras.getBoolean("ignore_backoff", false);
    }

    /* access modifiers changed from: package-private */
    public boolean isNotAllowedOnMetered() {
        return this.extras.getBoolean("allow_metered", false);
    }

    /* access modifiers changed from: package-private */
    public boolean isManual() {
        return this.extras.getBoolean("force", false);
    }

    /* access modifiers changed from: package-private */
    public boolean isIgnoreSettings() {
        return this.extras.getBoolean("ignore_settings", false);
    }

    /* access modifiers changed from: package-private */
    public boolean isAppStandbyExempted() {
        return this.syncExemptionFlag != 0;
    }

    static void extrasToStringBuilder(Bundle bundle, StringBuilder sb) {
        if (bundle == null) {
            sb.append("null");
            return;
        }
        sb.append("[");
        for (String key2 : bundle.keySet()) {
            sb.append(key2);
            sb.append("=");
            sb.append(bundle.get(key2));
            sb.append(" ");
        }
        sb.append("]");
    }

    static String extrasToString(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        extrasToStringBuilder(bundle, sb);
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public String wakeLockName() {
        if (this.wakeLockName != null) {
            return this.wakeLockName;
        }
        String str = this.target.provider + SliceClientPermissions.SliceAuthority.DELIMITER + this.target.account.type;
        this.wakeLockName = str;
        return str;
    }

    public Object[] toEventLog(int event) {
        Object[] logArray = new Object[4];
        logArray[1] = Integer.valueOf(event);
        logArray[2] = Integer.valueOf(this.syncSource);
        logArray[0] = this.target.provider;
        logArray[3] = Integer.valueOf(this.target.account.name.hashCode());
        return logArray;
    }
}
