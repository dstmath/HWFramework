package com.android.server.notification;

import android.content.ComponentName;
import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.notification.ConditionProviders.Callback;
import java.io.PrintWriter;
import java.util.Objects;

public class ZenModeConditions implements Callback {
    private static final boolean DEBUG = ZenModeHelper.DEBUG;
    private static final String TAG = "ZenModeHelper";
    private final ConditionProviders mConditionProviders;
    private boolean mFirstEvaluation = true;
    private final ZenModeHelper mHelper;
    private final ArrayMap<Uri, ComponentName> mSubscriptions = new ArrayMap();

    public ZenModeConditions(ZenModeHelper helper, ConditionProviders conditionProviders) {
        this.mHelper = helper;
        this.mConditionProviders = conditionProviders;
        if (this.mConditionProviders.isSystemProviderEnabled("countdown")) {
            this.mConditionProviders.addSystemProvider(new CountdownConditionProvider());
        }
        if (this.mConditionProviders.isSystemProviderEnabled("schedule")) {
            this.mConditionProviders.addSystemProvider(new ScheduleConditionProvider());
        }
        if (this.mConditionProviders.isSystemProviderEnabled("event")) {
            this.mConditionProviders.addSystemProvider(new EventConditionProvider());
        }
        this.mConditionProviders.setCallback(this);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mSubscriptions=");
        pw.println(this.mSubscriptions);
    }

    public void evaluateConfig(ZenModeConfig config, boolean processSubscriptions) {
        if (config != null) {
            if (!(config.manualRule == null || config.manualRule.condition == null || (config.manualRule.isTrueOrUnknown() ^ 1) == 0)) {
                if (DEBUG) {
                    Log.d(TAG, "evaluateConfig: clearing manual rule");
                }
                config.manualRule = null;
            }
            ArraySet<Uri> current = new ArraySet();
            evaluateRule(config.manualRule, current, processSubscriptions);
            for (ZenRule automaticRule : config.automaticRules.values()) {
                evaluateRule(automaticRule, current, processSubscriptions);
                updateSnoozing(automaticRule);
            }
            synchronized (this.mSubscriptions) {
                for (int i = this.mSubscriptions.size() - 1; i >= 0; i--) {
                    Uri id = (Uri) this.mSubscriptions.keyAt(i);
                    ComponentName component = (ComponentName) this.mSubscriptions.valueAt(i);
                    if (processSubscriptions && !current.contains(id)) {
                        this.mConditionProviders.unsubscribeIfNecessary(component, id);
                        this.mSubscriptions.removeAt(i);
                    }
                }
            }
            this.mFirstEvaluation = false;
        }
    }

    public void onBootComplete() {
    }

    public void onUserSwitched() {
    }

    public void onServiceAdded(ComponentName component) {
        if (DEBUG) {
            Log.d(TAG, "onServiceAdded " + component);
        }
        this.mHelper.setConfig(this.mHelper.getConfig(), "zmc.onServiceAdded");
    }

    public void onConditionChanged(Uri id, Condition condition) {
        if (DEBUG) {
            Log.d(TAG, "onConditionChanged " + id + " " + condition);
        }
        ZenModeConfig config = this.mHelper.getConfig();
        if (config != null) {
            boolean updated = updateCondition(id, condition, config.manualRule);
            for (ZenRule automaticRule : config.automaticRules.values()) {
                updated = (updated | updateCondition(id, condition, automaticRule)) | updateSnoozing(automaticRule);
            }
            if (updated) {
                this.mHelper.setConfig(config, "conditionChanged");
            }
        }
    }

    public void onConditionChanged(Condition[] conditions) {
        ZenModeConfig config = this.mHelper.getConfig();
        if (config != null) {
            boolean updated = false;
            for (Condition condition : conditions) {
                updated |= updateCondition(condition.id, condition, config.manualRule);
                for (ZenRule automaticRule : config.automaticRules.values()) {
                    updated = (updated | updateCondition(condition.id, condition, automaticRule)) | updateSnoozing(automaticRule);
                }
            }
            if (updated) {
                this.mHelper.setConfig(config, "conditionChanged");
            }
        }
    }

    private void evaluateRule(ZenRule rule, ArraySet<Uri> current, boolean processSubscriptions) {
        if (rule != null && rule.conditionId != null) {
            Uri id = rule.conditionId;
            boolean isSystemCondition = false;
            for (SystemConditionProviderService sp : this.mConditionProviders.getSystemProviders()) {
                if (sp.isValidConditionId(id)) {
                    this.mConditionProviders.ensureRecordExists(sp.getComponent(), id, sp.asInterface());
                    rule.component = sp.getComponent();
                    isSystemCondition = true;
                }
            }
            if (!isSystemCondition) {
                IConditionProvider cp = this.mConditionProviders.findConditionProvider(rule.component);
                if (DEBUG) {
                    boolean z;
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("Ensure external rule exists: ");
                    if (cp != null) {
                        z = true;
                    } else {
                        z = false;
                    }
                    Log.d(str, append.append(z).append(" for ").append(id).toString());
                }
                if (cp != null) {
                    this.mConditionProviders.ensureRecordExists(rule.component, id, cp);
                }
            }
            if (rule.component == null) {
                Log.w(TAG, "No component found for automatic rule: " + rule.conditionId);
                rule.enabled = false;
                return;
            }
            if (current != null) {
                current.add(id);
            }
            if (processSubscriptions) {
                if (this.mConditionProviders.subscribeIfNecessary(rule.component, rule.conditionId)) {
                    synchronized (this.mSubscriptions) {
                        this.mSubscriptions.put(rule.conditionId, rule.component);
                    }
                } else {
                    rule.condition = null;
                    if (DEBUG) {
                        Log.d(TAG, "zmc failed to subscribe");
                    }
                }
            }
            if (rule.condition == null) {
                rule.condition = this.mConditionProviders.findCondition(rule.component, rule.conditionId);
                if (rule.condition != null && DEBUG) {
                    Log.d(TAG, "Found existing condition for: " + rule.conditionId);
                }
            }
        }
    }

    private boolean isAutomaticActive(ComponentName component) {
        if (component == null) {
            return false;
        }
        ZenModeConfig config = this.mHelper.getConfig();
        if (config == null) {
            return false;
        }
        for (ZenRule rule : config.automaticRules.values()) {
            if (component.equals(rule.component) && rule.isAutomaticActive()) {
                return true;
            }
        }
        return false;
    }

    private boolean updateSnoozing(ZenRule rule) {
        if (rule == null || !rule.snoozing || (!this.mFirstEvaluation && (rule.isTrueOrUnknown() ^ 1) == 0)) {
            return false;
        }
        rule.snoozing = false;
        if (DEBUG) {
            Log.d(TAG, "Snoozing reset for " + rule.conditionId);
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateCondition(Uri id, Condition condition, ZenRule rule) {
        if (id == null || rule == null || rule.conditionId == null || !rule.conditionId.equals(id) || Objects.equals(condition, rule.condition)) {
            return false;
        }
        rule.condition = condition;
        return true;
    }
}
