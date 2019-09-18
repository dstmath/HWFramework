package com.android.server.autofill;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.os.UserHandle;
import android.service.autofill.IAutofillFieldClassificationService;
import android.util.Log;
import android.util.Slog;
import android.view.autofill.AutofillValue;
import com.android.internal.annotations.GuardedBy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class FieldClassificationStrategy {
    private static final String TAG = "FieldClassificationStrategy";
    private final Context mContext;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public ArrayList<Command> mQueuedCommands;
    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public IAutofillFieldClassificationService mRemoteService;
    @GuardedBy("mLock")
    private ServiceConnection mServiceConnection;
    private final int mUserId;

    private interface Command {
        void run(IAutofillFieldClassificationService iAutofillFieldClassificationService) throws RemoteException;
    }

    private interface MetadataParser<T> {
        T get(Resources resources, int i);
    }

    public FieldClassificationStrategy(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
    }

    /* access modifiers changed from: package-private */
    public ServiceInfo getServiceInfo() {
        String packageName = this.mContext.getPackageManager().getServicesSystemSharedLibraryPackageName();
        if (packageName == null) {
            Slog.w(TAG, "no external services package!");
            return null;
        }
        Intent intent = new Intent("android.service.autofill.AutofillFieldClassificationService");
        intent.setPackage(packageName);
        ResolveInfo resolveInfo = this.mContext.getPackageManager().resolveService(intent, 132);
        if (resolveInfo != null && resolveInfo.serviceInfo != null) {
            return resolveInfo.serviceInfo;
        }
        Slog.w(TAG, "No valid components found.");
        return null;
    }

    private ComponentName getServiceComponentName() {
        ServiceInfo serviceInfo = getServiceInfo();
        if (serviceInfo == null) {
            return null;
        }
        ComponentName name = new ComponentName(serviceInfo.packageName, serviceInfo.name);
        if (!"android.permission.BIND_AUTOFILL_FIELD_CLASSIFICATION_SERVICE".equals(serviceInfo.permission)) {
            Slog.w(TAG, name.flattenToShortString() + " does not require permission " + "android.permission.BIND_AUTOFILL_FIELD_CLASSIFICATION_SERVICE");
            return null;
        }
        if (Helper.sVerbose) {
            Slog.v(TAG, "getServiceComponentName(): " + name);
        }
        return name;
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        synchronized (this.mLock) {
            if (this.mServiceConnection != null) {
                if (Helper.sDebug) {
                    Slog.d(TAG, "reset(): unbinding service.");
                }
                this.mContext.unbindService(this.mServiceConnection);
                this.mServiceConnection = null;
            } else if (Helper.sDebug) {
                Slog.d(TAG, "reset(): service is not bound. Do nothing.");
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        android.util.Slog.w(TAG, "exception calling service: " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00af, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00b0, code lost:
        android.os.Binder.restoreCallingIdentity(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b3, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00b5, code lost:
        return;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:5:0x0007, B:34:0x0093] */
    private void connectAndRun(Command command) {
        synchronized (this.mLock) {
            if (this.mRemoteService != null) {
                if (Helper.sVerbose) {
                    Slog.v(TAG, "running command right away");
                }
                command.run(this.mRemoteService);
            } else {
                if (Helper.sDebug) {
                    Slog.d(TAG, "service is null; queuing command");
                }
                if (this.mQueuedCommands == null) {
                    this.mQueuedCommands = new ArrayList<>(1);
                }
                this.mQueuedCommands.add(command);
                if (this.mServiceConnection == null) {
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "creating connection");
                    }
                    this.mServiceConnection = new ServiceConnection() {
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            if (Helper.sVerbose) {
                                Slog.v(FieldClassificationStrategy.TAG, "onServiceConnected(): " + name);
                            }
                            synchronized (FieldClassificationStrategy.this.mLock) {
                                IAutofillFieldClassificationService unused = FieldClassificationStrategy.this.mRemoteService = IAutofillFieldClassificationService.Stub.asInterface(service);
                                if (FieldClassificationStrategy.this.mQueuedCommands != null) {
                                    int size = FieldClassificationStrategy.this.mQueuedCommands.size();
                                    if (Helper.sDebug) {
                                        Slog.d(FieldClassificationStrategy.TAG, "running " + size + " queued commands");
                                    }
                                    for (int i = 0; i < size; i++) {
                                        Command queuedCommand = (Command) FieldClassificationStrategy.this.mQueuedCommands.get(i);
                                        try {
                                            if (Helper.sVerbose) {
                                                Slog.v(FieldClassificationStrategy.TAG, "running queued command #" + i);
                                            }
                                            queuedCommand.run(FieldClassificationStrategy.this.mRemoteService);
                                        } catch (RemoteException e) {
                                            Slog.w(FieldClassificationStrategy.TAG, "exception calling " + name + ": " + e);
                                        }
                                    }
                                    ArrayList unused2 = FieldClassificationStrategy.this.mQueuedCommands = null;
                                } else if (Helper.sDebug) {
                                    Slog.d(FieldClassificationStrategy.TAG, "no queued commands");
                                }
                            }
                        }

                        public void onServiceDisconnected(ComponentName name) {
                            if (Helper.sVerbose) {
                                Slog.v(FieldClassificationStrategy.TAG, "onServiceDisconnected(): " + name);
                            }
                            synchronized (FieldClassificationStrategy.this.mLock) {
                                IAutofillFieldClassificationService unused = FieldClassificationStrategy.this.mRemoteService = null;
                            }
                        }

                        public void onBindingDied(ComponentName name) {
                            if (Helper.sVerbose) {
                                Slog.v(FieldClassificationStrategy.TAG, "onBindingDied(): " + name);
                            }
                            synchronized (FieldClassificationStrategy.this.mLock) {
                                IAutofillFieldClassificationService unused = FieldClassificationStrategy.this.mRemoteService = null;
                            }
                        }

                        public void onNullBinding(ComponentName name) {
                            if (Helper.sVerbose) {
                                Slog.v(FieldClassificationStrategy.TAG, "onNullBinding(): " + name);
                            }
                            synchronized (FieldClassificationStrategy.this.mLock) {
                                IAutofillFieldClassificationService unused = FieldClassificationStrategy.this.mRemoteService = null;
                            }
                        }
                    };
                    ComponentName component = getServiceComponentName();
                    if (Helper.sVerbose) {
                        Slog.v(TAG, "binding to: " + component);
                    }
                    if (component != null) {
                        Intent intent = new Intent();
                        intent.setComponent(component);
                        long token = Binder.clearCallingIdentity();
                        this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1, UserHandle.of(this.mUserId));
                        if (Helper.sVerbose) {
                            Slog.v(TAG, "bound");
                        }
                        Binder.restoreCallingIdentity(token);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String[] getAvailableAlgorithms() {
        return (String[]) getMetadataValue("android.autofill.field_classification.available_algorithms", $$Lambda$FieldClassificationStrategy$NQQgQ63vxhPkiwOWrnwRyuYSHTM.INSTANCE);
    }

    /* access modifiers changed from: package-private */
    public String getDefaultAlgorithm() {
        return (String) getMetadataValue("android.autofill.field_classification.default_algorithm", $$Lambda$FieldClassificationStrategy$vGIL1YGX_9ksoSV74T7gO4fkEBE.INSTANCE);
    }

    private <T> T getMetadataValue(String field, MetadataParser<T> parser) {
        ServiceInfo serviceInfo = getServiceInfo();
        if (serviceInfo == null) {
            return null;
        }
        try {
            return parser.get(this.mContext.getPackageManager().getResourcesForApplication(serviceInfo.applicationInfo), serviceInfo.metaData.getInt(field));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error getting application resources for " + serviceInfo, e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public void getScores(RemoteCallback callback, String algorithmName, Bundle algorithmArgs, List<AutofillValue> actualValues, String[] userDataValues) {
        $$Lambda$FieldClassificationStrategy$gMvCLMmbA3vJ4CLoCwKkhYrCMsQ r0 = new Command(callback, algorithmName, algorithmArgs, actualValues, userDataValues) {
            private final /* synthetic */ RemoteCallback f$0;
            private final /* synthetic */ String f$1;
            private final /* synthetic */ Bundle f$2;
            private final /* synthetic */ List f$3;
            private final /* synthetic */ String[] f$4;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run(IAutofillFieldClassificationService iAutofillFieldClassificationService) {
                iAutofillFieldClassificationService.getScores(this.f$0, this.f$1, this.f$2, this.f$3, this.f$4);
            }
        };
        connectAndRun(r0);
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter pw) {
        ComponentName impl = getServiceComponentName();
        pw.print(prefix);
        pw.print("User ID: ");
        pw.println(this.mUserId);
        pw.print(prefix);
        pw.print("Queued commands: ");
        if (this.mQueuedCommands == null) {
            pw.println("N/A");
        } else {
            pw.println(this.mQueuedCommands.size());
        }
        pw.print(prefix);
        pw.print("Implementation: ");
        if (impl == null) {
            pw.println("N/A");
            return;
        }
        pw.println(impl.flattenToShortString());
        pw.print(prefix);
        pw.print("Available algorithms: ");
        pw.println(Arrays.toString(getAvailableAlgorithms()));
        pw.print(prefix);
        pw.print("Default algorithm: ");
        pw.println(getDefaultAlgorithm());
    }
}
