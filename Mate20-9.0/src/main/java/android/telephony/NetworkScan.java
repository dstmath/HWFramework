package android.telephony;

import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.telephony.ITelephony;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NetworkScan {
    public static final int ERROR_INTERRUPTED = 10002;
    public static final int ERROR_INVALID_SCAN = 2;
    public static final int ERROR_INVALID_SCANID = 10001;
    public static final int ERROR_MODEM_ERROR = 1;
    public static final int ERROR_MODEM_UNAVAILABLE = 3;
    public static final int ERROR_RADIO_INTERFACE_ERROR = 10000;
    public static final int ERROR_UNSUPPORTED = 4;
    public static final int SUCCESS = 0;
    private static final String TAG = "NetworkScan";
    private final int mScanId;
    private final int mSubId;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ScanErrorCode {
    }

    public void stopScan() {
        ITelephony telephony = getITelephony();
        if (telephony != null) {
            try {
                telephony.stopNetworkScan(this.mSubId, this.mScanId);
            } catch (RemoteException e) {
                Rlog.e(TAG, "stopNetworkScan  RemoteException");
            } catch (RuntimeException e2) {
                Rlog.e(TAG, "stopNetworkScan  RuntimeException");
            }
        } else {
            Rlog.e(TAG, "Failed to get the ITelephony instance.");
        }
    }

    @Deprecated
    public void stop() throws RemoteException {
        try {
            stopScan();
        } catch (RuntimeException e) {
            throw new RemoteException("Failed to stop the network scan with id " + this.mScanId);
        }
    }

    public NetworkScan(int scanId, int subId) {
        this.mScanId = scanId;
        this.mSubId = subId;
    }

    private ITelephony getITelephony() {
        return ITelephony.Stub.asInterface(ServiceManager.getService("phone"));
    }
}
