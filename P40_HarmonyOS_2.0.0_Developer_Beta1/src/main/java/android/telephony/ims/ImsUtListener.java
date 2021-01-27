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
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationUpdated(null, id);
            } else {
                Log.e(LOG_TAG, "utConfigurationUpdated: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationUpdated: remote exception");
        }
    }

    public void onUtConfigurationUpdateFailed(int id, ImsReasonInfo error) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationUpdateFailed(null, id, error);
            } else {
                Log.e(LOG_TAG, "utConfigurationUpdateFailed: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationUpdateFailed: remote exception");
        }
    }

    public void onUtConfigurationQueried(int id, Bundle ssInfo) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationQueried(null, id, ssInfo);
            } else {
                Log.e(LOG_TAG, "utConfigurationQueried: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationQueried: remote exception");
        }
    }

    public void onUtConfigurationQueryFailed(int id, ImsReasonInfo error) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationQueryFailed(null, id, error);
            } else {
                Log.e(LOG_TAG, "utConfigurationQueryFailed: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationQueryFailed: remote exception");
        }
    }

    public void onUtConfigurationCallBarringQueried(int id, ImsSsInfo[] cbInfo) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationCallBarringQueried(null, id, cbInfo);
            } else {
                Log.e(LOG_TAG, "utConfigurationCallBarringQueried: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallBarringQueried: remote exception");
        }
    }

    public void onUtConfigurationCallForwardQueried(int id, ImsCallForwardInfo[] cfInfo) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationCallForwardQueried(null, id, cfInfo);
            } else {
                Log.e(LOG_TAG, "utConfigurationCallForwardQueried: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallForwardQueried: remote exception");
        }
    }

    public void onUtConfigurationCallWaitingQueried(int id, ImsSsInfo[] cwInfo) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.utConfigurationCallWaitingQueried(null, id, cwInfo);
            } else {
                Log.e(LOG_TAG, "utConfigurationCallWaitingQueried: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "utConfigurationCallWaitingQueried: remote exception");
        }
    }

    public void onSupplementaryServiceIndication(ImsSsData ssData) {
        try {
            if (this.mServiceInterface != null) {
                this.mServiceInterface.onSupplementaryServiceIndication(ssData);
            } else {
                Log.e(LOG_TAG, "onSupplementaryServiceIndication: mServiceInterface null");
            }
        } catch (RemoteException e) {
            Log.w(LOG_TAG, "onSupplementaryServiceIndication: remote exception");
        }
    }

    public ImsUtListener(IImsUtListener serviceInterface) {
        this.mServiceInterface = serviceInterface;
    }
}
