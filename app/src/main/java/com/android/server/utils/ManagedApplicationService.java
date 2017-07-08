package com.android.server.utils;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.IInterface;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Slog;
import java.util.Objects;

public class ManagedApplicationService {
    private final String TAG;
    private IInterface mBoundInterface;
    private final BinderChecker mChecker;
    private final int mClientLabel;
    private final ComponentName mComponent;
    private ServiceConnection mConnection;
    private final Context mContext;
    private final DeathRecipient mDeathRecipient;
    private final Object mLock;
    private ServiceConnection mPendingConnection;
    private PendingEvent mPendingEvent;
    private final String mSettingsAction;
    private final int mUserId;

    /* renamed from: com.android.server.utils.ManagedApplicationService.2 */
    class AnonymousClass2 implements ServiceConnection {
        final /* synthetic */ Intent val$intent;

        AnonymousClass2(Intent val$intent) {
            this.val$intent = val$intent;
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IInterface iface = null;
            PendingEvent pendingEvent = null;
            synchronized (ManagedApplicationService.this.mLock) {
                if (ManagedApplicationService.this.mPendingConnection == this) {
                    ManagedApplicationService.this.mPendingConnection = null;
                    ManagedApplicationService.this.mConnection = this;
                    try {
                        iBinder.linkToDeath(ManagedApplicationService.this.mDeathRecipient, 0);
                        ManagedApplicationService.this.mBoundInterface = ManagedApplicationService.this.mChecker.asInterface(iBinder);
                        if (!ManagedApplicationService.this.mChecker.checkType(ManagedApplicationService.this.mBoundInterface)) {
                            ManagedApplicationService.this.mContext.unbindService(this);
                            ManagedApplicationService.this.mBoundInterface = null;
                        }
                        iface = ManagedApplicationService.this.mBoundInterface;
                        pendingEvent = ManagedApplicationService.this.mPendingEvent;
                        ManagedApplicationService.this.mPendingEvent = null;
                    } catch (RemoteException e) {
                        Slog.w(ManagedApplicationService.this.TAG, "Unable to bind service: " + this.val$intent, e);
                        ManagedApplicationService.this.mBoundInterface = null;
                    }
                    if (!(iface == null || pendingEvent == null)) {
                        try {
                            pendingEvent.runEvent(iface);
                        } catch (Exception ex) {
                            Slog.e(ManagedApplicationService.this.TAG, "Received exception from user service: ", ex);
                        }
                    }
                    return;
                }
                ManagedApplicationService.this.mContext.unbindService(this);
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            Slog.w(ManagedApplicationService.this.TAG, "Service disconnected: " + this.val$intent);
            ManagedApplicationService.this.mConnection = null;
            ManagedApplicationService.this.mBoundInterface = null;
        }
    }

    public interface BinderChecker {
        IInterface asInterface(IBinder iBinder);

        boolean checkType(IInterface iInterface);
    }

    public interface PendingEvent {
        void runEvent(IInterface iInterface) throws RemoteException;
    }

    private ManagedApplicationService(Context context, ComponentName component, int userId, int clientLabel, String settingsAction, BinderChecker binderChecker) {
        this.TAG = getClass().getSimpleName();
        this.mDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                synchronized (ManagedApplicationService.this.mLock) {
                    ManagedApplicationService.this.mBoundInterface = null;
                }
            }
        };
        this.mLock = new Object();
        this.mContext = context;
        this.mComponent = component;
        this.mUserId = userId;
        this.mClientLabel = clientLabel;
        this.mSettingsAction = settingsAction;
        this.mChecker = binderChecker;
    }

    public static ManagedApplicationService build(Context context, ComponentName component, int userId, int clientLabel, String settingsAction, BinderChecker binderChecker) {
        return new ManagedApplicationService(context, component, userId, clientLabel, settingsAction, binderChecker);
    }

    public int getUserId() {
        return this.mUserId;
    }

    public ComponentName getComponent() {
        return this.mComponent;
    }

    public boolean disconnectIfNotMatching(ComponentName componentName, int userId) {
        if (matches(componentName, userId)) {
            return false;
        }
        disconnect();
        return true;
    }

    public void sendEvent(PendingEvent event) {
        synchronized (this.mLock) {
            IInterface iface = this.mBoundInterface;
            if (iface == null) {
                this.mPendingEvent = event;
            }
        }
        if (iface != null) {
            try {
                event.runEvent(iface);
            } catch (Exception ex) {
                Slog.e(this.TAG, "Received exception from user service: ", ex);
            }
        }
    }

    public void disconnect() {
        synchronized (this.mLock) {
            this.mPendingConnection = null;
            if (this.mConnection != null) {
                this.mContext.unbindService(this.mConnection);
                this.mConnection = null;
            }
            this.mBoundInterface = null;
        }
    }

    public void connect() {
        synchronized (this.mLock) {
            if (this.mConnection == null && this.mPendingConnection == null) {
                Intent intent = new Intent().setComponent(this.mComponent).putExtra("android.intent.extra.client_label", this.mClientLabel).putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent(this.mSettingsAction), 0));
                ServiceConnection serviceConnection = new AnonymousClass2(intent);
                this.mPendingConnection = serviceConnection;
                try {
                    if (!this.mContext.bindServiceAsUser(intent, serviceConnection, 67108865, new UserHandle(this.mUserId))) {
                        Slog.w(this.TAG, "Unable to bind service: " + intent);
                    }
                } catch (SecurityException e) {
                    Slog.w(this.TAG, "Unable to bind service: " + intent, e);
                }
                return;
            }
        }
    }

    private boolean matches(ComponentName component, int userId) {
        return Objects.equals(this.mComponent, component) && this.mUserId == userId;
    }
}
