package com.huawei.android.hardware.mtkfmradio;

import android.os.RemoteException;
import android.util.Log;
import android.util.MutableInt;
import com.huawei.android.hardware.fmradio.FmRadioHidlAdapter;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.mtkfmradio.FmHidlClient;

/* access modifiers changed from: package-private */
public class FmHidlClient implements FmRadioHidlAdapter.FmRadioCallbackWrapper {
    private static final int CONVERT_RATE = 10;
    private static final String TAG = "FmHidlClient";
    private int[] mAfList;
    private IFmHidlClientCallbacks mCallbacks;
    volatile FmRadioHidlAdapter mFmProxy = null;
    private final Object mFmProxyLock = new Object();
    private FmState mFmState;

    FmHidlClient(IFmHidlClientCallbacks callbacks, FmState state) {
        this.mCallbacks = callbacks;
        this.mFmState = state;
    }

    public boolean enable(BaseFmConfig config) {
        Log.d(TAG, "FmHidlClient : enable");
        if (config == null) {
            Log.w(TAG, "enable(): config is null!");
            return false;
        }
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            if (fmProxy.setControl(7, config.getChSpacing()) != 0 || fmProxy.setControl(5, config.getEmphasis()) != 0 || fmProxy.setControl(6, config.getRdsStd()) != 0 || fmProxy.setBand(config.getLowerLimit(), config.getUpperLimit()) != 0 || fmProxy.setControl(2, config.getRadioBand()) != 0) {
                return false;
            }
            int res = fmProxy.enable(this);
            Log.d(TAG, "FmHidlClient : enable : re" + res);
            if (res == 0) {
                return true;
            }
            Log.e(TAG, "enable failed.");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "enable caught exception");
            return false;
        }
    }

    public boolean disable() {
        Log.d(TAG, "FmHidlClient : disable");
        try {
            int res = getFmProxy().disable();
            Log.d(TAG, "FmHidlClient : disable : re " + res);
            if (res == 0) {
                return true;
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "disable caught exception");
            return false;
        }
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        Log.d(TAG, "searchStations : mode: " + mode + " dwellPeriod: " + dwellPeriod + " direction :" + direction);
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            if (fmProxy.setControl(0, mode) != 0) {
                Log.e(TAG, "mode set control failed ");
                return false;
            } else if (fmProxy.setControl(1, dwellPeriod) != 0) {
                Log.e(TAG, "dwellperiod set control failed ");
                return false;
            } else if (pty != 0 && fmProxy.setControl(3, pty) != 0) {
                Log.e(TAG, "pty set control failed ");
                return false;
            } else if (pi == 0 || fmProxy.setControl(4, pi) == 0) {
                int res = fmProxy.startSearch(direction);
                Log.d(TAG, "searchStations : re: " + res);
                if (res == 0) {
                    return true;
                }
                Log.d(TAG, "serach failed");
                return false;
            } else {
                Log.e(TAG, "pi set control failed ");
                return false;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "searchStations caught exception");
            return true;
        }
    }

    public boolean cancelSearch() {
        try {
            if (getFmProxy().cancelSearch() != 0) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "cancelSearch caught exception");
            return false;
        }
    }

    public int getFreq() {
        int freq = -1;
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            MutableInt gotFreq = new MutableInt(0);
            fmProxy.getFreq(new FmRadioHidlAdapter.GetFreqCallbackWrapper(gotFreq) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$Gi02yYGmGAwRXSxzpGh1yKYI1s */
                private final /* synthetic */ MutableInt f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, int i2) {
                    FmHidlClient.this.lambda$getFreq$0$FmHidlClient(this.f$1, i, i2);
                }
            });
            freq = gotFreq.value;
        } catch (RemoteException e) {
            Log.e(TAG, "getFreq caught exception");
        }
        return freq * 10;
    }

    public /* synthetic */ void lambda$getFreq$0$FmHidlClient(MutableInt gotFreq, int result, int val) {
        if (checkResult(result)) {
            gotFreq.value = val;
        }
    }

    private boolean checkResult(int status) {
        if (status == 0) {
            return true;
        }
        Log.e(TAG, "Result is not OK ");
        return false;
    }

    public int setFreq(int freq) {
        try {
            return getFmProxy().setFreq(freq / 10);
        } catch (RemoteException e) {
            Log.e(TAG, "setFreq caught exception");
            return 0;
        }
    }

    public boolean setRdsOnOff(int onOff) {
        try {
            if (getFmProxy().setRdsOnOff(onOff) != 0) {
                return false;
            }
            return true;
        } catch (RemoteException e) {
            Log.e(TAG, "setRdsOnOff caught exception");
            return false;
        }
    }

    public boolean setLowPwrMode(boolean lpmode) {
        int re;
        FmRadioHidlAdapter fmProxy = getFmProxy();
        if (lpmode) {
            try {
                re = fmProxy.setControl(8, 1);
            } catch (RemoteException e) {
                Log.e(TAG, "setLowPwrMode caught exception");
                return false;
            }
        } else {
            re = fmProxy.setControl(8, 0);
        }
        if (re != 0) {
            return false;
        }
        return true;
    }

    public int getPrgmId() {
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            MutableInt gotId = new MutableInt(0);
            fmProxy.getPrgmId(new FmRadioHidlAdapter.GetPrgmIdCallbackWrapper(gotId) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$KNXiQzr8CcJKkEN6BbhzcgpZ6w */
                private final /* synthetic */ MutableInt f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, int i2) {
                    FmHidlClient.this.lambda$getPrgmId$1$FmHidlClient(this.f$1, i, i2);
                }
            });
            return gotId.value;
        } catch (RemoteException e) {
            Log.e(TAG, "getPrgmId caught exception");
            return -1;
        }
    }

    public /* synthetic */ void lambda$getPrgmId$1$FmHidlClient(MutableInt gotId, int result, int val) {
        if (checkResult(result)) {
            gotId.value = val;
        }
    }

    public int getPrgmType() {
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            MutableInt gotType = new MutableInt(0);
            fmProxy.getPrgmType(new FmRadioHidlAdapter.GetPrgmTypeCallbackWrapper(gotType) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$KMW8ndbQqyGoSpfuWE111C2r2uk */
                private final /* synthetic */ MutableInt f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, int i2) {
                    FmHidlClient.this.lambda$getPrgmType$2$FmHidlClient(this.f$1, i, i2);
                }
            });
            return gotType.value;
        } catch (RemoteException e) {
            Log.e(TAG, "getPrgmType caught exception");
            return -1;
        }
    }

    public /* synthetic */ void lambda$getPrgmType$2$FmHidlClient(MutableInt gotType, int result, int val) {
        if (checkResult(result)) {
            gotType.value = val;
        }
    }

    public int getRssi() {
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            MutableInt gotRssi = new MutableInt(0);
            fmProxy.getRssi(new FmRadioHidlAdapter.GetRssiCallbackWrapper(gotRssi) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$M14wO6GLjif3X8asxanESkC65FY */
                private final /* synthetic */ MutableInt f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, int i2) {
                    FmHidlClient.this.lambda$getRssi$3$FmHidlClient(this.f$1, i, i2);
                }
            });
            return gotRssi.value;
        } catch (RemoteException e) {
            Log.e(TAG, "getRssi caught exception");
            return -1;
        }
    }

    public /* synthetic */ void lambda$getRssi$3$FmHidlClient(MutableInt gotRssi, int result, int val) {
        if (checkResult(result)) {
            gotRssi.value = val;
        }
    }

    public int[] getAfInfo() {
        try {
            getFmProxy().getAfInfo(new FmRadioHidlAdapter.GetAfInfoCallbackWrapper() {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$MQx1c8cPgw26B_fOUWtxsD0p4 */

                public final void onValues(int i, int[] iArr) {
                    FmHidlClient.this.lambda$getAfInfo$4$FmHidlClient(i, iArr);
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "getAfInfo caught exception");
        }
        return this.mAfList;
    }

    public /* synthetic */ void lambda$getAfInfo$4$FmHidlClient(int result, int[] afList) {
        if (checkResult(result)) {
            this.mAfList = afList;
        }
    }

    public String getPrgmServices() {
        Mutable<String> gotPrgmServices = new Mutable<>();
        try {
            getFmProxy().getPrgmServices(new FmRadioHidlAdapter.GetPrgmServicesCallbackWrapper(gotPrgmServices) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$tuLpMRr2IKuF12DJH6imCwqKEYk */
                private final /* synthetic */ FmHidlClient.Mutable f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, String str) {
                    FmHidlClient.this.lambda$getPrgmServices$5$FmHidlClient(this.f$1, i, str);
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "getPrgmServices caught exception");
        }
        return (String) ((Mutable) gotPrgmServices).value;
    }

    public /* synthetic */ void lambda$getPrgmServices$5$FmHidlClient(Mutable gotPrgmServices, int result, String val) {
        if (checkResult(result)) {
            gotPrgmServices.value = val;
        }
    }

    public String getRadioText() {
        Mutable<String> gotRadioText = new Mutable<>();
        try {
            getFmProxy().getPrgmServices(new FmRadioHidlAdapter.GetPrgmServicesCallbackWrapper(gotRadioText) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$WNfMWz8aN9sPyOBi14GZ1dy_lMk */
                private final /* synthetic */ FmHidlClient.Mutable f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, String str) {
                    FmHidlClient.this.lambda$getRadioText$6$FmHidlClient(this.f$1, i, str);
                }
            });
        } catch (RemoteException e) {
            Log.e(TAG, "getRadioText caught exception");
        }
        return (String) ((Mutable) gotRadioText).value;
    }

    public /* synthetic */ void lambda$getRadioText$6$FmHidlClient(Mutable gotRadioText, int result, String val) {
        if (checkResult(result)) {
            gotRadioText.value = val;
        }
    }

    public int getRdsStatus() {
        FmRadioHidlAdapter fmProxy = getFmProxy();
        try {
            MutableInt gotRdsStatus = new MutableInt(0);
            fmProxy.getRdsStatus(new FmRadioHidlAdapter.GetRdsStatusCallbackWrapper(gotRdsStatus) {
                /* class com.huawei.android.hardware.mtkfmradio.$$Lambda$FmHidlClient$HRhmcd43XJIuXPIiKvdOVMvyUWA */
                private final /* synthetic */ MutableInt f$1;

                {
                    this.f$1 = r2;
                }

                public final void onValues(int i, int i2) {
                    FmHidlClient.this.lambda$getRdsStatus$7$FmHidlClient(this.f$1, i, i2);
                }
            });
            return gotRdsStatus.value;
        } catch (RemoteException e) {
            Log.e(TAG, "getRdsStatus caught exception");
            return -1;
        }
    }

    public /* synthetic */ void lambda$getRdsStatus$7$FmHidlClient(MutableInt gotRdsStatus, int result, int val) {
        if (checkResult(result)) {
            gotRdsStatus.value = val;
        }
    }

    public void eventNotifyCb(int result, int event) {
        if (event == 1) {
            int freq = getFreq();
            int state = this.mFmState.getSyncFmSearchState();
            Log.d(TAG, "Got TUNE_EVENT state:" + state);
            if (state == 0) {
                this.mFmState.setSearchState(3);
                this.mCallbacks.onSearchComplete(freq);
            } else if (freq > 0) {
                this.mCallbacks.onRadioTunedStatus(freq);
            } else {
                Log.e(TAG, "get frequency command failed");
            }
        } else if (event == 3) {
            int state2 = this.mFmState.getSyncFmSearchState();
            Log.d(TAG, "Got SEARCH_COMPLETE state:" + state2);
            if (state2 == 1 || state2 == 4) {
                this.mFmState.setSearchState(3);
                this.mCallbacks.onSearchComplete(getFreq());
                return;
            }
            Log.e(TAG, "Current state for SEARCH_COMPLETE is " + state2 + " abnormal");
        } else if (event == 2) {
            Log.d(TAG, "Got SEARCH_IN_PROGRESS");
            this.mCallbacks.onSearchInProgress();
        } else if (event == 4) {
            int state3 = this.mFmState.getSyncFmSearchState();
            Log.d(TAG, "Got SEARCH_CANCELLED state:" + state3);
            if (state3 != 4) {
                Log.e(TAG, "Current state for SEARCH_CANCELLED is " + state3 + " abnormal");
                return;
            }
            this.mFmState.setSearchState(3);
            this.mCallbacks.onSearchCancelled();
        } else if (event == 5) {
            Log.d(TAG, "Got RDS_AVAL_EVENT");
            this.mCallbacks.onRdsInfo(getRdsStatus());
        } else {
            Log.e(TAG, "Unknown Event");
        }
    }

    public void getControlCb(int result, int id, int value) {
    }

    private FmRadioHidlAdapter getFmProxy() {
        if (this.mFmProxy == null) {
            synchronized (this.mFmProxyLock) {
                if (this.mFmProxy == null) {
                    this.mFmProxy = new FmRadioHidlAdapter();
                }
            }
        }
        return this.mFmProxy;
    }

    /* access modifiers changed from: private */
    public static class Mutable<E> {
        private E value;

        Mutable() {
            this.value = null;
        }

        Mutable(E value2) {
            this.value = value2;
        }
    }
}
