package android.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.UserHandle;

/* compiled from: ContextImpl */
class ReceiverRestrictedContext extends ContextWrapper {
    ReceiverRestrictedContext(Context base) {
        super(base);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiver(receiver, filter, null, null);
    }

    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (receiver == null) {
            return super.registerReceiver(null, filter, broadcastPermission, scheduler);
        }
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to register to receive intents");
    }

    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (receiver == null) {
            return super.registerReceiverAsUser(null, user, filter, broadcastPermission, scheduler);
        }
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to register to receive intents");
    }

    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to bind to services");
    }
}
