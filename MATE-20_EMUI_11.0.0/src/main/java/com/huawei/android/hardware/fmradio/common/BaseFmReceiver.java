package com.huawei.android.hardware.fmradio.common;

import com.huawei.android.hardware.fmradio.FmRxEvCallbacks;

public interface BaseFmReceiver {
    boolean cancelSearch();

    boolean disable();

    boolean enable(BaseFmConfig baseFmConfig);

    boolean enableAFjump(boolean z);

    int[] getAFInfo();

    int getAudioQuilty(int i);

    int getFMState();

    boolean getInternalAntenna();

    int getPowerMode();

    byte[] getRawRDS(int i);

    int getRssi();

    int[] getRssiLimit();

    int getSignalThreshold();

    int[] getStationList();

    int getTunedFrequency();

    boolean registerClient(FmRxEvCallbacks fmRxEvCallbacks);

    boolean registerRdsGroupProcessing(int i);

    boolean reset();

    boolean searchStationList(int i, int i2, int i3, int i4);

    boolean searchStations(int i, int i2, int i3);

    boolean searchStations(int i, int i2, int i3, int i4, int i5);

    void setFmDeviceConnectionState(int i);

    int setFmRssiThresh(int i);

    int setFmSnrThresh(int i);

    boolean setInternalAntenna(boolean z);

    boolean setMuteMode(int i);

    boolean setPowerMode(int i);

    boolean setRdsGroupOptions(int i, int i2, boolean z);

    boolean setSignalThreshold(int i);

    boolean setStation(int i);

    boolean setStereoMode(boolean z);

    boolean unregisterClient();
}
