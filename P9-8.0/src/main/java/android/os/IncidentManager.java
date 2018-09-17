package android.os;

import android.content.Context;
import android.os.IIncidentManager.Stub;
import android.provider.Settings.System;
import android.util.Slog;

public class IncidentManager {
    private static final String TAG = "incident";
    private Context mContext;

    public IncidentManager(Context context) {
        this.mContext = context;
    }

    public void reportIncident(IncidentReportArgs args) {
        IIncidentManager service = Stub.asInterface(ServiceManager.getService("incident"));
        if (service == null) {
            Slog.e("incident", "reportIncident can't find incident binder service");
            return;
        }
        try {
            service.reportIncident(args);
        } catch (RemoteException ex) {
            Slog.e("incident", "reportIncident failed", ex);
        }
    }

    public void reportIncident(String settingName, byte[] headerProto) {
        try {
            IncidentReportArgs args = IncidentReportArgs.parseSetting(System.getString(this.mContext.getContentResolver(), settingName));
            if (args == null) {
                Slog.i("incident", "Incident report requested but disabled: " + settingName);
                return;
            }
            args.addHeader(headerProto);
            IIncidentManager service = Stub.asInterface(ServiceManager.getService("incident"));
            if (service == null) {
                Slog.e("incident", "reportIncident can't find incident binder service");
                return;
            }
            Slog.i("incident", "Taking incident report: " + settingName);
            try {
                service.reportIncident(args);
            } catch (RemoteException ex) {
                Slog.e("incident", "reportIncident failed", ex);
            }
        } catch (IllegalArgumentException ex2) {
            Slog.w("incident", "Bad value for incident report setting '" + settingName + "'", ex2);
        }
    }
}
