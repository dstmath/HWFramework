package com.android.server.location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ILocationProviderManager;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.FgThread;
import com.android.server.LocationManagerService;
import com.android.server.ServiceWatcher;
import com.android.server.location.AbstractLocationProvider;
import com.android.server.pm.DumpState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LocationProviderProxy extends AbstractLocationProvider {
    private static final boolean D = LocationManagerService.D;
    private static final String TAG = "LocationProviderProxy";
    private final ILocationProviderManager.Stub mManager;
    @GuardedBy({"mProviderPackagesLock"})
    private final CopyOnWriteArrayList<String> mProviderPackages;
    private final Object mProviderPackagesLock;
    @GuardedBy({"mRequestLock"})
    private ProviderRequest mRequest;
    private final Object mRequestLock;
    private final ServiceWatcher mServiceWatcher;
    @GuardedBy({"mRequestLock"})
    private WorkSource mWorkSource;

    public static LocationProviderProxy createAndBind(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        LocationProviderProxy proxy = new LocationProviderProxy(context, locationProviderManager, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private LocationProviderProxy(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId) {
        super(context, locationProviderManager);
        this.mProviderPackagesLock = new Object();
        this.mRequestLock = new Object();
        this.mManager = new ILocationProviderManager.Stub() {
            /* class com.android.server.location.LocationProviderProxy.AnonymousClass1 */

            public void onSetAdditionalProviderPackages(List<String> packageNames) {
                LocationProviderProxy.this.onSetAdditionalProviderPackages(packageNames);
            }

            public void onSetEnabled(boolean enabled) {
                LocationProviderProxy.this.setEnabled(enabled);
            }

            public void onSetProperties(ProviderProperties properties) {
                LocationProviderProxy.this.setProperties(properties);
            }

            public void onReportLocation(Location location) {
                LocationProviderProxy.this.reportLocation(location);
            }
        };
        this.mProviderPackages = new CopyOnWriteArrayList<>();
        this.mServiceWatcher = new ServiceWatcher(context, TAG, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, FgThread.getHandler()) {
            /* class com.android.server.location.LocationProviderProxy.AnonymousClass2 */

            /* access modifiers changed from: protected */
            @Override // com.android.server.ServiceWatcher
            public void onBind() {
                runOnBinder(new ServiceWatcher.BinderRunner() {
                    /* class com.android.server.location.$$Lambda$LocationProviderProxy$2$QT3uzVX4fLIc1b7F_cP9P1hzluA */

                    @Override // com.android.server.ServiceWatcher.BinderRunner
                    public final void run(IBinder iBinder) {
                        LocationProviderProxy.this.initializeService(iBinder);
                    }
                });
            }

            /* access modifiers changed from: protected */
            @Override // com.android.server.ServiceWatcher
            public void onUnbind() {
                LocationProviderProxy.this.resetProviderPackages(Collections.emptyList());
                LocationProviderProxy.this.setEnabled(false);
                LocationProviderProxy.this.setProperties(null);
            }
        };
        this.mRequest = null;
        this.mWorkSource = new WorkSource();
    }

    protected LocationProviderProxy(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        super(context, locationProviderManager);
        this.mProviderPackagesLock = new Object();
        this.mRequestLock = new Object();
        this.mManager = new ILocationProviderManager.Stub() {
            /* class com.android.server.location.LocationProviderProxy.AnonymousClass1 */

            public void onSetAdditionalProviderPackages(List<String> packageNames) {
                LocationProviderProxy.this.onSetAdditionalProviderPackages(packageNames);
            }

            public void onSetEnabled(boolean enabled) {
                LocationProviderProxy.this.setEnabled(enabled);
            }

            public void onSetProperties(ProviderProperties properties) {
                LocationProviderProxy.this.setProperties(properties);
            }

            public void onReportLocation(Location location) {
                LocationProviderProxy.this.reportLocation(location);
            }
        };
        this.mProviderPackages = new CopyOnWriteArrayList<>();
        this.mServiceWatcher = null;
        this.mRequest = null;
        this.mWorkSource = new WorkSource();
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void initializeService(IBinder binder) throws RemoteException {
        ILocationProvider service = ILocationProvider.Stub.asInterface(binder);
        if (D) {
            Log.d(TAG, "applying state to connected service " + this.mServiceWatcher);
        }
        resetProviderPackages(Collections.emptyList());
        service.setLocationProviderManager(this.mManager);
        synchronized (this.mRequestLock) {
            if (this.mRequest != null) {
                service.setRequest(this.mRequest, this.mWorkSource);
            }
        }
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public List<String> getProviderPackages() {
        CopyOnWriteArrayList<String> copyOnWriteArrayList;
        synchronized (this.mProviderPackagesLock) {
            copyOnWriteArrayList = this.mProviderPackages;
        }
        return copyOnWriteArrayList;
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void setRequest(ProviderRequest request, WorkSource source) {
        synchronized (this.mRequestLock) {
            this.mRequest = request;
            this.mWorkSource = source;
        }
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner(request, source) {
            /* class com.android.server.location.$$Lambda$LocationProviderProxy$p3DjIvk7Of_sUF4Dc9plMNfdklc */
            private final /* synthetic */ ProviderRequest f$0;
            private final /* synthetic */ WorkSource f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // com.android.server.ServiceWatcher.BinderRunner
            public final void run(IBinder iBinder) {
                ILocationProvider.Stub.asInterface(iBinder).setRequest(this.f$0, this.f$1);
            }
        });
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("    service=" + this.mServiceWatcher);
        synchronized (this.mProviderPackagesLock) {
            if (this.mProviderPackages.size() > 1) {
                pw.println("    additional packages=" + this.mProviderPackages);
            }
        }
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public int getStatus(Bundle extras) {
        return ((Integer) this.mServiceWatcher.runOnBinderBlocking(new ServiceWatcher.BlockingBinderRunner(extras) {
            /* class com.android.server.location.$$Lambda$LocationProviderProxy$HNmfjzTDtRS07pxKSpIAhcUh88Y */
            private final /* synthetic */ Bundle f$0;

            {
                this.f$0 = r1;
            }

            @Override // com.android.server.ServiceWatcher.BlockingBinderRunner
            public final Object run(IBinder iBinder) {
                return Integer.valueOf(ILocationProvider.Stub.asInterface(iBinder).getStatus(this.f$0));
            }
        }, 1)).intValue();
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public long getStatusUpdateTime() {
        return ((Long) this.mServiceWatcher.runOnBinderBlocking($$Lambda$LocationProviderProxy$UCv9FaTEgs0wfXFwhEpgptlzgk.INSTANCE, 0L)).longValue();
    }

    @Override // com.android.server.location.AbstractLocationProvider
    public void sendExtraCommand(String command, Bundle extras) {
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner(command, extras) {
            /* class com.android.server.location.$$Lambda$LocationProviderProxy$QOuHAndhADLzUnTK39JocbxRlQY */
            private final /* synthetic */ String f$0;
            private final /* synthetic */ Bundle f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // com.android.server.ServiceWatcher.BinderRunner
            public final void run(IBinder iBinder) {
                ILocationProvider.Stub.asInterface(iBinder).sendExtraCommand(this.f$0, this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSetAdditionalProviderPackages(List<String> packageNames) {
        resetProviderPackages(packageNames);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetProviderPackages(List<String> additionalPackageNames) {
        ArrayList<String> permittedPackages = new ArrayList<>(additionalPackageNames.size());
        for (String packageName : additionalPackageNames) {
            try {
                this.mContext.getPackageManager().getPackageInfo(packageName, DumpState.DUMP_DEXOPT);
                permittedPackages.add(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(TAG, this.mServiceWatcher + " specified unknown additional provider package: " + packageName);
            }
        }
        synchronized (this.mProviderPackagesLock) {
            this.mProviderPackages.clear();
            String myPackage = this.mServiceWatcher.getCurrentPackageName();
            if (myPackage != null) {
                this.mProviderPackages.add(myPackage);
                this.mProviderPackages.addAll(permittedPackages);
            }
        }
    }
}
