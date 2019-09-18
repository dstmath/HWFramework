package com.android.server.mtm.iaware.brjob.scheduler;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.rms.iaware.AwareLog;
import android.text.TextUtils;
import com.android.server.am.HwBroadcastRecord;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
                    Iterator<IntentFilter.ActionFilterEntry> it = filter.actionFilterIterator();
                    if (it != null) {
                        while (it.hasNext()) {
                            IntentFilter.ActionFilterEntry actionFilter = it.next();
                            if (actionFilter.getAction() != null && actionFilter.getAction().equals(hwBr.getAction())) {
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
        String filterName2 = filterName.trim().toLowerCase(Locale.ENGLISH);
        if (AwareJobSchedulerConstants.FORCE_CACHE_ACTION_FILTER_NAME.toLowerCase(Locale.ENGLISH).equals(filterName2)) {
            try {
                this.mForceCacheTag = Integer.parseInt(filterValue);
            } catch (NumberFormatException e) {
                AwareLog.e(TAG, "iaware_brjob scheduler force cache value error!");
            }
            return;
        }
        boolean hasMatch = false;
        String[] conditionArray = AwareJobSchedulerConstants.getConditionArray();
        int i = 0;
        while (true) {
            if (i >= conditionArray.length) {
                break;
            }
            String condition = conditionArray[i];
            if (condition.toLowerCase(Locale.ENGLISH).equals(filterName2)) {
                hasMatch = true;
                this.mActionFilters.put(condition, filterValue);
                break;
            }
            i++;
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
        return TextUtils.isEmpty(getAction()) || TextUtils.isEmpty(getComponentName());
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
        if (condition == null || !this.mSatisfied.containsKey(condition)) {
            return false;
        }
        return this.mSatisfied.get(condition).booleanValue();
    }

    private boolean shouldCache(String condition) {
        if (AwareJobSchedulerConstants.getCacheConditionMap().containsKey(condition)) {
            return AwareJobSchedulerConstants.getCacheConditionMap().get(condition).booleanValue();
        }
        return false;
    }

    public String getActionFilterValue(String filterName) {
        if (filterName == null) {
            return null;
        }
        return this.mActionFilters.get(filterName);
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
        boolean shouldCancel = true;
        if (this.mForceCacheTag == 2) {
            shouldCancel = !this.mShouldCache.get();
        } else if (this.mForceCacheTag == 1) {
            shouldCancel = false;
        }
        boolean shouldCancel2 = shouldCancel;
        if (DEBUG) {
            AwareLog.i(TAG, "iaware_brjob, shouldCancelled: " + shouldCancel2);
        }
        return shouldCancel2;
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
        Iterator<Map.Entry<String, String>> it = this.mActionFilters.entrySet().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String condition = it.next().getKey();
            if (DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("iaware_brjob isReady: ");
                sb.append(condition);
                sb.append(", ");
                sb.append(this.mSatisfied.containsKey(condition) ? this.mSatisfied.get(condition) : "false(not contains)");
                AwareLog.i(TAG, sb.toString());
            }
            if (this.mSatisfied.containsKey(condition)) {
                if (!this.mSatisfied.get(condition).booleanValue()) {
                    break;
                }
            } else {
                break;
            }
        }
        ready = false;
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
            for (Map.Entry<String, String> entry : this.mActionFilters.entrySet()) {
                pw.print("        name: ");
                pw.print(entry.getKey());
                pw.print("  value: ");
                pw.println(entry.getValue());
            }
        }
    }

    public String toString() {
        String jobinfo = this.mHwBr.toString();
        return jobinfo + ("[" + getComponentName() + "]");
    }

    public static final void setDebug(boolean debug) {
        DEBUG = debug;
    }
}
