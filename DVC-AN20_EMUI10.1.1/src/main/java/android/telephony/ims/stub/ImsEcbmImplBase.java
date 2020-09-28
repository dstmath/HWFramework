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
        /* class android.telephony.ims.stub.ImsEcbmImplBase.AnonymousClass1 */

        @Override // com.android.ims.internal.IImsEcbm
        public void setListener(IImsEcbmListener listener) {
            ImsEcbmImplBase.this.mListener = listener;
        }

        @Override // com.android.ims.internal.IImsEcbm
        public void exitEmergencyCallbackMode() {
            ImsEcbmImplBase.this.exitEmergencyCallbackMode();
        }
    };
    private IImsEcbmListener mListener;

    public IImsEcbm getImsEcbm() {
        return this.mImsEcbm;
    }

    public void exitEmergencyCallbackMode() {
        Log.d(TAG, "exitEmergencyCallbackMode() not implemented");
    }

    public final void enteredEcbm() {
        Log.d(TAG, "Entered ECBM.");
        IImsEcbmListener iImsEcbmListener = this.mListener;
        if (iImsEcbmListener != null) {
            try {
                iImsEcbmListener.enteredECBM();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public final void exitedEcbm() {
        Log.d(TAG, "Exited ECBM.");
        IImsEcbmListener iImsEcbmListener = this.mListener;
        if (iImsEcbmListener != null) {
            try {
                iImsEcbmListener.exitedECBM();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
