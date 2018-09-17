package com.android.server.telecom;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageManagerInternal.PackagesProvider;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
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
    private final Context mContext;
    @GuardedBy("mLock")
    private IntArray mDefaultDialerAppRequests;
    @GuardedBy("mLock")
    private IntArray mDefaultSimCallManagerRequests;
    @GuardedBy("mLock")
    private IntArray mDefaultSmsAppRequests;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private TelecomServiceConnection mServiceConnection;

    private class TelecomServiceConnection implements ServiceConnection {
        /* synthetic */ TelecomServiceConnection(TelecomLoaderService this$0, TelecomServiceConnection -this1) {
            this();
        }

        private TelecomServiceConnection() {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                service.linkToDeath(new DeathRecipient() {
                    public void binderDied() {
                        TelecomLoaderService.this.connectToTelecom();
                    }
                }, 0);
                SmsApplication.getDefaultMmsApplication(TelecomLoaderService.this.mContext, false);
                ServiceManager.addService("telecom", service);
                synchronized (TelecomLoaderService.this.mLock) {
                    if (!(TelecomLoaderService.this.mDefaultSmsAppRequests == null && TelecomLoaderService.this.mDefaultDialerAppRequests == null && TelecomLoaderService.this.mDefaultSimCallManagerRequests == null)) {
                        int i;
                        String packageName;
                        int userId;
                        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
                        if (TelecomLoaderService.this.mDefaultSmsAppRequests != null) {
                            ComponentName smsComponent = SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true);
                            if (smsComponent != null) {
                                for (i = TelecomLoaderService.this.mDefaultSmsAppRequests.size() - 1; i >= 0; i--) {
                                    int userid = TelecomLoaderService.this.mDefaultSmsAppRequests.get(i);
                                    TelecomLoaderService.this.mDefaultSmsAppRequests.remove(i);
                                    packageManagerInternal.grantDefaultPermissionsToDefaultSmsApp(smsComponent.getPackageName(), userid);
                                }
                            }
                        }
                        if (TelecomLoaderService.this.mDefaultDialerAppRequests != null) {
                            packageName = DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext);
                            if (packageName != null) {
                                for (i = TelecomLoaderService.this.mDefaultDialerAppRequests.size() - 1; i >= 0; i--) {
                                    userId = TelecomLoaderService.this.mDefaultDialerAppRequests.get(i);
                                    TelecomLoaderService.this.mDefaultDialerAppRequests.remove(i);
                                    packageManagerInternal.grantDefaultPermissionsToDefaultDialerApp(packageName, userId);
                                }
                            }
                        }
                        if (TelecomLoaderService.this.mDefaultSimCallManagerRequests != null) {
                            PhoneAccountHandle phoneAccount = ((TelecomManager) TelecomLoaderService.this.mContext.getSystemService("telecom")).getSimCallManager();
                            if (phoneAccount != null) {
                                int requestCount = TelecomLoaderService.this.mDefaultSimCallManagerRequests.size();
                                packageName = phoneAccount.getComponentName().getPackageName();
                                for (i = requestCount - 1; i >= 0; i--) {
                                    userId = TelecomLoaderService.this.mDefaultSimCallManagerRequests.get(i);
                                    TelecomLoaderService.this.mDefaultSimCallManagerRequests.remove(i);
                                    packageManagerInternal.grantDefaultPermissionsToDefaultSimCallManager(packageName, userId);
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

    private void connectToTelecom() {
        synchronized (this.mLock) {
            if (this.mServiceConnection != null) {
                this.mContext.unbindService(this.mServiceConnection);
                this.mServiceConnection = null;
            }
            TelecomServiceConnection serviceConnection = new TelecomServiceConnection(this, null);
            Intent intent = new Intent(SERVICE_ACTION);
            intent.setComponent(SERVICE_COMPONENT);
            if (this.mContext.bindServiceAsUser(intent, serviceConnection, 67108929, UserHandle.SYSTEM)) {
                this.mServiceConnection = serviceConnection;
            }
        }
    }

    private void registerDefaultAppProviders() {
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        packageManagerInternal.setSmsAppPackagesProvider(new PackagesProvider() {
            /* JADX WARNING: Missing block: B:13:0x0039, code:
            if (com.android.internal.telephony.SmsApplication.getDefaultSmsApplication(com.android.server.telecom.TelecomLoaderService.-get0(r5.this$0), true) == null) goto L_0x0048;
     */
            /* JADX WARNING: Missing block: B:15:0x0044, code:
            return new java.lang.String[]{com.android.internal.telephony.SmsApplication.getDefaultSmsApplication(com.android.server.telecom.TelecomLoaderService.-get0(r5.this$0), true).getPackageName()};
     */
            /* JADX WARNING: Missing block: B:19:0x0048, code:
            return null;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultSmsAppRequests == null) {
                            TelecomLoaderService.this.mDefaultSmsAppRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultSmsAppRequests.add(userId);
                        return null;
                    }
                }
            }
        });
        packageManagerInternal.setDialerAppPackagesProvider(new PackagesProvider() {
            /* JADX WARNING: Missing block: B:13:0x0038, code:
            if (android.telecom.DefaultDialerManager.getDefaultDialerApplication(com.android.server.telecom.TelecomLoaderService.-get0(r5.this$0)) == null) goto L_0x0044;
     */
            /* JADX WARNING: Missing block: B:15:0x0040, code:
            return new java.lang.String[]{android.telecom.DefaultDialerManager.getDefaultDialerApplication(com.android.server.telecom.TelecomLoaderService.-get0(r5.this$0))};
     */
            /* JADX WARNING: Missing block: B:19:0x0044, code:
            return null;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultDialerAppRequests == null) {
                            TelecomLoaderService.this.mDefaultDialerAppRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultDialerAppRequests.add(userId);
                        return null;
                    }
                }
            }
        });
        packageManagerInternal.setSimCallManagerPackagesProvider(new PackagesProvider() {
            /* JADX WARNING: Missing block: B:13:0x0041, code:
            if (((android.telecom.TelecomManager) com.android.server.telecom.TelecomLoaderService.-get0(r6.this$0).getSystemService("telecom")).getSimCallManager(r7) == null) goto L_0x0055;
     */
            /* JADX WARNING: Missing block: B:15:0x0051, code:
            return new java.lang.String[]{((android.telecom.TelecomManager) com.android.server.telecom.TelecomLoaderService.-get0(r6.this$0).getSystemService("telecom")).getSimCallManager(r7).getComponentName().getPackageName()};
     */
            /* JADX WARNING: Missing block: B:19:0x0055, code:
            return null;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultSimCallManagerRequests == null) {
                            TelecomLoaderService.this.mDefaultSimCallManagerRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultSimCallManagerRequests.add(userId);
                        return null;
                    }
                }
            }
        });
    }

    private void registerDefaultAppNotifier() {
        final PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        final Uri defaultSmsAppUri = Secure.getUriFor("sms_default_application");
        final Uri defaultDialerAppUri = Secure.getUriFor("dialer_default_application");
        ContentObserver contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            public void onChange(boolean selfChange, Uri uri, int userId) {
                if (defaultSmsAppUri.equals(uri)) {
                    int setupCompleted = Secure.getInt(TelecomLoaderService.this.mContext.getContentResolver(), "user_setup_complete", 0);
                    if (SystemProperties.getInt("ro.config.skip_grant_cust_sms", 0) == 1 && setupCompleted == 0) {
                        Slog.i(TelecomLoaderService.TAG, "skip grantDefaultPermissionsToDefaultSmsApp before OOBE completed");
                        return;
                    }
                    Slog.i(TelecomLoaderService.TAG, "not skip grantDefaultPermissionsToDefaultSmsApp");
                    ComponentName smsComponent = SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true);
                    if (smsComponent != null) {
                        packageManagerInternal.grantDefaultPermissionsToDefaultSmsApp(smsComponent.getPackageName(), userId);
                    }
                } else if (defaultDialerAppUri.equals(uri)) {
                    String packageName = DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext);
                    if (packageName != null) {
                        packageManagerInternal.grantDefaultPermissionsToDefaultDialerApp(packageName, userId);
                    }
                    TelecomLoaderService.this.updateSimCallManagerPermissions(packageManagerInternal, userId);
                }
            }
        };
        this.mContext.getContentResolver().registerContentObserver(defaultSmsAppUri, false, contentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(defaultDialerAppUri, false, contentObserver, -1);
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

    private void updateSimCallManagerPermissions(PackageManagerInternal packageManagerInternal, int userId) {
        PhoneAccountHandle phoneAccount = ((TelecomManager) this.mContext.getSystemService("telecom")).getSimCallManager(userId);
        if (phoneAccount != null) {
            Slog.i(TAG, "updating sim call manager permissions for userId:" + userId);
            packageManagerInternal.grantDefaultPermissionsToDefaultSimCallManager(phoneAccount.getComponentName().getPackageName(), userId);
        }
    }
}
