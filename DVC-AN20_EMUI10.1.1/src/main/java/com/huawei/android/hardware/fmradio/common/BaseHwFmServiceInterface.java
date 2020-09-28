package com.huawei.android.hardware.fmradio.common;

import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.fmradio.IFmEventCallback;

interface BaseHwFmServiceInterface {
    int acquireFd(String str) throws RemoteException;

    int audioControl(int i, int i2, int i3) throws RemoteException;

    int cancelSearch(int i) throws RemoteException;

    int closeFd(int i) throws RemoteException;

    boolean disable();

    boolean enable(BaseFmConfig baseFmConfig, IBinder iBinder);

    int[] getAfInfo();

    int getAudioQuilty(int i, int i2) throws RemoteException;

    int getBuffer(int i, byte[] bArr, int i2) throws RemoteException;

    int getControl(int i, int i2) throws RemoteException;

    int getFreq(int i) throws RemoteException;

    int getLowerBand(int i) throws RemoteException;

    int getPrgmId();

    String getPrgmServices();

    int getPrgmType();

    int getRSSI(int i) throws RemoteException;

    String getRadioText();

    int getRawRds(int i, byte[] bArr, int i2) throws RemoteException;

    int getRdsStatus();

    int getRssi();

    int getUpperBand(int i) throws RemoteException;

    boolean mtkCancelSearch();

    void registerListener(IBaseFmRxEvCallbacksAdaptor iBaseFmRxEvCallbacksAdaptor);

    boolean registerRdsGroupProcessing(int i);

    boolean searchStations(int i, int i2, int i3, int i4, int i5);

    int setBand(int i, int i2, int i3) throws RemoteException;

    int setControl(int i, int i2, int i3) throws RemoteException;

    void setFmDeviceConnectionState(int i) throws RemoteException;

    int setFmRssiThresh(int i, int i2) throws RemoteException;

    int setFmSnrThresh(int i, int i2) throws RemoteException;

    int setFreq(int i, int i2) throws RemoteException;

    boolean setLowPwrMode(boolean z);

    int setMonoStereo(int i, int i2) throws RemoteException;

    void setNotchFilter(boolean z) throws RemoteException;

    boolean setRdsOnOff(int i);

    boolean setStation(int i);

    void startListner(int i, IFmEventCallback iFmEventCallback) throws RemoteException;

    int startSearch(int i, int i2) throws RemoteException;

    void stopListner() throws RemoteException;

    void unregisterListener(IBaseFmRxEvCallbacksAdaptor iBaseFmRxEvCallbacksAdaptor);
}
