package android.net.sip;

import android.net.sip.ISipSessionListener.Stub;

public class SipSessionAdapter extends Stub {
    public void onCalling(ISipSession session) {
    }

    public void onRinging(ISipSession session, SipProfile caller, String sessionDescription) {
    }

    public void onRingingBack(ISipSession session) {
    }

    public void onCallEstablished(ISipSession session, String sessionDescription) {
    }

    public void onCallEnded(ISipSession session) {
    }

    public void onCallBusy(ISipSession session) {
    }

    public void onCallTransferring(ISipSession session, String sessionDescription) {
    }

    public void onCallChangeFailed(ISipSession session, int errorCode, String message) {
    }

    public void onError(ISipSession session, int errorCode, String message) {
    }

    public void onRegistering(ISipSession session) {
    }

    public void onRegistrationDone(ISipSession session, int duration) {
    }

    public void onRegistrationFailed(ISipSession session, int errorCode, String message) {
    }

    public void onRegistrationTimeout(ISipSession session) {
    }
}
