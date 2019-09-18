package com.android.internal.telephony.test;

public interface SimulatedRadioControl {
    void pauseResponses();

    void progressConnectingCallState();

    void progressConnectingToActive();

    void resumeResponses();

    void setAutoProgressConnectingCall(boolean z);

    void setNextCallFailCause(int i);

    void setNextDialFailImmediately(boolean z);

    void shutdown();

    void triggerHangupAll();

    void triggerHangupBackground();

    void triggerHangupForeground();

    void triggerIncomingSMS(String str);

    void triggerIncomingUssd(String str, String str2);

    void triggerRing(String str);

    void triggerSsn(int i, int i2);
}
