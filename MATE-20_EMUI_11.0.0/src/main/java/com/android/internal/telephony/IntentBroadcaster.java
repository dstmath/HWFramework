package com.android.internal.telephony;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import com.huawei.internal.telephony.IccCardConstantsEx;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class IntentBroadcaster {
    private static final String TAG = "IntentBroadcaster";
    private static IntentBroadcaster sIntentBroadcaster;
    private Map<Integer, Intent> mRebroadcastIntents = new HashMap();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.internal.telephony.IntentBroadcaster.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.USER_UNLOCKED")) {
                synchronized (IntentBroadcaster.this.mRebroadcastIntents) {
                    Iterator iterator = IntentBroadcaster.this.mRebroadcastIntents.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry pair = (Map.Entry) iterator.next();
                        Intent i = (Intent) pair.getValue();
                        i.putExtra("rebroadcastOnUnlock", true);
                        iterator.remove();
                        IntentBroadcaster intentBroadcaster = IntentBroadcaster.this;
                        intentBroadcaster.logd("Rebroadcasting intent " + i.getAction() + " " + i.getStringExtra(IccCardConstantsEx.INTENT_KEY_ICC_STATE) + " for slotId " + pair.getKey());
                        ActivityManager.broadcastStickyIntent(i, -1);
                    }
                }
            }
        }
    };

    private IntentBroadcaster(Context context) {
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.USER_UNLOCKED"));
    }

    public static IntentBroadcaster getInstance(Context context) {
        if (sIntentBroadcaster == null) {
            sIntentBroadcaster = new IntentBroadcaster(context);
        }
        return sIntentBroadcaster;
    }

    public static IntentBroadcaster getInstance() {
        return sIntentBroadcaster;
    }

    public void broadcastStickyIntent(Intent intent, int slotId) {
        logd("Broadcasting and adding intent for rebroadcast: " + intent.getAction() + " " + intent.getStringExtra(IccCardConstantsEx.INTENT_KEY_ICC_STATE) + " for slotId " + slotId);
        synchronized (this.mRebroadcastIntents) {
            ActivityManager.broadcastStickyIntent(intent, -1);
            this.mRebroadcastIntents.put(Integer.valueOf(slotId), intent);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logd(String s) {
        Log.d(TAG, s);
    }
}
