package android.telephony.ims.feature;

import android.app.PendingIntent;
import android.os.Message;
import com.android.ims.ImsCallProfile;
import com.android.ims.internal.IImsCallSession;
import com.android.ims.internal.IImsCallSessionListener;
import com.android.ims.internal.IImsConfig;
import com.android.ims.internal.IImsEcbm;
import com.android.ims.internal.IImsMultiEndpoint;
import com.android.ims.internal.IImsRegistrationListener;
import com.android.ims.internal.IImsUt;

public class MMTelFeature extends ImsFeature implements IMMTelFeature {
    public int startSession(PendingIntent incomingCallIntent, IImsRegistrationListener listener) {
        return 0;
    }

    public void endSession(int sessionId) {
    }

    public boolean isConnected(int callSessionType, int callType) {
        return false;
    }

    public boolean isOpened() {
        return false;
    }

    public void addRegistrationListener(IImsRegistrationListener listener) {
    }

    public void removeRegistrationListener(IImsRegistrationListener listener) {
    }

    public ImsCallProfile createCallProfile(int sessionId, int callSessionType, int callType) {
        return null;
    }

    public IImsCallSession createCallSession(int sessionId, ImsCallProfile profile, IImsCallSessionListener listener) {
        return null;
    }

    public IImsCallSession getPendingCallSession(int sessionId, String callId) {
        return null;
    }

    public IImsUt getUtInterface() {
        return null;
    }

    public IImsConfig getConfigInterface() {
        return null;
    }

    public void turnOnIms() {
    }

    public void turnOffIms() {
    }

    public IImsEcbm getEcbmInterface() {
        return null;
    }

    public void setUiTTYMode(int uiTtyMode, Message onComplete) {
    }

    public IImsMultiEndpoint getMultiEndpointInterface() {
        return null;
    }

    public void onFeatureRemoved() {
    }

    public int getLastCallType() {
        return -1;
    }
}
