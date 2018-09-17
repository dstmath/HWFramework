package com.android.ims;

import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.ims.internal.IImsExternalCallStateListener.Stub;
import com.android.ims.internal.IImsMultiEndpoint;
import java.util.List;

public class ImsMultiEndpoint {
    private static final boolean DBG = true;
    private static final String TAG = "ImsMultiEndpoint";
    private final IImsMultiEndpoint mImsMultiendpoint;

    private class ImsExternalCallStateListenerProxy extends Stub {
        private ImsExternalCallStateListener mListener;

        public ImsExternalCallStateListenerProxy(ImsExternalCallStateListener listener) {
            this.mListener = listener;
        }

        public void onImsExternalCallStateUpdate(List<ImsExternalCallState> externalCallState) {
            Rlog.d(ImsMultiEndpoint.TAG, "onImsExternalCallStateUpdate");
            if (this.mListener != null) {
                this.mListener.onImsExternalCallStateUpdate(externalCallState);
            }
        }
    }

    public ImsMultiEndpoint(IImsMultiEndpoint iImsMultiEndpoint) {
        Rlog.d(TAG, "ImsMultiEndpoint created");
        this.mImsMultiendpoint = iImsMultiEndpoint;
    }

    public void setExternalCallStateListener(ImsExternalCallStateListener externalCallStateListener) throws ImsException {
        try {
            Rlog.d(TAG, "setExternalCallStateListener");
            this.mImsMultiendpoint.setListener(new ImsExternalCallStateListenerProxy(externalCallStateListener));
        } catch (RemoteException e) {
            throw new ImsException("setExternalCallStateListener could not be set.", e, 106);
        }
    }
}
