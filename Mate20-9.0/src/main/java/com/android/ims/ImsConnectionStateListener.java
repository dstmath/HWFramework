package com.android.ims;

import android.net.Uri;
import android.telephony.ims.ImsReasonInfo;
import android.telephony.ims.feature.ImsFeature;
import android.telephony.ims.stub.ImsRegistrationImplBase;
import java.util.Arrays;

public class ImsConnectionStateListener extends ImsRegistrationImplBase.Callback {
    public final void onRegistered(int imsRadioTech) {
        onImsConnected(imsRadioTech);
    }

    public final void onRegistering(int imsRadioTech) {
        onImsProgressing(imsRadioTech);
    }

    public final void onDeregistered(ImsReasonInfo info) {
        onImsDisconnected(info);
    }

    public final void onTechnologyChangeFailed(int imsRadioTech, ImsReasonInfo info) {
        onRegistrationChangeFailed(imsRadioTech, info);
    }

    public void onSubscriberAssociatedUriChanged(Uri[] uris) {
        registrationAssociatedUriChanged(uris);
    }

    public void onFeatureCapabilityChangedAdapter(int imsRadioTech, ImsFeature.Capabilities c) {
        int[] enabledCapabilities = new int[6];
        Arrays.fill(enabledCapabilities, -1);
        int[] disabledCapabilities = new int[6];
        Arrays.fill(disabledCapabilities, -1);
        switch (imsRadioTech) {
            case 0:
                if (c.isCapable(1)) {
                    enabledCapabilities[0] = 0;
                }
                if (c.isCapable(2)) {
                    enabledCapabilities[1] = 1;
                }
                if (c.isCapable(4)) {
                    enabledCapabilities[4] = 4;
                    break;
                }
                break;
            case 1:
                if (c.isCapable(1)) {
                    enabledCapabilities[2] = 2;
                }
                if (c.isCapable(2)) {
                    enabledCapabilities[3] = 3;
                }
                if (c.isCapable(4)) {
                    enabledCapabilities[5] = 5;
                    break;
                }
                break;
        }
        for (int i = 0; i < enabledCapabilities.length; i++) {
            if (enabledCapabilities[i] != i) {
                disabledCapabilities[i] = i;
            }
        }
        onFeatureCapabilityChanged(1, enabledCapabilities, disabledCapabilities);
    }

    public void onImsConnected(int imsRadioTech) {
    }

    public void onImsProgressing(int imsRadioTech) {
    }

    public void onImsDisconnected(ImsReasonInfo imsReasonInfo) {
    }

    public void onImsResumed() {
    }

    public void onImsSuspended() {
    }

    public void onFeatureCapabilityChanged(int serviceClass, int[] enabledFeatures, int[] disabledFeatures) {
    }

    public void onVoiceMessageCountChanged(int count) {
    }

    public void registrationAssociatedUriChanged(Uri[] uris) {
    }

    public void onRegistrationChangeFailed(int imsRadioTech, ImsReasonInfo imsReasonInfo) {
    }
}
