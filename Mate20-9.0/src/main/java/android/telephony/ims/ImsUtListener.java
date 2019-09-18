package android.telephony.ims;

import android.annotation.SystemApi;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.android.ims.internal.IImsUtListener;

@SystemApi
public class ImsUtListener {
    private static final String LOG_TAG = "ImsUtListener";
    private IImsUtListener mServiceInterface;

    public void onUtConfigurationUpdated(int id) {
        try {
            this.mServiceInterface.utConfigurationUpdated(null, id);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationUpdated: remote exception");
        }
    }

    public void onUtConfigurationUpdateFailed(int id, ImsReasonInfo error) {
        try {
            this.mServiceInterface.utConfigurationUpdateFailed(null, id, error);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationUpdateFailed: remote exception");
        }
    }

    public void onUtConfigurationQueried(int id, Bundle ssInfo) {
        try {
            this.mServiceInterface.utConfigurationQueried(null, id, ssInfo);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationQueried: remote exception");
        }
    }

    public void onUtConfigurationQueryFailed(int id, ImsReasonInfo error) {
        try {
            this.mServiceInterface.utConfigurationQueryFailed(null, id, error);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationQueryFailed: remote exception");
        }
    }

    public void onUtConfigurationCallBarringQueried(int id, ImsSsInfo[] cbInfo) {
        try {
            this.mServiceInterface.utConfigurationCallBarringQueried(null, id, cbInfo);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallBarringQueried: remote exception");
        }
    }

    public void onUtConfigurationCallForwardQueried(int id, ImsCallForwardInfo[] cfInfo) {
        try {
            this.mServiceInterface.utConfigurationCallForwardQueried(null, id, cfInfo);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallForwardQueried: remote exception");
        }
    }

    public void onUtConfigurationCallWaitingQueried(int id, ImsSsInfo[] cwInfo) {
        try {
            this.mServiceInterface.utConfigurationCallWaitingQueried(null, id, cwInfo);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallWaitingQueried: remote exception");
        }
    }

    public void onSupplementaryServiceIndication(ImsSsData ssData) {
        try {
            this.mServiceInterface.onSupplementaryServiceIndication(ssData);
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "onSupplementaryServiceIndication: remote exception");
        }
    }

    public ImsUtListener(IImsUtListener serviceInterface) {
        this.mServiceInterface = serviceInterface;
    }
}
