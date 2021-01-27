package android.app;

import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ReceiverCallNotAllowedException;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.UserHandle;
import java.util.concurrent.Executor;

/* access modifiers changed from: package-private */
/* compiled from: ContextImpl */
public class ReceiverRestrictedContext extends ContextWrapper {
    @UnsupportedAppUsage
    ReceiverRestrictedContext(Context base) {
        super(base);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return registerReceiver(receiver, filter, null, null);
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (receiver == null) {
            return super.registerReceiver(null, filter, broadcastPermission, scheduler);
        }
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to register to receive intents");
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public Intent registerReceiverAsUser(BroadcastReceiver receiver, UserHandle user, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        if (receiver == null) {
            return super.registerReceiverAsUser(null, user, filter, broadcastPermission, scheduler);
        }
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to register to receive intents");
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to bind to services");
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean bindService(Intent service, int flags, Executor executor, ServiceConnection conn) {
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to bind to services");
    }

    @Override // android.content.ContextWrapper, android.content.Context
    public boolean bindIsolatedService(Intent service, int flags, String instanceName, Executor executor, ServiceConnection conn) {
        throw new ReceiverCallNotAllowedException("BroadcastReceiver components are not allowed to bind to services");
    }
}
