package com.android.server.mtm.iaware.brjob.scheduler;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.ActionFilterEntry;
import android.content.pm.ResolveInfo;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AwareJobStatus {
    private static boolean DEBUG = false;
    private static final String TAG = "AwareJobStatus";
    private Map<String, String> mActionFilters = new HashMap();
    private int mForceCacheTag = 2;
    private HwBroadcastRecord mHwBr;
    private AtomicBoolean mParseError = new AtomicBoolean(false);
    private ResolveInfo mReceiver;
    private Map<String, Boolean> mSatisfied = new HashMap();
    private AtomicBoolean mShouldCache = new AtomicBoolean(true);
    private AtomicBoolean mShouldRunByError = new AtomicBoolean(false);

    private AwareJobStatus() {
    }

    public AwareJobStatus(HwBroadcastRecord hwBr) {
        if (hwBr != null) {
            this.mHwBr = hwBr;
            List receivers = this.mHwBr.getBrReceivers();
            if (receivers != null && receivers.size() > 0) {
                Object target = receivers.get(0);
                if (target instanceof ResolveInfo) {
                    this.mReceiver = (ResolveInfo) target;
                } else {
                    return;
                }
            }
            if (this.mReceiver != null) {
                IntentFilter filter = this.mReceiver.filter;
                if (filter != null) {
                    Iterator<ActionFilterEntry> it = filter.actionFilterIterator();
                    if (it != null) {
                        while (it.hasNext()) {
                            ActionFilterEntry actionFilter = (ActionFilterEntry) it.next();
                            if (actionFilter.getAction() != null && (actionFilter.getAction().equals(hwBr.getAction()) ^ 1) == 0) {
                                parseActionFilterEntry(actionFilter.getFilterName(), actionFilter.getFilterValue());
                            } else if (DEBUG) {
                                AwareLog.w(TAG, "iaware_brjob, action not match. ");
                            }
                        }
                    }
                }
            }
        }
    }

    private void parseActionFilterEntry(String filterName, String filterValue) {
        if (DEBUG) {
            AwareLog.i(TAG, "iaware_brjob parseActionFilterEntry: " + filterName + ", " + filterValue);
        }
        if (filterName == null || filterName.length() == 0) {
            AwareLog.e(TAG, "iaware_brjob scheduler state key is error!");
            this.mParseError.set(true);
            return;
        }
        filterName = filterName.trim().toLowerCase(Locale.ENGLISH);
        if (AwareJobSchedulerConstants.FORCE_CACHE_ACTION_FILTER_NAME.toLowerCase(Locale.ENGLISH).equals(filterName)) {
            try {
                this.mForceCacheTag = Integer.parseInt(filterValue);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "iaware_brjob scheduler force cache value error!");
            }
            return;
        }
        boolean hasMatch = false;
        String[] conditionArray = AwareJobSchedulerConstants.getConditionArray();
        for (String condition : conditionArray) {
            if (condition.toLowerCase(Locale.ENGLISH).equals(filterName)) {
                hasMatch = true;
                this.mActionFilters.put(condition, filterValue);
                break;
            }
        }
        if (!hasMatch) {
            AwareLog.e(TAG, "iaware_brjob scheduler state key is error!");
            this.mParseError.set(true);
        }
    }

    public boolean hasConstraint(String key) {
        if (this.mActionFilters.containsKey(key)) {
            return true;
        }
        return false;
    }

    public boolean isParamError() {
        return !TextUtils.isEmpty(getAction()) ? TextUtils.isEmpty(getComponentName()) : true;
    }

    public boolean isParseError() {
        return this.mParseError.get();
    }

    public void setSatisfied(String condition, boolean satisfied) {
        if (condition != null) {
            boolean shouldCache = shouldCache(condition);
            if (DEBUG) {
                AwareLog.i(TAG, "iaware_brjob, receiver: " + getComponentName() + ", condition: " + condition + " satisfied: " + satisfied);
            }
            this.mSatisfied.put(condition, Boolean.valueOf(satisfied));
            this.mShouldCache.compareAndSet(true, shouldCache);
        }
    }

    public boolean isSatisfied(String condition) {
        if (condition == null || (this.mSatisfied.containsKey(condition) ^ 1) != 0) {
            return false;
        }
        return ((Boolean) this.mSatisfied.get(condition)).booleanValue();
    }

    private boolean shouldCache(String condition) {
        if (AwareJobSchedulerConstants.getCacheConditionMap().containsKey(condition)) {
            return ((Boolean) AwareJobSchedulerConstants.getCacheConditionMap().get(condition)).booleanValue();
        }
        return false;
    }

    public String getActionFilterValue(String filterName) {
        if (filterName == null) {
            return null;
        }
        return (String) this.mActionFilters.get(filterName);
    }

    public int getActionFilterSize() {
        return this.mActionFilters.size();
    }

    public String getReceiverPkg() {
        if (!(this.mReceiver == null || this.mReceiver.getComponentInfo() == null)) {
            try {
                return this.mReceiver.getComponentInfo().packageName;
            } catch (IllegalStateException e) {
                AwareLog.e(TAG, "iaware_brjob, mReceiver.getComponentInfo() error!");
            }
        }
        return null;
    }

    public String getComponentName() {
        if (!(this.mReceiver == null || this.mReceiver.getComponentInfo() == null)) {
            try {
                return this.mReceiver.getComponentInfo().getComponentName().getClassName();
            } catch (IllegalStateException e) {
                AwareLog.e(TAG, "iaware_brjob, mReceiver.getComponentInfo() error!");
            }
        }
        return null;
    }

    public HwBroadcastRecord getHwBroadcastRecord() {
        return this.mHwBr;
    }

    public String getAction() {
        if (this.mHwBr != null) {
            return this.mHwBr.getAction();
        }
        return null;
    }

    public Intent getIntent() {
        if (this.mHwBr != null) {
            return this.mHwBr.getIntent();
        }
        return null;
    }

    public boolean equalJob(AwareJobStatus job) {
        if (job == null) {
            return false;
        }
        String action = job.getAction();
        String comp = job.getComponentName();
        if (action == null || !action.equals(getAction()) || comp == null || !comp.equals(getComponentName())) {
            return false;
        }
        return true;
    }

    public boolean shouldCancelled() {
        boolean shouldCancel;
        if (this.mForceCacheTag == 2) {
            shouldCancel = this.mShouldCache.get() ^ 1;
        } else if (this.mForceCacheTag == 1) {
            shouldCancel = false;
        } else {
            shouldCancel = true;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "iaware_brjob, shouldCancelled: " + shouldCancel);
        }
        return shouldCancel;
    }

    public void setShouldRunByError() {
        this.mShouldRunByError.set(true);
    }

    public boolean isShouldRunByError() {
        return this.mShouldRunByError.get();
    }

    public boolean isReady() {
        if (this.mShouldRunByError.get()) {
            if (DEBUG) {
                AwareLog.i(TAG, "iaware_brjob isReady all: ShouldRunByError");
            }
            return true;
        }
        boolean ready = true;
        for (Entry<String, String> entry : this.mActionFilters.entrySet()) {
            String condition = (String) entry.getKey();
            if (DEBUG) {
                AwareLog.i(TAG, "iaware_brjob isReady: " + condition + ", " + (this.mSatisfied.containsKey(condition) ? (Serializable) this.mSatisfied.get(condition) : "false(not contains)"));
            }
            if (this.mSatisfied.containsKey(condition)) {
                if ((((Boolean) this.mSatisfied.get(condition)).booleanValue() ^ 1) != 0) {
                }
            }
            ready = false;
        }
        if (DEBUG) {
            AwareLog.i(TAG, "iaware_brjob, receiver: " + getComponentName() + ", action: " + getAction() + ", isReady: " + ready);
        }
        return ready;
    }

    public static AwareJobStatus createFromBroadcastRecord(HwBroadcastRecord hwBr) {
        return new AwareJobStatus(hwBr);
    }

    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.print("  AwareJOB #");
            pw.print(" action:");
            if (this.mHwBr != null) {
                pw.print(this.mHwBr.getAction());
            }
            pw.print(" receiver:");
            pw.println(getReceiverPkg());
            pw.println("    filter:");
            for (Entry<String, String> entry : this.mActionFilters.entrySet()) {
                pw.print("        name: ");
                pw.print((String) entry.getKey());
                pw.print("  value: ");
                pw.println((String) entry.getValue());
            }
        }
    }

    public String toString() {
        String jobinfo = this.mHwBr.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(getComponentName());
        sb.append("]");
        return jobinfo + sb.toString();
    }

    public static final void setDebug(boolean debug) {
        DEBUG = debug;
    }
}
