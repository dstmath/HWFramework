package android.service.watchdog;

import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.service.watchdog.ExplicitHealthCheckService;
import android.service.watchdog.IExplicitHealthCheckService;
import android.util.Log;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@SystemApi
public abstract class ExplicitHealthCheckService extends Service {
    public static final String BIND_PERMISSION = "android.permission.BIND_EXPLICIT_HEALTH_CHECK_SERVICE";
    public static final String EXTRA_HEALTH_CHECK_PASSED_PACKAGE = "android.service.watchdog.extra.health_check_passed_package";
    public static final String EXTRA_REQUESTED_PACKAGES = "android.service.watchdog.extra.requested_packages";
    public static final String EXTRA_SUPPORTED_PACKAGES = "android.service.watchdog.extra.supported_packages";
    public static final String SERVICE_INTERFACE = "android.service.watchdog.ExplicitHealthCheckService";
    private static final String TAG = "ExplicitHealthCheckService";
    private RemoteCallback mCallback;
    private final Handler mHandler = new Handler(Looper.getMainLooper(), null, true);
    private final ExplicitHealthCheckServiceWrapper mWrapper = new ExplicitHealthCheckServiceWrapper();

    public abstract void onCancelHealthCheck(String str);

    public abstract List<String> onGetRequestedPackages();

    public abstract List<PackageConfig> onGetSupportedPackages();

    public abstract void onRequestHealthCheck(String str);

    @Override // android.app.Service
    public final IBinder onBind(Intent intent) {
        return this.mWrapper;
    }

    public final void notifyHealthCheckPassed(String packageName) {
        this.mHandler.post(new Runnable(packageName) {
            /* class android.service.watchdog.$$Lambda$ExplicitHealthCheckService$ulagkAZ2bM7LW9T7PSTxSLQfBE */
            private final /* synthetic */ String f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                ExplicitHealthCheckService.this.lambda$notifyHealthCheckPassed$0$ExplicitHealthCheckService(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$notifyHealthCheckPassed$0$ExplicitHealthCheckService(String packageName) {
        if (this.mCallback != null) {
            Objects.requireNonNull(packageName, "Package passing explicit health check must be non-null");
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_HEALTH_CHECK_PASSED_PACKAGE, packageName);
            this.mCallback.sendResult(bundle);
            return;
        }
        Log.wtf(TAG, "System missed explicit health check result for " + packageName);
    }

    @SystemApi
    public static final class PackageConfig implements Parcelable {
        public static final Parcelable.Creator<PackageConfig> CREATOR = new Parcelable.Creator<PackageConfig>() {
            /* class android.service.watchdog.ExplicitHealthCheckService.PackageConfig.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public PackageConfig createFromParcel(Parcel source) {
                return new PackageConfig(source);
            }

            @Override // android.os.Parcelable.Creator
            public PackageConfig[] newArray(int size) {
                return new PackageConfig[size];
            }
        };
        private static final long DEFAULT_HEALTH_CHECK_TIMEOUT_MILLIS = TimeUnit.HOURS.toMillis(1);
        private final long mHealthCheckTimeoutMillis;
        private final String mPackageName;

        public PackageConfig(String packageName, long healthCheckTimeoutMillis) {
            this.mPackageName = (String) Preconditions.checkNotNull(packageName);
            if (healthCheckTimeoutMillis == 0) {
                this.mHealthCheckTimeoutMillis = DEFAULT_HEALTH_CHECK_TIMEOUT_MILLIS;
            } else {
                this.mHealthCheckTimeoutMillis = Preconditions.checkArgumentNonnegative(healthCheckTimeoutMillis);
            }
        }

        private PackageConfig(Parcel parcel) {
            this.mPackageName = parcel.readString();
            this.mHealthCheckTimeoutMillis = parcel.readLong();
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public long getHealthCheckTimeoutMillis() {
            return this.mHealthCheckTimeoutMillis;
        }

        public String toString() {
            return "PackageConfig{" + this.mPackageName + ", " + this.mHealthCheckTimeoutMillis + "}";
        }

        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof PackageConfig)) {
                return false;
            }
            PackageConfig otherInfo = (PackageConfig) other;
            if (!Objects.equals(Long.valueOf(otherInfo.getHealthCheckTimeoutMillis()), Long.valueOf(this.mHealthCheckTimeoutMillis)) || !Objects.equals(otherInfo.getPackageName(), this.mPackageName)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return Objects.hash(this.mPackageName, Long.valueOf(this.mHealthCheckTimeoutMillis));
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(this.mPackageName);
            parcel.writeLong(this.mHealthCheckTimeoutMillis);
        }
    }

    /* access modifiers changed from: private */
    public class ExplicitHealthCheckServiceWrapper extends IExplicitHealthCheckService.Stub {
        private ExplicitHealthCheckServiceWrapper() {
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void setCallback(RemoteCallback callback) throws RemoteException {
            ExplicitHealthCheckService.this.mHandler.post(new Runnable(callback) {
                /* class android.service.watchdog.$$Lambda$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper$EnMyVE8D3ameNypg4gr2IMP7BCo */
                private final /* synthetic */ RemoteCallback f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ExplicitHealthCheckService.ExplicitHealthCheckServiceWrapper.this.lambda$setCallback$0$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$setCallback$0$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(RemoteCallback callback) {
            ExplicitHealthCheckService.this.mCallback = callback;
        }

        public /* synthetic */ void lambda$request$1$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(String packageName) {
            ExplicitHealthCheckService.this.onRequestHealthCheck(packageName);
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void request(String packageName) throws RemoteException {
            ExplicitHealthCheckService.this.mHandler.post(new Runnable(packageName) {
                /* class android.service.watchdog.$$Lambda$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper$Gn8La3kwBvbjLET_Nqtstvz2RZg */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ExplicitHealthCheckService.ExplicitHealthCheckServiceWrapper.this.lambda$request$1$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(this.f$1);
                }
            });
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void cancel(String packageName) throws RemoteException {
            ExplicitHealthCheckService.this.mHandler.post(new Runnable(packageName) {
                /* class android.service.watchdog.$$Lambda$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper$pC_jIpmynGf4FhRLSRCGbJwUkGE */
                private final /* synthetic */ String f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ExplicitHealthCheckService.ExplicitHealthCheckServiceWrapper.this.lambda$cancel$2$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$cancel$2$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(String packageName) {
            ExplicitHealthCheckService.this.onCancelHealthCheck(packageName);
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void getSupportedPackages(RemoteCallback callback) throws RemoteException {
            ExplicitHealthCheckService.this.mHandler.post(new Runnable(callback) {
                /* class android.service.watchdog.$$Lambda$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper$5Rv9E4Jc0y0GMGqI_g82qtYpg */
                private final /* synthetic */ RemoteCallback f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ExplicitHealthCheckService.ExplicitHealthCheckServiceWrapper.this.lambda$getSupportedPackages$3$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$getSupportedPackages$3$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(RemoteCallback callback) {
            List<PackageConfig> packages = ExplicitHealthCheckService.this.onGetSupportedPackages();
            Objects.requireNonNull(packages, "Supported package list must be non-null");
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(ExplicitHealthCheckService.EXTRA_SUPPORTED_PACKAGES, new ArrayList<>(packages));
            callback.sendResult(bundle);
        }

        @Override // android.service.watchdog.IExplicitHealthCheckService
        public void getRequestedPackages(RemoteCallback callback) throws RemoteException {
            ExplicitHealthCheckService.this.mHandler.post(new Runnable(callback) {
                /* class android.service.watchdog.$$Lambda$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper$yycuCTr7mDJWrqKxpXb1sTmkyA */
                private final /* synthetic */ RemoteCallback f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ExplicitHealthCheckService.ExplicitHealthCheckServiceWrapper.this.lambda$getRequestedPackages$4$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$getRequestedPackages$4$ExplicitHealthCheckService$ExplicitHealthCheckServiceWrapper(RemoteCallback callback) {
            List<String> packages = ExplicitHealthCheckService.this.onGetRequestedPackages();
            Objects.requireNonNull(packages, "Requested  package list must be non-null");
            Bundle bundle = new Bundle();
            bundle.putStringArrayList(ExplicitHealthCheckService.EXTRA_REQUESTED_PACKAGES, new ArrayList<>(packages));
            callback.sendResult(bundle);
        }
    }
}
