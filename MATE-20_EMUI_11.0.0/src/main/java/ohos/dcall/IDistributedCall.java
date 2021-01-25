package ohos.dcall;

import ohos.rpc.IRemoteBroker;

public interface IDistributedCall extends IRemoteBroker {
    void addCallObserver(int i, ICallStateObserver iCallStateObserver, int i2);

    int answerCall(int i, int i2);

    boolean dial(String str, boolean z);

    int disconnect(int i);

    void displayCallScreen(boolean z);

    int getCallState();

    boolean hasCall();

    void inputDialerSpecialCode(String str);

    boolean isNewCallAllowed();

    boolean isVideoCallingEnabled();

    boolean isVoiceCap();

    void muteRinger();

    int postDialDtmfContinue(int i, boolean z);

    int reject(int i, boolean z, String str);

    void removeCallObserver(int i, ICallStateObserver iCallStateObserver);

    int setAudioDevice(int i);

    int setMuted(boolean z);

    int startDtmfTone(int i, char c);

    int stopDtmfTone(int i);
}
