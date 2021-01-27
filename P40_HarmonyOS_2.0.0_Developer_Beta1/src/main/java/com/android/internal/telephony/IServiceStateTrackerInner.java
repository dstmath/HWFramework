package com.android.internal.telephony;

import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.telephony.CellIdentity;
import android.telephony.CellLocation;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;

public interface IServiceStateTrackerInner {
    void clearmLastCellInfoList();

    PersistableBundle getCarrierConfigHw();

    int getCarrierNameDisplayBitmask(ServiceState serviceState);

    CellLocation getCellLocationInfo();

    int getCid(CellIdentity cellIdentity);

    boolean getDesiredPowerState();

    boolean getDoRecoveryTriggerState();

    long getLastCellInfoReqTime();

    long getNitzSpaceTime();

    boolean getNitzTimeZoneDetectionSuccessful();

    SignalStrength getSignalStrength();

    CellIdentity getmCellIdentity();

    ServiceState getmNewSSHw();

    ServiceState getmSSHw();

    void handleLteEmmCause(int i, int i2, int i3);

    void handleNetworkRejectionEx(int i, int i2);

    boolean hasSecondaryCellServing();

    void onSignalStrengthResultHw(Message message);

    void pollState();

    void registerForDataRegStateOrRatChanged(int i, Handler handler, int i2, Object obj);

    void setNewNsaState(int i);

    void setRetryNotifySsState(boolean z);

    void setSignalStrength(SignalStrength signalStrength);

    void unregisterForDataRegStateOrRatChanged(int i, Handler handler);

    void updateSpnDisplayHw();
}
