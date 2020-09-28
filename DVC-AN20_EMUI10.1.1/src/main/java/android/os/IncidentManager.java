package android.os;

import android.annotation.SystemApi;
import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.IIncidentAuthListener;
import android.os.IIncidentCompanion;
import android.os.IIncidentManager;
import android.os.IncidentManager;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Slog;
import android.util.TimeUtils;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

@SystemApi
public class IncidentManager {
    public static final int FLAG_CONFIRMATION_DIALOG = 1;
    public static final int PRIVACY_POLICY_AUTO = 200;
    public static final int PRIVACY_POLICY_EXPLICIT = 100;
    public static final int PRIVACY_POLICY_LOCAL = 0;
    private static final String TAG = "IncidentManager";
    public static final String URI_AUTHORITY = "android.os.IncidentManager";
    public static final String URI_PARAM_CALLING_PACKAGE = "pkg";
    public static final String URI_PARAM_FLAGS = "flags";
    public static final String URI_PARAM_ID = "id";
    public static final String URI_PARAM_RECEIVER_CLASS = "receiver";
    public static final String URI_PARAM_REPORT_ID = "r";
    public static final String URI_PARAM_TIMESTAMP = "t";
    public static final String URI_PATH = "/pending";
    public static final String URI_SCHEME = "content";
    private IIncidentCompanion mCompanionService;
    private final Context mContext;
    private IIncidentManager mIncidentService;
    private Object mLock = new Object();

    @Retention(RetentionPolicy.SOURCE)
    public @interface PrivacyPolicy {
    }

    @SystemApi
    public static class PendingReport {
        private final int mFlags;
        private final String mRequestingPackage;
        private final long mTimestamp;
        private final Uri mUri;

        public PendingReport(Uri uri) {
            try {
                this.mFlags = Integer.parseInt(uri.getQueryParameter("flags"));
                String requestingPackage = uri.getQueryParameter("pkg");
                if (requestingPackage != null) {
                    this.mRequestingPackage = requestingPackage;
                    try {
                        this.mTimestamp = Long.parseLong(uri.getQueryParameter(IncidentManager.URI_PARAM_TIMESTAMP));
                        this.mUri = uri;
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("Invalid URI: No t parameter. " + uri);
                    }
                } else {
                    throw new RuntimeException("Invalid URI: No pkg parameter. " + uri);
                }
            } catch (NumberFormatException e2) {
                throw new RuntimeException("Invalid URI: No flags parameter. " + uri);
            }
        }

        public String getRequestingPackage() {
            return this.mRequestingPackage;
        }

        public int getFlags() {
            return this.mFlags;
        }

        public long getTimestamp() {
            return this.mTimestamp;
        }

        public Uri getUri() {
            return this.mUri;
        }

        public String toString() {
            return "PendingReport(" + getUri().toString() + ")";
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof PendingReport)) {
                return false;
            }
            PendingReport that = (PendingReport) obj;
            if (!this.mUri.equals(that.mUri) || this.mFlags != that.mFlags || !this.mRequestingPackage.equals(that.mRequestingPackage) || this.mTimestamp != that.mTimestamp) {
                return false;
            }
            return true;
        }
    }

    @SystemApi
    public static class IncidentReport implements Parcelable, Closeable {
        public static final Parcelable.Creator<IncidentReport> CREATOR = new Parcelable.Creator() {
            /* class android.os.IncidentManager.IncidentReport.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public IncidentReport[] newArray(int size) {
                return new IncidentReport[size];
            }

            @Override // android.os.Parcelable.Creator
            public IncidentReport createFromParcel(Parcel in) {
                return new IncidentReport(in);
            }
        };
        private ParcelFileDescriptor mFileDescriptor;
        private final int mPrivacyPolicy;
        private final long mTimestampNs;

        public IncidentReport(Parcel in) {
            this.mTimestampNs = in.readLong();
            this.mPrivacyPolicy = in.readInt();
            if (in.readInt() != 0) {
                this.mFileDescriptor = ParcelFileDescriptor.CREATOR.createFromParcel(in);
            } else {
                this.mFileDescriptor = null;
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            try {
                if (this.mFileDescriptor != null) {
                    this.mFileDescriptor.close();
                    this.mFileDescriptor = null;
                }
            } catch (IOException e) {
            }
        }

        public long getTimestamp() {
            return this.mTimestampNs / TimeUtils.NANOS_PER_MS;
        }

        public long getPrivacyPolicy() {
            return (long) this.mPrivacyPolicy;
        }

        public InputStream getInputStream() throws IOException {
            ParcelFileDescriptor parcelFileDescriptor = this.mFileDescriptor;
            if (parcelFileDescriptor == null) {
                return null;
            }
            return new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor);
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return this.mFileDescriptor != null ? 1 : 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel out, int flags) {
            out.writeLong(this.mTimestampNs);
            out.writeInt(this.mPrivacyPolicy);
            if (this.mFileDescriptor != null) {
                out.writeInt(1);
                this.mFileDescriptor.writeToParcel(out, flags);
                return;
            }
            out.writeInt(0);
        }
    }

    public static class AuthListener {
        IIncidentAuthListener.Stub mBinder = new IIncidentAuthListener.Stub() {
            /* class android.os.IncidentManager.AuthListener.AnonymousClass1 */

            @Override // android.os.IIncidentAuthListener
            public void onReportApproved() {
                if (AuthListener.this.mExecutor != null) {
                    AuthListener.this.mExecutor.execute(new Runnable() {
                        /* class android.os.$$Lambda$IncidentManager$AuthListener$1$lPkHJjJYlkckZZgbwSfNFtF2x_U */

                        public final void run() {
                            IncidentManager.AuthListener.AnonymousClass1.this.lambda$onReportApproved$0$IncidentManager$AuthListener$1();
                        }
                    });
                } else {
                    AuthListener.this.onReportApproved();
                }
            }

            public /* synthetic */ void lambda$onReportApproved$0$IncidentManager$AuthListener$1() {
                AuthListener.this.onReportApproved();
            }

            @Override // android.os.IIncidentAuthListener
            public void onReportDenied() {
                if (AuthListener.this.mExecutor != null) {
                    AuthListener.this.mExecutor.execute(new Runnable() {
                        /* class android.os.$$Lambda$IncidentManager$AuthListener$1$VoPbrfU3RKoeruCLRzIQ8yeLsyQ */

                        public final void run() {
                            IncidentManager.AuthListener.AnonymousClass1.this.lambda$onReportDenied$1$IncidentManager$AuthListener$1();
                        }
                    });
                } else {
                    AuthListener.this.onReportDenied();
                }
            }

            public /* synthetic */ void lambda$onReportDenied$1$IncidentManager$AuthListener$1() {
                AuthListener.this.onReportDenied();
            }
        };
        Executor mExecutor;

        public void onReportApproved() {
        }

        public void onReportDenied() {
        }
    }

    public IncidentManager(Context context) {
        this.mContext = context;
    }

    public void reportIncident(IncidentReportArgs args) {
        reportIncidentInternal(args);
    }

    public void requestAuthorization(int callingUid, String callingPackage, int flags, AuthListener listener) {
        requestAuthorization(callingUid, callingPackage, flags, this.mContext.getMainExecutor(), listener);
    }

    public void requestAuthorization(int callingUid, String callingPackage, int flags, Executor executor, AuthListener listener) {
        try {
            if (listener.mExecutor == null) {
                listener.mExecutor = executor;
                getCompanionServiceLocked().authorizeReport(callingUid, callingPackage, null, null, flags, listener.mBinder);
                return;
            }
            throw new RuntimeException("Do not reuse AuthListener objects when calling requestAuthorization");
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void cancelAuthorization(AuthListener listener) {
        try {
            getCompanionServiceLocked().cancelAuthorization(listener.mBinder);
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<PendingReport> getPendingReports() {
        try {
            List<String> strings = getCompanionServiceLocked().getPendingReports();
            int size = strings.size();
            ArrayList<PendingReport> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(new PendingReport(Uri.parse(strings.get(i))));
            }
            return result;
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void approveReport(Uri uri) {
        try {
            getCompanionServiceLocked().approveReport(uri.toString());
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void denyReport(Uri uri) {
        try {
            getCompanionServiceLocked().denyReport(uri.toString());
        } catch (RemoteException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Uri> getIncidentReportList(String receiverClass) {
        try {
            List<String> strings = getCompanionServiceLocked().getIncidentReportList(this.mContext.getPackageName(), receiverClass);
            int size = strings.size();
            ArrayList<Uri> result = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                result.add(Uri.parse(strings.get(i)));
            }
            return result;
        } catch (RemoteException ex) {
            throw new RuntimeException("System server or incidentd going down", ex);
        }
    }

    public IncidentReport getIncidentReport(Uri uri) {
        String id = uri.getQueryParameter("r");
        if (id == null) {
            return null;
        }
        String pkg = uri.getQueryParameter("pkg");
        if (pkg != null) {
            String cls = uri.getQueryParameter("receiver");
            if (cls != null) {
                try {
                    return getCompanionServiceLocked().getIncidentReport(pkg, cls, id);
                } catch (RemoteException ex) {
                    throw new RuntimeException("System server or incidentd going down", ex);
                }
            } else {
                throw new RuntimeException("Invalid URI: No receiver parameter. " + uri);
            }
        } else {
            throw new RuntimeException("Invalid URI: No pkg parameter. " + uri);
        }
    }

    public void deleteIncidentReports(Uri uri) {
        if (uri == null) {
            try {
                getCompanionServiceLocked().deleteAllIncidentReports(this.mContext.getPackageName());
            } catch (RemoteException ex) {
                throw new RuntimeException("System server or incidentd going down", ex);
            }
        } else {
            String pkg = uri.getQueryParameter("pkg");
            if (pkg != null) {
                String cls = uri.getQueryParameter("receiver");
                if (cls != null) {
                    String id = uri.getQueryParameter("r");
                    if (id != null) {
                        try {
                            getCompanionServiceLocked().deleteIncidentReports(pkg, cls, id);
                        } catch (RemoteException ex2) {
                            throw new RuntimeException("System server or incidentd going down", ex2);
                        }
                    } else {
                        throw new RuntimeException("Invalid URI: No r parameter. " + uri);
                    }
                } else {
                    throw new RuntimeException("Invalid URI: No receiver parameter. " + uri);
                }
            } else {
                throw new RuntimeException("Invalid URI: No pkg parameter. " + uri);
            }
        }
    }

    private void reportIncidentInternal(IncidentReportArgs args) {
        try {
            IIncidentManager service = getIIncidentManagerLocked();
            if (service == null) {
                Slog.e(TAG, "reportIncident can't find incident binder service");
            } else {
                service.reportIncident(args);
            }
        } catch (RemoteException ex) {
            Slog.e(TAG, "reportIncident failed", ex);
        }
    }

    private IIncidentManager getIIncidentManagerLocked() throws RemoteException {
        IIncidentManager iIncidentManager = this.mIncidentService;
        if (iIncidentManager != null) {
            return iIncidentManager;
        }
        synchronized (this.mLock) {
            if (this.mIncidentService != null) {
                return this.mIncidentService;
            }
            this.mIncidentService = IIncidentManager.Stub.asInterface(ServiceManager.getService(Context.INCIDENT_SERVICE));
            if (this.mIncidentService != null) {
                this.mIncidentService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    /* class android.os.$$Lambda$IncidentManager$yGukxCMuLDmoRlrh5jGUmq5BOTk */

                    @Override // android.os.IBinder.DeathRecipient
                    public final void binderDied() {
                        IncidentManager.this.lambda$getIIncidentManagerLocked$0$IncidentManager();
                    }
                }, 0);
            }
            return this.mIncidentService;
        }
    }

    public /* synthetic */ void lambda$getIIncidentManagerLocked$0$IncidentManager() {
        synchronized (this.mLock) {
            this.mIncidentService = null;
        }
    }

    private IIncidentCompanion getCompanionServiceLocked() throws RemoteException {
        IIncidentCompanion iIncidentCompanion = this.mCompanionService;
        if (iIncidentCompanion != null) {
            return iIncidentCompanion;
        }
        synchronized (this) {
            if (this.mCompanionService != null) {
                return this.mCompanionService;
            }
            this.mCompanionService = IIncidentCompanion.Stub.asInterface(ServiceManager.getService(Context.INCIDENT_COMPANION_SERVICE));
            if (this.mCompanionService != null) {
                this.mCompanionService.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                    /* class android.os.$$Lambda$IncidentManager$mfBTEJgu7VPkoPMTQdf1KC7oi5g */

                    @Override // android.os.IBinder.DeathRecipient
                    public final void binderDied() {
                        IncidentManager.this.lambda$getCompanionServiceLocked$1$IncidentManager();
                    }
                }, 0);
            }
            return this.mCompanionService;
        }
    }

    public /* synthetic */ void lambda$getCompanionServiceLocked$1$IncidentManager() {
        synchronized (this.mLock) {
            this.mCompanionService = null;
        }
    }
}
