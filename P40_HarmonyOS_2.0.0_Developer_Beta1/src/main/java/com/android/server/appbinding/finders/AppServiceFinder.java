package com.android.server.appbinding.finders;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.GuardedBy;
import com.android.server.appbinding.AppBindingConstants;
import com.android.server.appbinding.AppBindingUtils;
import java.io.PrintWriter;
import java.util.function.BiConsumer;

public abstract class AppServiceFinder<TServiceType, TServiceInterfaceType extends IInterface> {
    protected static final boolean DEBUG = false;
    protected static final String TAG = "AppBindingService";
    protected final Context mContext;
    protected final Handler mHandler;
    @GuardedBy({"mLock"})
    private final SparseArray<String> mLastMessages = new SparseArray<>(4);
    protected final BiConsumer<AppServiceFinder, Integer> mListener;
    private final Object mLock = new Object();
    @GuardedBy({"mLock"})
    private final SparseArray<String> mTargetPackages = new SparseArray<>(4);
    @GuardedBy({"mLock"})
    private final SparseArray<ServiceInfo> mTargetServices = new SparseArray<>(4);

    public abstract TServiceInterfaceType asInterface(IBinder iBinder);

    public abstract String getAppDescription();

    public abstract int getBindFlags(AppBindingConstants appBindingConstants);

    /* access modifiers changed from: protected */
    public abstract String getServiceAction();

    /* access modifiers changed from: protected */
    public abstract Class<TServiceType> getServiceClass();

    /* access modifiers changed from: protected */
    public abstract String getServicePermission();

    public abstract String getTargetPackage(int i);

    public AppServiceFinder(Context context, BiConsumer<AppServiceFinder, Integer> listener, Handler callbackHandler) {
        this.mContext = context;
        this.mListener = listener;
        this.mHandler = callbackHandler;
    }

    /* access modifiers changed from: protected */
    public boolean isEnabled(AppBindingConstants constants) {
        return true;
    }

    public void startMonitoring() {
    }

    public void onUserRemoved(int userId) {
        synchronized (this.mLock) {
            this.mTargetPackages.delete(userId);
            this.mTargetServices.delete(userId);
            this.mLastMessages.delete(userId);
        }
    }

    public final ServiceInfo findService(int userId, IPackageManager ipm, AppBindingConstants constants) {
        synchronized (this.mLock) {
            this.mTargetPackages.put(userId, null);
            this.mTargetServices.put(userId, null);
            this.mLastMessages.put(userId, null);
            if (!isEnabled(constants)) {
                this.mLastMessages.put(userId, "feature disabled");
                Slog.i("AppBindingService", getAppDescription() + " feature disabled");
                return null;
            }
            String targetPackage = getTargetPackage(userId);
            if (targetPackage == null) {
                this.mLastMessages.put(userId, "Target package not found");
                Slog.w("AppBindingService", getAppDescription() + " u" + userId + " Target package not found");
                return null;
            }
            this.mTargetPackages.put(userId, targetPackage);
            StringBuilder errorMessage = new StringBuilder();
            ServiceInfo service = AppBindingUtils.findService(targetPackage, userId, getServiceAction(), getServicePermission(), getServiceClass(), ipm, errorMessage);
            if (service == null) {
                this.mLastMessages.put(userId, errorMessage.toString());
                return null;
            }
            String error = validateService(service);
            if (error != null) {
                this.mLastMessages.put(userId, error);
                Log.e("AppBindingService", error);
                return null;
            }
            this.mLastMessages.put(userId, "Valid service found");
            this.mTargetServices.put(userId, service);
            return service;
        }
    }

    /* access modifiers changed from: protected */
    public String validateService(ServiceInfo service) {
        return null;
    }

    public void dump(String prefix, PrintWriter pw) {
        pw.print(prefix);
        pw.print("App type: ");
        pw.print(getAppDescription());
        pw.println();
        synchronized (this.mLock) {
            for (int i = 0; i < this.mTargetPackages.size(); i++) {
                int userId = this.mTargetPackages.keyAt(i);
                pw.print(prefix);
                pw.print("  User: ");
                pw.print(userId);
                pw.println();
                pw.print(prefix);
                pw.print("    Package: ");
                pw.print(this.mTargetPackages.get(userId));
                pw.println();
                pw.print(prefix);
                pw.print("    Service: ");
                pw.print(this.mTargetServices.get(userId));
                pw.println();
                pw.print(prefix);
                pw.print("    Message: ");
                pw.print(this.mLastMessages.get(userId));
                pw.println();
            }
        }
    }

    public void dumpSimple(PrintWriter pw) {
        synchronized (this.mLock) {
            for (int i = 0; i < this.mTargetPackages.size(); i++) {
                int userId = this.mTargetPackages.keyAt(i);
                pw.print("finder,");
                pw.print(getAppDescription());
                pw.print(",");
                pw.print(userId);
                pw.print(",");
                pw.print(this.mTargetPackages.get(userId));
                pw.print(",");
                pw.print(this.mTargetServices.get(userId));
                pw.print(",");
                pw.print(this.mLastMessages.get(userId));
                pw.println();
            }
        }
    }
}
