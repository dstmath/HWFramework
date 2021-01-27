package ohos.dcall;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public interface IDistributedCall extends IRemoteBroker {
    void addCallObserver(int i, ICallStateObserver iCallStateObserver, int i2);

    int answerCall(int i, int i2);

    int dial(Uri uri, PacMap pacMap);

    boolean dial(String str, boolean z);

    int disconnect(int i);

    void displayCallScreen(boolean z);

    int distributeCallEvent(int i, String str, PacMap pacMap);

    int getCallState();

    List<String> getPredefinedRejectMessages(int i);

    boolean hasCall();

    boolean hasVoiceCapability();

    int hold(int i);

    int initDialEnv(PacMap pacMap);

    void inputDialerSpecialCode(String str);

    boolean isNewCallAllowed();

    boolean isVideoCallingEnabled();

    void muteRinger();

    int postDialDtmfContinue(int i, boolean z);

    int reject(int i, boolean z, String str);

    void removeCallObserver(int i, ICallStateObserver iCallStateObserver);

    int setAudioDevice(int i);

    int setMuted(boolean z);

    int startDtmfTone(int i, char c);

    int stopDtmfTone(int i);

    int unhold(int i);
}
