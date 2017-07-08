package huawei.android.hardware.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.IFingerprintService;
import android.hardware.fingerprint.IFingerprintService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class FingerprintManagerEx {
    private static final String TAG = "FingerprintManagerEx";
    private boolean isSupportHw;
    private Context mContext;
    private IFingerprintService mService;

    public FingerprintManagerEx(Context context) {
        this.isSupportHw = false;
        this.mContext = context;
        this.mService = Stub.asInterface(ServiceManager.getService("fingerprint"));
    }

    public int getRemainingNum() {
        if (this.mService != null) {
            try {
                return this.mService.getRemainingNum();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in getRemainingNum: ", e);
            }
        }
        return -1;
    }

    public long getRemainingTime() {
        if (this.mService != null) {
            try {
                return this.mService.getRemainingTime();
            } catch (RemoteException e) {
                Log.w(TAG, "Remote exception in getRemainingTime: ", e);
            }
        }
        return 0;
    }
}
