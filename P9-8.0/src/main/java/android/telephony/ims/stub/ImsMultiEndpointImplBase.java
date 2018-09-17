package android.telephony.ims.stub;

import android.os.RemoteException;
import com.android.ims.internal.IImsExternalCallStateListener;
import com.android.ims.internal.IImsMultiEndpoint.Stub;

public class ImsMultiEndpointImplBase extends Stub {
    public void setListener(IImsExternalCallStateListener listener) throws RemoteException {
    }

    public void requestImsExternalCallStateInfo() throws RemoteException {
    }
}
