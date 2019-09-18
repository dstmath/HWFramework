package com.android.internal.telephony.ims;

import android.net.Uri;
import android.os.RemoteException;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import android.util.ArrayMap;
import com.android.ims.internal.IImsRegistrationListener;
import java.util.Map;

public class ImsRegistrationCompatAdapter extends ImsRegistrationImplBase {
    /* access modifiers changed from: private */
    public static final Map<Integer, Integer> RADIO_TECH_MAPPER = new ArrayMap(2);
    private final IImsRegistrationListener mListener = new IImsRegistrationListener.Stub() {
        public void registrationConnected() throws RemoteException {
            ImsRegistrationCompatAdapter.this.onRegistered(-1);
        }

        public void registrationProgressing() throws RemoteException {
            ImsRegistrationCompatAdapter.this.onRegistering(-1);
        }

        public void registrationConnectedWithRadioTech(int imsRadioTech) throws RemoteException {
            ImsRegistrationCompatAdapter.this.onRegistered(((Integer) ImsRegistrationCompatAdapter.RADIO_TECH_MAPPER.getOrDefault(Integer.valueOf(imsRadioTech), -1)).intValue());
        }

        public void registrationProgressingWithRadioTech(int imsRadioTech) throws RemoteException {
            ImsRegistrationCompatAdapter.this.onRegistering(((Integer) ImsRegistrationCompatAdapter.RADIO_TECH_MAPPER.getOrDefault(Integer.valueOf(imsRadioTech), -1)).intValue());
        }

        public void registrationDisconnected(ImsReasonInfo imsReasonInfo) throws RemoteException {
            ImsRegistrationCompatAdapter.this.onDeregistered(imsReasonInfo);
        }

        public void registrationResumed() throws RemoteException {
        }

        public void registrationSuspended() throws RemoteException {
        }

        public void registrationServiceCapabilityChanged(int serviceClass, int event) throws RemoteException {
        }

        public void registrationFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) throws RemoteException {
        }

        public void voiceMessageCountUpdate(int count) throws RemoteException {
        }

        public void registrationAssociatedUriChanged(Uri[] uris) throws RemoteException {
            ImsRegistrationCompatAdapter.this.onSubscriberAssociatedUriChanged(uris);
        }

        public void registrationChangeFailed(int targetAccessTech, ImsReasonInfo imsReasonInfo) throws RemoteException {
            ImsRegistrationCompatAdapter.this.onTechnologyChangeFailed(((Integer) ImsRegistrationCompatAdapter.RADIO_TECH_MAPPER.getOrDefault(Integer.valueOf(targetAccessTech), -1)).intValue(), imsReasonInfo);
        }
    };

    static {
        RADIO_TECH_MAPPER.put(14, 0);
        RADIO_TECH_MAPPER.put(18, 1);
    }

    public IImsRegistrationListener getRegistrationListener() {
        return this.mListener;
    }
}
