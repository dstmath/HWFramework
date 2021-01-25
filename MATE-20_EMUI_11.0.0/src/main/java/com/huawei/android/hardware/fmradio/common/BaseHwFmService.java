package com.huawei.android.hardware.fmradio.common;

import android.os.IBinder;
import android.os.RemoteException;
import com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.fmradio.IFmEventCallback;

public class BaseHwFmService implements BaseHwFmServiceInterface {
    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int acquireFd(String path) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int audioControl(int fd, int control, int field) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int cancelSearch(int fd) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int closeFd(int fd) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getFreq(int fd) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFreq(int fd, int freq) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getControl(int fd, int id) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setControl(int fd, int id, int value) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int startSearch(int fd, int dir) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getBuffer(int fd, byte[] buff, int index) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRSSI(int fd) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setBand(int fd, int low, int high) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getLowerBand(int fd) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getUpperBand(int fd) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setMonoStereo(int fd, int val) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRawRds(int fd, byte[] buff, int count) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void setNotchFilter(boolean value) throws RemoteException {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getAudioQuilty(int fd, int value) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFmSnrThresh(int fd, int value) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int setFmRssiThresh(int fd, int value) throws RemoteException {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void setFmDeviceConnectionState(int state) throws RemoteException {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void startListner(int fd, IFmEventCallback cb) throws RemoteException {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void stopListner() throws RemoteException {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean enable(BaseFmConfig configSettings, IBinder callback) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean disable() {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean mtkCancelSearch() {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean setStation(int frequencyKHz) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean setRdsOnOff(int onOff) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public String getPrgmServices() {
        return null;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public String getRadioText() {
        return null;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getPrgmType() {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getPrgmId() {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void registerListener(IBaseFmRxEvCallbacksAdaptor listener) {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void unregisterListener(IBaseFmRxEvCallbacksAdaptor listener) {
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean setLowPwrMode(boolean val) {
        return false;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRdsStatus() {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRssi() {
        return 0;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int[] getAfInfo() {
        return new int[0];
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        return false;
    }
}
