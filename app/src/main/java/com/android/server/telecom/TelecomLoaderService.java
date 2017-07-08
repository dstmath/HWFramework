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
    private static final ComponentName SERVICE_COMPONENT = null;
    private static final String TAG = "TelecomLoaderService";
    private final Context mContext;
    @GuardedBy("mLock")
    private IntArray mDefaultDialerAppRequests;
    @GuardedBy("mLock")
    private IntArray mDefaultSimCallManagerRequests;
    @GuardedBy("mLock")
    private IntArray mDefaultSmsAppRequests;
    private final Object mLock;
    @GuardedBy("mLock")
    private TelecomServiceConnection mServiceConnection;

    /* renamed from: com.android.server.telecom.TelecomLoaderService.4 */
    class AnonymousClass4 extends ContentObserver {
        final /* synthetic */ Uri val$defaultDialerAppUri;
        final /* synthetic */ Uri val$defaultSmsAppUri;
        final /* synthetic */ PackageManagerInternal val$packageManagerInternal;

        AnonymousClass4(Handler $anonymous0, Uri val$defaultSmsAppUri, PackageManagerInternal val$packageManagerInternal, Uri val$defaultDialerAppUri) {
            this.val$defaultSmsAppUri = val$defaultSmsAppUri;
            this.val$packageManagerInternal = val$packageManagerInternal;
            this.val$defaultDialerAppUri = val$defaultDialerAppUri;
            super($anonymous0);
        }

        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (this.val$defaultSmsAppUri.equals(uri)) {
                ComponentName smsComponent = SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true);
                if (smsComponent != null) {
                    this.val$packageManagerInternal.grantDefaultPermissionsToDefaultSmsApp(smsComponent.getPackageName(), userId);
                }
            } else if (this.val$defaultDialerAppUri.equals(uri)) {
                String packageName = DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext);
                if (packageName != null) {
                    this.val$packageManagerInternal.grantDefaultPermissionsToDefaultDialerApp(packageName, userId);
                }
                TelecomLoaderService.this.updateSimCallManagerPermissions(this.val$packageManagerInternal, userId);
            }
        }
    }

    /* renamed from: com.android.server.telecom.TelecomLoaderService.5 */
    class AnonymousClass5 extends BroadcastReceiver {
        final /* synthetic */ PackageManagerInternal val$packageManagerInternal;

        AnonymousClass5(PackageManagerInternal val$packageManagerInternal) {
            this.val$packageManagerInternal = val$packageManagerInternal;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.telephony.action.CARRIER_CONFIG_CHANGED")) {
                for (int userId : UserManagerService.getInstance().getUserIds()) {
                    TelecomLoaderService.this.updateSimCallManagerPermissions(this.val$packageManagerInternal, userId);
                }
            }
        }
    }

    private class TelecomServiceConnection implements ServiceConnection {
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.telecom.TelecomLoaderService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.telecom.TelecomLoaderService.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.telecom.TelecomLoaderService.<clinit>():void");
    }

    public TelecomLoaderService(Context context) {
        super(context);
        this.mLock = new Object();
        this.mContext = context;
        registerDefaultAppProviders();
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_ACTIVITY_MANAGER_READY) {
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
        packageManagerInternal.setSmsAppPackagesProvider(new PackagesProvider() {
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultSmsAppRequests == null) {
                            TelecomLoaderService.this.mDefaultSmsAppRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultSmsAppRequests.add(userId);
                        return null;
                    }
                    if (SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true) == null) {
                        return null;
                    }
                    return new String[]{SmsApplication.getDefaultSmsApplication(TelecomLoaderService.this.mContext, true).getPackageName()};
                }
            }
        });
        packageManagerInternal.setDialerAppPackagesProvider(new PackagesProvider() {
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultDialerAppRequests == null) {
                            TelecomLoaderService.this.mDefaultDialerAppRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultDialerAppRequests.add(userId);
                        return null;
                    }
                    if (DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext) == null) {
                        return null;
                    }
                    return new String[]{DefaultDialerManager.getDefaultDialerApplication(TelecomLoaderService.this.mContext)};
                }
            }
        });
        packageManagerInternal.setSimCallManagerPackagesProvider(new PackagesProvider() {
            public String[] getPackages(int userId) {
                synchronized (TelecomLoaderService.this.mLock) {
                    if (TelecomLoaderService.this.mServiceConnection == null) {
                        if (TelecomLoaderService.this.mDefaultSimCallManagerRequests == null) {
                            TelecomLoaderService.this.mDefaultSimCallManagerRequests = new IntArray();
                        }
                        TelecomLoaderService.this.mDefaultSimCallManagerRequests.add(userId);
                        return null;
                    }
                    if (((TelecomManager) TelecomLoaderService.this.mContext.getSystemService("telecom")).getSimCallManager(userId) == null) {
                        return null;
                    }
                    return new String[]{((TelecomManager) TelecomLoaderService.this.mContext.getSystemService("telecom")).getSimCallManager(userId).getComponentName().getPackageName()};
                }
            }
        });
    }

    private void registerDefaultAppNotifier() {
        PackageManagerInternal packageManagerInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        Uri defaultSmsAppUri = Secure.getUriFor("sms_default_application");
        Uri defaultDialerAppUri = Secure.getUriFor("dialer_default_application");
        ContentObserver contentObserver = new AnonymousClass4(new Handler(Looper.getMainLooper()), defaultSmsAppUri, packageManagerInternal, defaultDialerAppUri);
        this.mContext.getContentResolver().registerContentObserver(defaultSmsAppUri, false, contentObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(defaultDialerAppUri, false, contentObserver, -1);
    }

    private void registerCarrierConfigChangedReceiver() {
        this.mContext.registerReceiverAsUser(new AnonymousClass5((PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class)), UserHandle.ALL, new IntentFilter("android.telephony.action.CARRIER_CONFIG_CHANGED"), null, null);
    }

    private void updateSimCallManagerPermissions(PackageManagerInternal packageManagerInternal, int userId) {
        PhoneAccountHandle phoneAccount = ((TelecomManager) this.mContext.getSystemService("telecom")).getSimCallManager(userId);
        if (phoneAccount != null) {
            Slog.i(TAG, "updating sim call manager permissions for userId:" + userId);
            packageManagerInternal.grantDefaultPermissionsToDefaultSimCallManager(phoneAccount.getComponentName().getPackageName(), userId);
        }
    }
}
