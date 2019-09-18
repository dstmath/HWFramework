package android.telephony.ims.stub;

import android.annotation.SystemApi;
import android.os.RemoteException;
import android.telephony.ims.ImsExternalCallState;
import android.util.Log;
import com.android.ims.internal.IImsExternalCallStateListener;
import com.android.ims.internal.IImsMultiEndpoint;
import java.util.List;

@SystemApi
public class ImsMultiEndpointImplBase {
    private static final String TAG = "MultiEndpointImplBase";
    private IImsMultiEndpoint mImsMultiEndpoint = new IImsMultiEndpoint.Stub() {
        public void setListener(IImsExternalCallStateListener listener) throws RemoteException {
            IImsExternalCallStateListener unused = ImsMultiEndpointImplBase.this.mListener = listener;
        }

        public void requestImsExternalCallStateInfo() throws RemoteException {
            ImsMultiEndpointImplBase.this.requestImsExternalCallStateInfo();
        }
    };
    /* access modifiers changed from: private */
    public IImsExternalCallStateListener mListener;

    public IImsMultiEndpoint getIImsMultiEndpoint() {
        return this.mImsMultiEndpoint;
    }

    public final void onImsExternalCallStateUpdate(List<ImsExternalCallState> externalCallDialogs) {
        Log.d(TAG, "ims external call state update triggered.");
        if (this.mListener != null) {
            try {
                this.mListener.onImsExternalCallStateUpdate(externalCallDialogs);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void requestImsExternalCallStateInfo() {
        Log.d(TAG, "requestImsExternalCallStateInfo() not implemented");
    }
}
