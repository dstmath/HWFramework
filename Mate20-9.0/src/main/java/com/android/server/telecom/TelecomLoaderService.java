package com.android.server.telecom;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManagerInternal;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
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

public class TelecomLoaderService extends SystemService {
    private static final String SERVICE_ACTION = "com.android.ITelecomService";
    private static final ComponentName SERVICE_COMPONENT = new ComponentName("com.android.server.telecom", "com.android.server.telecom.components.TelecomService");
    private static final String TAG = "TelecomLoaderService";
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IntArray mDefaultDialerAppRequests;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IntArray mDefaultSimCallManagerRequests;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IntArray mDefaultSmsAppRequests;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public TelecomServiceConnection mServiceConnection;

    private class TelecomServiceConnection implements ServiceConnection {
        private TelecomServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                service.linkToDeath(new IBinder.DeathRecipient() {
                    public void binderDied() {
                        TelecomLoaderService.this.connectToTelecom();
                    }
                }, 0);
                SmsApplication.getDefaultMmsApplication(TelecomLoaderService.this.mContext, false);
                ServiceManager.addService("telecom", service);
                synchronized (TelecomLoaderService.this.mLock) {
                    if (!(TelecomLoaderService.this.mDefaultSmsAppRequests == null && TelecomLoaderService.this.mDefaultDialerAppRequests == null && TelecomLoaderService.this.mDefaultSimCallManagerRequests == null)) {
                        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                        if (TelecomLoaderService.this.mDefaultSmsAppRequests != null) {
                            ComponentName smsComponent = SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true);
                            if (smsComponent != null) {
                                for (int i = TelecomLoaderService.this.mDefaultSmsAppRequests.size() - 1; i >= 0; i--) {
                                    int userid = TelecomLoaderService.this.mDefaultSmsAppRequests.get(i);
                                    TelecomLoaderService.this.mDefaultSmsAppRequests.remove(i);
                                    packageManagerInternal.grantDefaultPermissionsToDefaultSmsApp(smsComponent.getPackageName(), userid);
                                }
                            }
                        }
                        if (TelecomLoaderService.this.mDefaultDialerAppRequests != null) {
                            String packageName = DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext);
                            if (packageName != null) {
                                for (int i2 = TelecomLoaderService.this.mDefaultDialerAppRequests.size() - 1; i2 >= 0; i2--) {
                                    int userId = TelecomLoaderService.this.mDefaultDialerAppRequests.get(i2);
                                    TelecomLoaderService.this.mDefaultDialerAppRequests.remove(i2);
                                    packageManagerInternal.grantDefaultPermissionsToDefaultDialerApp(packageName, userId);
                                }
                            }
                        }
                        if (TelecomLoaderService.this.mDefaultSimCallManagerRequests != null) {
                            PhoneAccountHandle phoneAccount = ((TelecomManager) TelecomLoaderService.this.mContext.getSystemService("telecom")).getSimCallManager();
                            if (phoneAccount != null) {
                                int requestCount = TelecomLoaderService.this.mDefaultSimCallManagerRequests.size();
                                String packageName2 = phoneAccount.getComponentName().getPackageName();
                                for (int i3 = requestCount - 1; i3 >= 0; i3--) {
                                    int userId2 = TelecomLoaderService.this.mDefaultSimCallManagerRequests.get(i3);
                                    TelecomLoaderService.this.mDefaultSimCallManagerRequests.remove(i3);
                                    packageManagerInternal.grantDefaultPermissionsToDefaultSimCallManager(packageName2, userId2);
                                }
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Slog.w(TelecomLoaderService.TAG, "Failed linking to death.");
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            TelecomLoaderService.this.connectToTelecom();
        }
    }

    public TelecomLoaderService(Context context) {
        super(context);
        this.mContext = context;
        registerDefaultAppProviders();
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == 550) {
            registerDefaultAppNotifier();
            registerCarrierConfigChangedReceiver();
            connectToTelecom();
        }
    }

    /* access modifiers changed from: private */
    public void connectToTelecom() {
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
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        packageManagerInternal.setSmsAppPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* JADX WARNING: Code restructure failed: missing block: B:12:0x002e, code lost:
                r0 = com.android.internal.telephony.SmsApplication.getDefaultSmsApplication(com.android.server.telecom.TelecomLoaderService.access$100(r4.this$0), true);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0039, code lost:
                if (r0 == null) goto L_0x0045;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x0044, code lost:
                return new java.lang.String[]{r0.getPackageName()};
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0045, code lost:
                return null;
             */
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultSmsAppRequests == null) {
                            IntArray unused = TelecomLoaderService.this.mDefaultSmsAppRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultSmsAppRequests.add(userId);
                        return null;
                    }
                }
            }
        });
        packageManagerInternal.setDialerAppPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* JADX WARNING: Code restructure failed: missing block: B:12:0x002e, code lost:
                r0 = android.telecom.DefaultDialerManager.getDefaultDialerApplication(com.android.server.telecom.TelecomLoaderService.access$100(r4.this$0));
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
                if (r0 == null) goto L_0x0041;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x0040, code lost:
                return new java.lang.String[]{r0};
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0041, code lost:
                return null;
             */
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultDialerAppRequests == null) {
                            IntArray unused = TelecomLoaderService.this.mDefaultDialerAppRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultDialerAppRequests.add(userId);
                        return null;
                    }
                }
            }
        });
        packageManagerInternal.setSimCallManagerPackagesProvider(new PackageManagerInternal.PackagesProvider() {
            /* JADX WARNING: Code restructure failed: missing block: B:12:0x002e, code lost:
                r1 = ((android.telecom.TelecomManager) com.android.server.telecom.TelecomLoaderService.access$100(r5.this$0).getSystemService("telecom")).getSimCallManager(r6);
             */
            /* JADX WARNING: Code restructure failed: missing block: B:13:0x0041, code lost:
                if (r1 == null) goto L_0x0052;
             */
            /* JADX WARNING: Code restructure failed: missing block: B:15:0x0051, code lost:
                return new java.lang.String[]{r1.getComponentName().getPackageName()};
             */
            /* JADX WARNING: Code restructure failed: missing block: B:16:0x0052, code lost:
                return null;
             */
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultSimCallManagerRequests == null) {
                            IntArray unused = TelecomLoaderService.this.mDefaultSimCallManagerRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultSimCallManagerRequests.add(userId);
                        return null;
                    }
                }
            }
        });
    }

    private void registerDefaultAppNotifier() {
        Uri defaultSmsAppUri = Settings.Secure.getUriFor("sms_default_application");
        Uri defaultDialerAppUri = Settings.Secure.getUriFor("dialer_default_application");
        final Uri uri = defaultSmsAppUri;
        final PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        final Uri uri2 = defaultDialerAppUri;
        AnonymousClass4 r1 = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (uri.equals(uri)) {
                    int setupCompleted = Settings.Secure.getInt(TelecomLoaderService.this.mContext.getContentResolver(), "user_setup_complete", 0);
                    if (SystemProperties.getInt("ro.config.skip_grant_cust_sms", 0) == 1 && setupCompleted == 0) {
                        Slog.i(TelecomLoaderService.TAG, "skip grantDefaultPermissionsToDefaultSmsApp before OOBE completed");
                        return;
                    }
                    Slog.i(TelecomLoaderService.TAG, "not skip grantDefaultPermissionsToDefaultSmsApp");
                    ComponentName smsComponent = SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true);
                    if (smsComponent != null) {
                        packageManagerInternal.grantDefaultPermissionsToDefaultSmsApp(smsComponent.getPackageName(), userId);
                    }
                } else if (uri2.equals(uri)) {
                    String packageName = DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext);
                    if (packageName != null) {
                        packageManagerInternal.grantDefaultPermissionsToDefaultDialerApp(packageName, userId);
                    }
                    TelecomLoaderService.this.updateSimCallManagerPermissions(packageManagerInternal, userId);
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(defaultSmsAppUri, false, r1, -1);
        this.mContext.getContentResolver().registerContentObserver(defaultDialerAppUri, false, r1, -1);
    }

    private void registerCarrierConfigChangedReceiver() {
        final PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                    for (int userId : UserManagerService.getInstance().getUserIds()) {
                        TelecomLoaderService.this.updateSimCallManagerPermissions(packageManagerInternal, userId);
                    }
                }
            }
        }, UserHandle.ALL, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"), null, null);
    }

    /* access modifiers changed from: private */
    public void updateSimCallManagerPermissions(PackageManagerInternal packageManagerInternal, int userId) {
        PhoneAccountHandle phoneAccount = ((TelecomManager) this.mContext.getSystemService("telecom")).getSimCallManager(userId);
        if (phoneAccount != null) {
            Slog.i(TAG, "updating sim call manager permissions for userId:" + userId);
            packageManagerInternal.grantDefaultPermissionsToDefaultSimCallManager(phoneAccount.getComponentName().getPackageName(), userId);
        }
    }
}
