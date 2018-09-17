package android.telephony.ims.stub;

import android.os.RemoteException;
import com.android.ims.internal.IImsEcbm.Stub;
import com.android.ims.internal.IImsEcbmListener;

public class ImsEcbmImplBase extends Stub {
    public void setListener(IImsEcbmListener listener) throws RemoteException {
    }

    public void exitEmergencyCallbackMode() throws RemoteException {
    }
}
