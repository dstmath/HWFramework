package com.android.ims;

import android.net.Uri;

public class ImsConnectionStateListener {
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
