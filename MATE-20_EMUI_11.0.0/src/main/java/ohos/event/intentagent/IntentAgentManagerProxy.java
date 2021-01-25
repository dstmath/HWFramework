package ohos.event.intentagent;

import android.app.PendingIntent;
import java.util.Optional;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.event.intentagent.IntentAgent;
import ohos.eventhandler.EventHandler;

/* access modifiers changed from: package-private */
public class IntentAgentManagerProxy {
    private static final IntentAgentManagerProxy INSTANCE = new IntentAgentManagerProxy();
    private static final int INVALID_VALUE = -1;
    private IntentAgentAdapter adapter = IntentAgentAdapter.getInstance();

    private IntentAgentManagerProxy() {
    }

    static IntentAgentManagerProxy getInstance() {
        return INSTANCE;
    }

    /* access modifiers changed from: package-private */
    public IntentAgent getIntentAgent(Context context, IntentAgentInfo intentAgentInfo) {
        return this.adapter.getIntentAgent(context, intentAgentInfo).orElse(null);
    }

    /* access modifiers changed from: package-private */
    public void triggerIntentAgent(Context context, IntentAgent intentAgent, IntentAgent.OnCompleted onCompleted, EventHandler eventHandler, TriggerInfo triggerInfo) {
        this.adapter.triggerIntentAgent(context, intentAgent, onCompleted, eventHandler, triggerInfo);
    }

    /* access modifiers changed from: package-private */
    public void cancel(IntentAgent intentAgent) {
        PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent(intentAgent);
        if (pendingIntent != null) {
            pendingIntent.cancel();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean judgeEquality(IntentAgent intentAgent, IntentAgent intentAgent2) {
        return this.adapter.judgeEquality(intentAgent, intentAgent2);
    }

    /* access modifiers changed from: package-private */
    public int getHashCode(IntentAgent intentAgent) {
        PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent(intentAgent);
        if (pendingIntent == null) {
            return -1;
        }
        return pendingIntent.hashCode();
    }

    /* access modifiers changed from: package-private */
    public String getBundleName(IntentAgent intentAgent) {
        PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent(intentAgent);
        if (pendingIntent == null) {
            return null;
        }
        return pendingIntent.getCreatorPackage();
    }

    /* access modifiers changed from: package-private */
    public int getUid(IntentAgent intentAgent) {
        PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent(intentAgent);
        if (pendingIntent == null) {
            return -1;
        }
        return pendingIntent.getCreatorUid();
    }

    /* access modifiers changed from: package-private */
    public int getUserHandler(IntentAgent intentAgent) {
        return getUid(intentAgent);
    }

    /* access modifiers changed from: package-private */
    public Optional<Intent> getIntent(IntentAgent intentAgent) {
        PendingIntent pendingIntent = IntentAgentAdapterUtils.getPendingIntent(intentAgent);
        if (pendingIntent == null) {
            return Optional.empty();
        }
        android.content.Intent intent = pendingIntent.getIntent();
        if (intent == null) {
            return Optional.empty();
        }
        return IntentAgentAdapter.getZidaneIntent(intent, this.adapter.getType(pendingIntent));
    }
}
