package ohos.event.intentagent;

import android.app.PendingIntent;

public final class IntentAgentAdapterUtils {
    public static IntentAgent getIntentAgent(PendingIntent pendingIntent) {
        if (pendingIntent == null) {
            return null;
        }
        return new IntentAgent(pendingIntent);
    }

    public static PendingIntent getPendingIntent(IntentAgent intentAgent) {
        if (intentAgent == null) {
            return null;
        }
        Object object = intentAgent.getObject();
        if (!(object instanceof PendingIntent)) {
            return null;
        }
        return (PendingIntent) object;
    }

    private IntentAgentAdapterUtils() {
    }
}
