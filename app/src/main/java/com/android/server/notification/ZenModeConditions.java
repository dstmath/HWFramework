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
    private static final boolean DEBUG = false;
    private static final String TAG = "ZenModeHelper";
    private final ConditionProviders mConditionProviders;
    private boolean mFirstEvaluation;
    private final ZenModeHelper mHelper;
    private final ArrayMap<Uri, ComponentName> mSubscriptions;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.notification.ZenModeConditions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.notification.ZenModeConditions.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ZenModeConditions.<clinit>():void");
    }

    public ZenModeConditions(ZenModeHelper helper, ConditionProviders conditionProviders) {
        this.mSubscriptions = new ArrayMap();
        this.mFirstEvaluation = true;
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
            if (!(config.manualRule == null || config.manualRule.condition == null || config.manualRule.isTrueOrUnknown())) {
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
            for (int i = this.mSubscriptions.size() - 1; i >= 0; i--) {
                Uri id = (Uri) this.mSubscriptions.keyAt(i);
                ComponentName component = (ComponentName) this.mSubscriptions.valueAt(i);
                if (processSubscriptions && !current.contains(id)) {
                    this.mConditionProviders.unsubscribeIfNecessary(component, id);
                    this.mSubscriptions.removeAt(i);
                }
            }
            this.mFirstEvaluation = DEBUG;
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
        this.mHelper.setConfigAsync(this.mHelper.getConfig(), "zmc.onServiceAdded");
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
                this.mHelper.setConfigAsync(config, "conditionChanged");
            }
        }
    }

    public void onConditionChanged(Condition[] conditions) {
        ZenModeConfig config = this.mHelper.getConfig();
        if (config != null) {
            boolean updated = DEBUG;
            for (Condition condition : conditions) {
                updated |= updateCondition(condition.id, condition, config.manualRule);
                for (ZenRule automaticRule : config.automaticRules.values()) {
                    updated = (updated | updateCondition(condition.id, condition, automaticRule)) | updateSnoozing(automaticRule);
                }
            }
            if (updated) {
                this.mHelper.setConfigAsync(config, "conditionChanged");
            }
        }
    }

    private void evaluateRule(ZenRule rule, ArraySet<Uri> current, boolean processSubscriptions) {
        if (rule != null && rule.conditionId != null) {
            Uri id = rule.conditionId;
            boolean isSystemCondition = DEBUG;
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
                        z = DEBUG;
                    }
                    Log.d(str, append.append(z).append(" for ").append(id).toString());
                }
                if (cp != null) {
                    this.mConditionProviders.ensureRecordExists(rule.component, id, cp);
                }
            }
            if (rule.component == null) {
                Log.w(TAG, "No component found for automatic rule: " + rule.conditionId);
                rule.enabled = DEBUG;
                return;
            }
            if (current != null) {
                current.add(id);
            }
            if (processSubscriptions) {
                if (this.mConditionProviders.subscribeIfNecessary(rule.component, rule.conditionId)) {
                    this.mSubscriptions.put(rule.conditionId, rule.component);
                } else if (DEBUG) {
                    Log.d(TAG, "zmc failed to subscribe");
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
            return DEBUG;
        }
        ZenModeConfig config = this.mHelper.getConfig();
        if (config == null) {
            return DEBUG;
        }
        for (ZenRule rule : config.automaticRules.values()) {
            if (component.equals(rule.component) && rule.isAutomaticActive()) {
                return true;
            }
        }
        return DEBUG;
    }

    private boolean updateSnoozing(ZenRule rule) {
        if (rule == null || !rule.snoozing || (!this.mFirstEvaluation && rule.isTrueOrUnknown())) {
            return DEBUG;
        }
        rule.snoozing = DEBUG;
        if (DEBUG) {
            Log.d(TAG, "Snoozing reset for " + rule.conditionId);
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateCondition(Uri id, Condition condition, ZenRule rule) {
        if (id == null || rule == null || rule.conditionId == null || !rule.conditionId.equals(id) || Objects.equals(condition, rule.condition)) {
            return DEBUG;
        }
        rule.condition = condition;
        return true;
    }
}
