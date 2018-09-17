package android.telephony.ims.stub;

import android.os.Bundle;
import android.os.RemoteException;
import com.android.ims.ImsCallForwardInfo;
import com.android.ims.ImsReasonInfo;
import com.android.ims.ImsSsInfo;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener.Stub;

public class ImsUtListenerImplBase extends Stub {
    public void utConfigurationUpdated(IImsUt ut, int id) throws RemoteException {
    }

    public void utConfigurationUpdateFailed(IImsUt ut, int id, ImsReasonInfo error) throws RemoteException {
    }

    public void utConfigurationQueried(IImsUt ut, int id, Bundle ssInfo) throws RemoteException {
    }

    public void utConfigurationQueryFailed(IImsUt ut, int id, ImsReasonInfo error) throws RemoteException {
    }

    public void utConfigurationCallBarringQueried(IImsUt ut, int id, ImsSsInfo[] cbInfo) throws RemoteException {
    }

    public void utConfigurationCallForwardQueried(IImsUt ut, int id, ImsCallForwardInfo[] cfInfo) throws RemoteException {
    }

    public void utConfigurationCallWaitingQueried(IImsUt ut, int id, ImsSsInfo[] cwInfo) throws RemoteException {
    }
}
