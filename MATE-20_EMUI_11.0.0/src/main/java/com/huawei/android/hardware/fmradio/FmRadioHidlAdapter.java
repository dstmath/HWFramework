package com.huawei.android.hardware.fmradio;

import android.os.RemoteException;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class FmRadioHidlAdapter {

    public interface FmRadioCallbackWrapper {
        void eventNotifyCb(int i, int i2);

        void getControlCb(int i, int i2, int i3);
    }

    public interface GetAfInfoCallbackWrapper {
        void onValues(int i, int[] iArr);
    }

    public interface GetFreqCallbackWrapper {
        void onValues(int i, int i2);
    }

    public interface GetPrgmIdCallbackWrapper {
        void onValues(int i, int i2);
    }

    public interface GetPrgmServicesCallbackWrapper {
        void onValues(int i, String str);
    }

    public interface GetPrgmTypeCallbackWrapper {
        void onValues(int i, int i2);
    }

    public interface GetRadioTextCallbackWrapper {
        void onValues(int i, String str);
    }

    public interface GetRdsStatusCallbackWrapper {
        void onValues(int i, int i2);
    }

    public interface GetRssiCallbackWrapper {
        void onValues(int i, int i2);
    }

    public int enable(FmRadioCallbackWrapper fmRadioCallbackWrapper) throws RemoteException {
        return 0;
    }

    public int disable() throws RemoteException {
        return 0;
    }

    public int startSearch(int dir) throws RemoteException {
        return 0;
    }

    public int cancelSearch() throws RemoteException {
        return 0;
    }

    public int setControl(int id, int value) throws RemoteException {
        return 0;
    }

    public int getControl(int id) throws RemoteException {
        return 0;
    }

    public void getFreq(GetFreqCallbackWrapper getFreqCallback) throws RemoteException {
    }

    public int setFreq(int freq) throws RemoteException {
        return 0;
    }

    public void getRssi(GetRssiCallbackWrapper getRssiCallback) throws RemoteException {
    }

    public int setBand(int low, int high) throws RemoteException {
        return 0;
    }

    public int setRdsOnOff(int onOff) throws RemoteException {
        return 0;
    }

    public void getRdsStatus(GetRdsStatusCallbackWrapper getRdsStatusCallback) throws RemoteException {
    }

    public void getRadioText(GetRadioTextCallbackWrapper getRadioTextCallback) throws RemoteException {
    }

    public void getPrgmServices(GetPrgmServicesCallbackWrapper getPrgmServicesCallback) throws RemoteException {
    }

    public void getPrgmId(GetPrgmIdCallbackWrapper getPrgmIdCallback) throws RemoteException {
    }

    public void getPrgmType(GetPrgmTypeCallbackWrapper getPrgmTypeCallback) throws RemoteException {
    }

    public void getAfInfo(GetAfInfoCallbackWrapper getAfInfoCallback) throws RemoteException {
    }
}
