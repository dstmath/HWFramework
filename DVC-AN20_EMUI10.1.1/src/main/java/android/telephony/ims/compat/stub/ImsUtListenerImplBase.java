package android.telephony.ims.compat.stub;

import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.ims.ImsCallForwardInfo;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.ImsSsData;
import android.telephony.ims.ImsSsInfo;
import com.android.ims.internal.IImsUt;
import com.android.ims.internal.IImsUtListener;

public class ImsUtListenerImplBase extends IImsUtListener.Stub {
    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationUpdated(IImsUt ut, int id) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationUpdateFailed(IImsUt ut, int id, ImsReasonInfo error) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationQueried(IImsUt ut, int id, Bundle ssInfo) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationQueryFailed(IImsUt ut, int id, ImsReasonInfo error) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationCallBarringQueried(IImsUt ut, int id, ImsSsInfo[] cbInfo) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationCallForwardQueried(IImsUt ut, int id, ImsCallForwardInfo[] cfInfo) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void utConfigurationCallWaitingQueried(IImsUt ut, int id, ImsSsInfo[] cwInfo) throws RemoteException {
    }

    @Override // com.android.ims.internal.IImsUtListener
    public void onSupplementaryServiceIndication(ImsSsData ssData) {
    }
}
