package com.android.ims;

import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsEcbmListener.Stub;

public class ImsEcbm {
    private static final boolean DBG = true;
    private static final String TAG = "ImsEcbm";
    private final IImsEcbm miEcbm;

    private class ImsEcbmListenerProxy extends Stub {
        private ImsEcbmStateListener mListener;

        public ImsEcbmListenerProxy(ImsEcbmStateListener listener) {
            this.mListener = listener;
        }

        public void enteredECBM() {
            Rlog.d(ImsEcbm.TAG, "enteredECBM ::");
            if (this.mListener != null) {
                this.mListener.onECBMEntered();
            }
        }

        public void exitedECBM() {
            Rlog.d(ImsEcbm.TAG, "exitedECBM ::");
            if (this.mListener != null) {
                this.mListener.onECBMExited();
            }
        }
    }

    public ImsEcbm(IImsEcbm iEcbm) {
        Rlog.d(TAG, "ImsEcbm created");
        this.miEcbm = iEcbm;
    }

    public void setEcbmStateListener(ImsEcbmStateListener ecbmListener) throws ImsException {
        try {
            this.miEcbm.setListener(new ImsEcbmListenerProxy(ecbmListener));
        } catch (RemoteException e) {
            throw new ImsException("setEcbmStateListener()", e, 106);
        }
    }

    public void exitEmergencyCallbackMode() throws ImsException {
        try {
            this.miEcbm.exitEmergencyCallbackMode();
        } catch (RemoteException e) {
            throw new ImsException("exitEmergencyCallbackMode()", e, 106);
        }
    }
}
