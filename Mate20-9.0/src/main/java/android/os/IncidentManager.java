package android.os;

import android.annotation.SystemApi;
import android.content.Context;
import android.os.IBinder;
import android.os.IIncidentManager;
import android.util.Slog;

@SystemApi
public class IncidentManager {
    private static final String TAG = "IncidentManager";
    private final Context mContext;
    /* access modifiers changed from: private */
    public IIncidentManager mService;

    private class IncidentdDeathRecipient implements IBinder.DeathRecipient {
        private IncidentdDeathRecipient() {
        }

        public void binderDied() {
            synchronized (this) {
                IIncidentManager unused = IncidentManager.this.mService = null;
            }
        }
    }

    public IncidentManager(Context context) {
        this.mContext = context;
    }

    public void reportIncident(IncidentReportArgs args) {
        reportIncidentInternal(args);
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
        if (this.mService != null) {
            return this.mService;
        }
        synchronized (this) {
            if (this.mService != null) {
                IIncidentManager iIncidentManager = this.mService;
                return iIncidentManager;
            }
            this.mService = IIncidentManager.Stub.asInterface(ServiceManager.getService("incident"));
            if (this.mService != null) {
                this.mService.asBinder().linkToDeath(new IncidentdDeathRecipient(), 0);
            }
            IIncidentManager iIncidentManager2 = this.mService;
            return iIncidentManager2;
        }
    }
}
