package com.android.server.telecom;

import android.app.role.OnRoleHoldersChangedListener;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManagerInternal;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.telecom.DefaultDialerManager;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.util.IntArray;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.SmsApplication;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.pm.UserManagerService;
import com.android.server.pm.permission.DefaultPermissionGrantPolicy;
import com.android.server.pm.permission.PermissionManagerServiceInternal;

public class TelecomLoaderService extends SystemService {
    private static final String SERVICE_ACTION = "com.android.ITelecomService";
    private static final ComponentName SERVICE_COMPONENT = new ComponentName("com.android.server.telecom", "com.android.server.telecom.components.TelecomService");
    private static final String TAG = "TelecomLoaderService";
    private final Context mContext;
    @GuardedBy({"mLock"})
    private IntArray mDefaultSimCallManagerRequests;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private TelecomServiceConnection mServiceConnection;

    /* access modifiers changed from: private */
    public class TelecomServiceConnection implements ServiceConnection {
        private TelecomServiceConnection() {
        }

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            PhoneAccountHandle phoneAccount;
            try {
                service.linkToDeath(new IBinder.DeathRecipient() {
                    /* class com.android.server.telecom.TelecomLoaderService.TelecomServiceConnection.AnonymousClass1 */

                    @Override // android.os.IBinder.DeathRecipient
                    public void binderDied() {
                        TelecomLoaderService.this.connectToTelecom();
                    }
                }, 0);
                SmsApplication.getDefaultMmsApplication(TelecomLoaderService.this.mContext, false);
                ServiceManager.addService("telecom", service);
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mDefaultSimCallManagerRequests != null) {
                        DefaultPermissionGrantPolicy permissionPolicy = TelecomLoaderService.this.getDefaultPermissionGrantPolicy();
                        if (!(TelecomLoaderService.this.mDefaultSimCallManagerRequests == null || (phoneAccount = ((TelecomManager) TelecomLoaderService.this.mContext.getSystemService("telecom")).getSimCallManager()) == null)) {
                            int requestCount = TelecomLoaderService.this.mDefaultSimCallManagerRequests.size();
                            String packageName = phoneAccount.getComponentName().getPackageName();
                            for (int i = requestCount - 1; i >= 0; i--) {
                                int userId = TelecomLoaderService.this.mDefaultSimCallManagerRequests.get(i);
                                TelecomLoaderService.this.mDefaultSimCallManagerRequests.remove(i);
                                permissionPolicy.grantDefaultPermissionsToDefaultSimCallManager(packageName, userId);
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TelecomLoaderService.TAG, "Failed linking to death.");
            }
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            TelecomLoaderService.this.connectToTelecom();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private DefaultPermissionGrantPolicy getDefaultPermissionGrantPolicy() {
        return ((PermissionManagerServiceInternal) LocalServices.getService(PermissionManagerServiceInternal.class)).getDefaultPermissionGrantPolicy();
    }

    public TelecomLoaderService(Context context) {
        super(context);
        this.mContext = context;
        registerDefaultAppProviders();
    }

    @Override // com.android.server.SystemService
    public void onStart() {
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 550) {
            registerDefaultAppNotifier();
            registerCarrierConfigChangedReceiver();
            connectToTelecom();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void connectToTelecom() {
        synchronized (this.mLock) {
            if (this.mServiceConnection != null) {
                this.mContext.unbindService(this.mServiceConnection);
                this.mServiceConnection = null;
            }
            TelecomServiceConnection serviceConnection = new TelecomServiceConnection();
            Intent intent = new Intent(SERVICE_ACTION);
            intent.setComponent(SERVICE_COMPONENT);
            if (this.mContext.bindServiceAsUser(intent, serviceConnection, 67108929, UserHandle.SYSTEM)) {
                this.mServiceConnection = serviceConnection;
            }
        }
    }

    private void registerDefaultAppProviders() {
        DefaultPermissionGrantPolicy permissionPolicy = getDefaultPermissionGrantPolicy();
        permissionPolicy.setSmsAppPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.telecom.$$Lambda$TelecomLoaderService$lBXoYxesURvEmfzumX9uIBbg66M */

            public final String[] getPackages(int i) {
                return TelecomLoaderService.this.lambda$registerDefaultAppProviders$0$TelecomLoaderService(i);
            }
        });
        permissionPolicy.setDialerAppPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.telecom.$$Lambda$TelecomLoaderService$VVmvEgI0M6umDuBUYKUoUMO7l0 */

            public final String[] getPackages(int i) {
                return TelecomLoaderService.this.lambda$registerDefaultAppProviders$1$TelecomLoaderService(i);
            }
        });
        permissionPolicy.setSimCallManagerPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* class com.android.server.telecom.$$Lambda$TelecomLoaderService$gelHWcVU9jWWZhCeN99A3Sudtw */

            public final String[] getPackages(int i) {
                return TelecomLoaderService.this.lambda$registerDefaultAppProviders$2$TelecomLoaderService(i);
            }
        });
    }

    public /* synthetic */ String[] lambda$registerDefaultAppProviders$0$TelecomLoaderService(int userId) {
        synchronized (this.mLock) {
            if (this.mServiceConnection == null) {
                return null;
            }
        }
        ComponentName smsComponent = SmsApplication.getDefaultSmsApplication(this.mContext, true);
        if (smsComponent != null) {
            return new String[]{smsComponent.getPackageName()};
        }
        return null;
    }

    public /* synthetic */ String[] lambda$registerDefaultAppProviders$1$TelecomLoaderService(int userId) {
        synchronized (this.mLock) {
            if (this.mServiceConnection == null) {
                return null;
            }
        }
        String packageName = DefaultDialerManager.getDefaultDialerApplication(this.mContext);
        if (packageName != null) {
            return new String[]{packageName};
        }
        return null;
    }

    public /* synthetic */ String[] lambda$registerDefaultAppProviders$2$TelecomLoaderService(int userId) {
        synchronized (this.mLock) {
            if (this.mServiceConnection == null) {
                if (this.mDefaultSimCallManagerRequests == null) {
                    this.mDefaultSimCallManagerRequests = new IntArray();
                }
                this.mDefaultSimCallManagerRequests.add(userId);
                return null;
            }
        }
        PhoneAccountHandle phoneAccount = ((TelecomManager) this.mContext.getSystemService("telecom")).getSimCallManager(userId);
        if (phoneAccount != null) {
            return new String[]{phoneAccount.getComponentName().getPackageName()};
        }
        return null;
    }

    private void registerDefaultAppNotifier() {
        ((RoleManager) this.mContext.getSystemService(RoleManager.class)).addOnRoleHoldersChangedListenerAsUser(this.mContext.getMainExecutor(), new OnRoleHoldersChangedListener(getDefaultPermissionGrantPolicy()) {
            /* class com.android.server.telecom.$$Lambda$TelecomLoaderService$JaEag0KH0v0eOJ4BOrxYzuIZXXo */
            private final /* synthetic */ DefaultPermissionGrantPolicy f$1;

            {
                this.f$1 = r2;
            }

            public final void onRoleHoldersChanged(String str, UserHandle userHandle) {
                TelecomLoaderService.this.lambda$registerDefaultAppNotifier$3$TelecomLoaderService(this.f$1, str, userHandle);
            }
        }, UserHandle.ALL);
    }

    public /* synthetic */ void lambda$registerDefaultAppNotifier$3$TelecomLoaderService(DefaultPermissionGrantPolicy permissionPolicy, String roleName, UserHandle user) {
        updateSimCallManagerPermissions(permissionPolicy, user.getIdentifier());
    }

    private void registerCarrierConfigChangedReceiver() {
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            /* class com.android.server.telecom.TelecomLoaderService.AnonymousClass1 */

            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    int[] userIds = UserManagerService.getInstance().getUserIds();
                    for (int userId : userIds) {
                        TelecomLoaderService telecomLoaderService = TelecomLoaderService.this;
                        telecomLoaderService.updateSimCallManagerPermissions(telecomLoaderService.getDefaultPermissionGrantPolicy(), userId);
                    }
                }
            }
        }, UserHandle.ALL, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"), null, null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSimCallManagerPermissions(DefaultPermissionGrantPolicy permissionGrantPolicy, int userId) {
        PhoneAccountHandle phoneAccount = ((TelecomManager) this.mContext.getSystemService("telecom")).getSimCallManager(userId);
        if (phoneAccount != null) {
            Slog.i(TAG, "updating sim call manager permissions for userId:" + userId);
            permissionGrantPolicy.grantDefaultPermissionsToDefaultSimCallManager(phoneAccount.getComponentName().getPackageName(), userId);
        }
    }
}
