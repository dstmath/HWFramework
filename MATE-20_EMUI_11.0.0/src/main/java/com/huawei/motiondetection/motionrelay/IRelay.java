package com.huawei.motiondetection.motionrelay;

public interface IRelay {
    void destroy();

    void setRelayListener(RelayListener relayListener);

    void startMotionReco(int i);

    void startMotionReco(int i, boolean z);

    void startMotionRecoAsUser(int i, int i2);

    void startMotionService();

    void stopMotionReco(int i);

    void stopMotionReco(int i, boolean z);

    void stopMotionRecoAsUser(int i, int i2);

    void stopMotionService();
}
