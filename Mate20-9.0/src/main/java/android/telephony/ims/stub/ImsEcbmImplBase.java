package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsEcbmListener;

@SystemApi
public class ImsEcbmImplBase {
    private static final String TAG = "ImsEcbmImplBase";
    private IImsEcbm mImsEcbm = new IImsEcbm.Stub() {
        public void setListener(IImsEcbmListener listener) {
            IImsEcbmListener unused = ImsEcbmImplBase.this.mListener = listener;
        }

        public void exitEmergencyCallbackMode() {
            ImsEcbmImplBase.this.exitEmergencyCallbackMode();
        }
    };
    /* access modifiers changed from: private */
    public IImsEcbmListener mListener;

    public IImsEcbm getImsEcbm() {
        return this.mImsEcbm;
    }

    public void exitEmergencyCallbackMode() {
        Log.d(TAG, "exitEmergencyCallbackMode() not implemented");
    }

    public final void enteredEcbm() {
        Log.d(TAG, "Entered ECBM.");
        if (this.mListener != null) {
            try {
                this.mListener.enteredECBM();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final void exitedEcbm() {
        Log.d(TAG, "Exited ECBM.");
        if (this.mListener != null) {
            try {
                this.mListener.exitedECBM();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
