package com.android.server.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;
import com.android.internal.location.ILocationProvider;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.internal.os.TransferPipe;
import com.android.server.ServiceWatcher;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;

public class LocationProviderProxy implements LocationProviderInterface {
    private static final boolean D = false;
    private static final String TAG = "LocationProviderProxy";
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean mEnabled = false;
    /* access modifiers changed from: private */
    public Object mLock = new Object();
    private final String mName;
    private Runnable mNewServiceWork = new Runnable() {
        public void run() {
            final boolean enabled;
            final ProviderRequest request;
            final WorkSource source;
            ProviderProperties[] properties = new ProviderProperties[1];
            synchronized (LocationProviderProxy.this.mLock) {
                enabled = LocationProviderProxy.this.mEnabled;
                request = LocationProviderProxy.this.mRequest;
                source = LocationProviderProxy.this.mWorksource;
            }
            ServiceWatcher access$400 = LocationProviderProxy.this.mServiceWatcher;
            final ProviderProperties[] providerPropertiesArr = properties;
            AnonymousClass1 r1 = new ServiceWatcher.BinderRunner() {
                public void run(IBinder binder) {
                    ILocationProvider service = ILocationProvider.Stub.asInterface(binder);
                    try {
                        providerPropertiesArr[0] = service.getProperties();
                        if (providerPropertiesArr[0] == null) {
                            Log.e(LocationProviderProxy.TAG, LocationProviderProxy.this.mServiceWatcher.getBestPackageName() + " has invalid location provider properties");
                        }
                        if (enabled) {
                            service.enable();
                            if (request != null) {
                                service.setRequest(request, source);
                            }
                        }
                    } catch (RemoteException e) {
                        Log.w(LocationProviderProxy.TAG, e);
                    } catch (Exception e2) {
                        Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                    }
                }
            };
            access$400.runOnBinder(r1);
            synchronized (LocationProviderProxy.this.mLock) {
                ProviderProperties unused = LocationProviderProxy.this.mProperties = properties[0];
            }
        }
    };
    /* access modifiers changed from: private */
    public ProviderProperties mProperties;
    /* access modifiers changed from: private */
    public ProviderRequest mRequest = null;
    /* access modifiers changed from: private */
    public final ServiceWatcher mServiceWatcher;
    /* access modifiers changed from: private */
    public WorkSource mWorksource = new WorkSource();

    public static LocationProviderProxy createAndBind(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        LocationProviderProxy proxy = new LocationProviderProxy(context, name, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, handler);
        if (proxy.bind()) {
            return proxy;
        }
        return null;
    }

    private LocationProviderProxy(Context context, String name, String action, int overlaySwitchResId, int defaultServicePackageNameResId, int initialPackageNamesResId, Handler handler) {
        String str = name;
        this.mContext = context;
        this.mName = str;
        Context context2 = this.mContext;
        ServiceWatcher serviceWatcher = new ServiceWatcher(context2, "LocationProviderProxy-" + str, action, overlaySwitchResId, defaultServicePackageNameResId, initialPackageNamesResId, this.mNewServiceWork, handler);
        this.mServiceWatcher = serviceWatcher;
    }

    protected LocationProviderProxy(Context context, String name) {
        this.mContext = context;
        this.mName = name;
        this.mServiceWatcher = null;
    }

    private boolean bind() {
        return this.mServiceWatcher.start();
    }

    public String getConnectedPackageName() {
        return this.mServiceWatcher.getBestPackageName();
    }

    public String getName() {
        return this.mName;
    }

    public ProviderProperties getProperties() {
        ProviderProperties providerProperties;
        synchronized (this.mLock) {
            providerProperties = this.mProperties;
        }
        return providerProperties;
    }

    public void enable() {
        synchronized (this.mLock) {
            this.mEnabled = true;
        }
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    ILocationProvider.Stub.asInterface(binder).enable();
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
            }
        });
    }

    public void disable() {
        synchronized (this.mLock) {
            this.mEnabled = false;
        }
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    ILocationProvider.Stub.asInterface(binder).disable();
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
            }
        });
    }

    public boolean isEnabled() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mEnabled;
        }
        return z;
    }

    public void setRequest(final ProviderRequest request, final WorkSource source) {
        synchronized (this.mLock) {
            this.mRequest = request;
            this.mWorksource = source;
        }
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    ILocationProvider.Stub.asInterface(binder).setRequest(request, source);
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
            }
        });
    }

    public void dump(final FileDescriptor fd, final PrintWriter pw, final String[] args) {
        pw.append("REMOTE SERVICE");
        pw.append(" name=").append(this.mName);
        pw.append(" pkg=").append(this.mServiceWatcher.getBestPackageName());
        PrintWriter append = pw.append(" version=");
        append.append(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS + this.mServiceWatcher.getBestVersion());
        pw.append(10);
        if (!this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    TransferPipe.dumpAsync(ILocationProvider.Stub.asInterface(binder).asBinder(), fd, args);
                } catch (RemoteException | IOException e) {
                    PrintWriter printWriter = pw;
                    printWriter.println("Failed to dump location provider: " + e);
                }
            }
        })) {
            pw.println("service down (null)");
        }
    }

    public int getStatus(final Bundle extras) {
        final int[] result = {1};
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    result[0] = ILocationProvider.Stub.asInterface(binder).getStatus(extras);
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
            }
        });
        return result[0];
    }

    public long getStatusUpdateTime() {
        final long[] result = {0};
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    result[0] = ILocationProvider.Stub.asInterface(binder).getStatusUpdateTime();
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
            }
        });
        return result[0];
    }

    public boolean sendExtraCommand(final String command, final Bundle extras) {
        final boolean[] result = {false};
        this.mServiceWatcher.runOnBinder(new ServiceWatcher.BinderRunner() {
            public void run(IBinder binder) {
                try {
                    result[0] = ILocationProvider.Stub.asInterface(binder).sendExtraCommand(command, extras);
                } catch (RemoteException e) {
                    Log.w(LocationProviderProxy.TAG, e);
                } catch (Exception e2) {
                    Log.e(LocationProviderProxy.TAG, "Exception from " + LocationProviderProxy.this.mServiceWatcher.getBestPackageName(), e2);
                }
            }
        });
        return result[0];
    }
}
