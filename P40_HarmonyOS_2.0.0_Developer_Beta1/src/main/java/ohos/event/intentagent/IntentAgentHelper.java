package ohos.event.intentagent;

import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.event.intentagent.IntentAgent;
import ohos.eventhandler.EventHandler;

public final class IntentAgentHelper {
    private IntentAgentHelper() {
    }

    public static IntentAgent getIntentAgent(Context context, IntentAgentInfo intentAgentInfo) {
        return IntentAgentManagerProxy.getInstance().getIntentAgent(context, intentAgentInfo);
    }

    public static void triggerIntentAgent(Context context, IntentAgent intentAgent, IntentAgent.OnCompleted onCompleted, EventHandler eventHandler, TriggerInfo triggerInfo) {
        IntentAgentManagerProxy.getInstance().triggerIntentAgent(context, intentAgent, onCompleted, eventHandler, triggerInfo);
    }

    public static void cancel(IntentAgent intentAgent) {
        IntentAgentManagerProxy.getInstance().cancel(intentAgent);
    }

    public static boolean judgeEquality(IntentAgent intentAgent, IntentAgent intentAgent2) {
        return IntentAgentManagerProxy.getInstance().judgeEquality(intentAgent, intentAgent2);
    }

    public static int getHashCode(IntentAgent intentAgent) {
        return IntentAgentManagerProxy.getInstance().getHashCode(intentAgent);
    }

    public static String getBundleName(IntentAgent intentAgent) {
        return IntentAgentManagerProxy.getInstance().getBundleName(intentAgent);
    }

    public static int getUid(IntentAgent intentAgent) {
        return IntentAgentManagerProxy.getInstance().getUid(intentAgent);
    }

    public static int getUserHandler(IntentAgent intentAgent) {
        return IntentAgentManagerProxy.getInstance().getUserHandler(intentAgent);
    }

    public static Intent getIntent(IntentAgent intentAgent) {
        return IntentAgentManagerProxy.getInstance().getIntent(intentAgent).orElse(null);
    }
}
