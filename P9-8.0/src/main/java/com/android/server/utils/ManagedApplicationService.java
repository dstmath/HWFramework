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
    private final String TAG = getClass().getSimpleName();
    private IInterface mBoundInterface;
    private final BinderChecker mChecker;
    private final int mClientLabel;
    private final ComponentName mComponent;
    private ServiceConnection mConnection;
    private final Context mContext;
    private final DeathRecipient mDeathRecipient = new DeathRecipient() {
        public void binderDied() {
            synchronized (ManagedApplicationService.this.mLock) {
                ManagedApplicationService.this.mBoundInterface = null;
            }
        }
    };
    private final Object mLock = new Object();
    private ServiceConnection mPendingConnection;
    private PendingEvent mPendingEvent;
    private final String mSettingsAction;
    private final int mUserId;

    public interface BinderChecker {
        IInterface asInterface(IBinder iBinder);

        boolean checkType(IInterface iInterface);
    }

    public interface PendingEvent {
        void runEvent(IInterface iInterface) throws RemoteException;
    }

    private ManagedApplicationService(Context context, ComponentName component, int userId, int clientLabel, String settingsAction, BinderChecker binderChecker) {
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

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0013 A:{Splitter: B:8:0x000c, ExcHandler: java.lang.RuntimeException (r0_0 'ex' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:13:0x0013, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:14:0x0014, code:
            android.util.Slog.e(r4.TAG, "Received exception from user service: ", r0);
     */
    /* JADX WARNING: Missing block: B:17:?, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendEvent(PendingEvent event) {
        IInterface iface;
        synchronized (this.mLock) {
            iface = this.mBoundInterface;
            if (iface == null) {
                this.mPendingEvent = event;
            }
        }
        if (iface != null) {
            try {
                event.runEvent(iface);
            } catch (Exception ex) {
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

    /* JADX WARNING: Missing block: B:8:0x000c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void connect() {
        synchronized (this.mLock) {
            if (this.mConnection == null && this.mPendingConnection == null) {
                final Intent intent = new Intent().setComponent(this.mComponent).putExtra("android.intent.extra.client_label", this.mClientLabel).putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(this.mContext, 0, new Intent(this.mSettingsAction), 0));
                ServiceConnection serviceConnection = new ServiceConnection() {
                    /* JADX WARNING: Removed duplicated region for block: B:27:0x00a6 A:{Splitter: B:14:0x006d, ExcHandler: java.lang.RuntimeException (r1_0 'ex' java.lang.Exception)} */
                    /* JADX WARNING: Missing block: B:27:0x00a6, code:
            r1 = move-exception;
     */
                    /* JADX WARNING: Missing block: B:28:0x00a7, code:
            android.util.Slog.e(com.android.server.utils.ManagedApplicationService.-get0(r8.this$0), "Received exception from user service: ", r1);
     */
                    /* Code decompiled incorrectly, please refer to instructions dump. */
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
                                    Slog.w(ManagedApplicationService.this.TAG, "Unable to bind service: " + intent, e);
                                    ManagedApplicationService.this.mBoundInterface = null;
                                }
                            } else {
                                ManagedApplicationService.this.mContext.unbindService(this);
                                return;
                            }
                        }
                        if (!(iface == null || pendingEvent == null)) {
                            try {
                                pendingEvent.runEvent(iface);
                            } catch (Exception ex) {
                            }
                        }
                    }

                    public void onServiceDisconnected(ComponentName componentName) {
                        Slog.w(ManagedApplicationService.this.TAG, "Service disconnected: " + intent);
                        ManagedApplicationService.this.mConnection = null;
                        ManagedApplicationService.this.mBoundInterface = null;
                    }
                };
                this.mPendingConnection = serviceConnection;
                try {
                    if (!this.mContext.bindServiceAsUser(intent, serviceConnection, 67108865, new UserHandle(this.mUserId))) {
                        Slog.w(this.TAG, "Unable to bind service: " + intent);
                    }
                } catch (SecurityException e) {
                    Slog.w(this.TAG, "Unable to bind service: " + intent, e);
                }
            }
        }
    }

    private boolean matches(ComponentName component, int userId) {
        return Objects.equals(this.mComponent, component) && this.mUserId == userId;
    }
}
