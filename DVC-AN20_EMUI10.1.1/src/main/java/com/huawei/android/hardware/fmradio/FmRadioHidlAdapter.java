package com.huawei.android.hardware.fmradio;

import android.os.IHwBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.hardware.fmradio.FmRadioHidlAdapter;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.annotation.HwSystemApi;
import vendor.mediatek.hardware.fm.V1_0.IFmRadio;
import vendor.mediatek.hardware.fm.V1_0.IFmRadioCallback;

@HwSystemApi
public class FmRadioHidlAdapter {
    private static final String FM_HIDL_SERVICE_NAME = "fm_hidl_service";
    private static final String TAG = "FmRadioHidlAdapter";
    private final IHwBinder.DeathRecipient mFmHidlDeathRecipient;
    volatile IFmRadio mFmProxy;
    private Object mHidlLock;

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

    public /* synthetic */ void lambda$new$0$FmRadioHidlAdapter(long cookie) {
        Log.wtf(TAG, "IFmRadio service died: cookie=" + cookie);
        synchronized (this.mHidlLock) {
            this.mFmProxy = null;
        }
    }

    public FmRadioHidlAdapter() {
        this.mFmProxy = null;
        this.mHidlLock = new Object();
        this.mFmHidlDeathRecipient = new IHwBinder.DeathRecipient() {
            /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$rXcjwSlfXEYmq_wkEhmRNSweF0 */

            public final void serviceDied(long j) {
                FmRadioHidlAdapter.this.lambda$new$0$FmRadioHidlAdapter(j);
            }
        };
        this.mFmProxy = getFmProxy();
    }

    private IFmRadio getFmProxy() {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                try {
                    this.mFmProxy = IFmRadio.getService(FM_HIDL_SERVICE_NAME);
                    Log.d(TAG, "FmHidlClient Service : mFmProxy" + this.mFmProxy);
                    if (!this.mFmProxy.linkToDeath(this.mFmHidlDeathRecipient, 0)) {
                        Log.wtf(TAG, "Error on linkToDeath on IFmRadio");
                        this.mFmProxy = null;
                        return null;
                    }
                } catch (RemoteException e) {
                    this.mFmProxy = null;
                    Log.e(TAG, "FmHidlClient fail");
                }
            }
            return this.mFmProxy;
        }
    }

    public int enable(FmRadioCallbackWrapper fmRadioCallbackWrapper) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.enable(new HidlFmRadioCallbackWrapper(fmRadioCallbackWrapper));
        }
    }

    public int disable() throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.disable();
        }
    }

    public int startSearch(int dir) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.startSearch(dir);
        }
    }

    public int cancelSearch() throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.cancelSearch();
        }
    }

    public int setControl(int id, int value) throws RemoteException {
        synchronized (this.mHidlLock) {
            if ("true".equals(SystemPropertiesEx.get("ro.config.fm.disable", "false"))) {
                return 4;
            }
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.setControl(id, value);
        }
    }

    public int getControl(int id) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.getControl(id);
        }
    }

    public void getFreq(GetFreqCallbackWrapper getFreqCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getFreq(new IFmRadio.getFreqCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$oB4pbyp8iyXF00Nwd_HdILLivw4 */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getFreqCallback
                    public final void onValues(int i, int i2) {
                        FmRadioHidlAdapter.lambda$getFreq$1(FmRadioHidlAdapter.GetFreqCallbackWrapper.this, i, i2);
                    }
                });
            }
        }
    }

    public int setFreq(int freq) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.setFreq(freq);
        }
    }

    public void getRssi(GetRssiCallbackWrapper getRssiCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getRssi(new IFmRadio.getRssiCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$LLFaGtgR0otYg2vhZMcpUMmQY0 */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getRssiCallback
                    public final void onValues(int i, int i2) {
                        FmRadioHidlAdapter.lambda$getRssi$2(FmRadioHidlAdapter.GetRssiCallbackWrapper.this, i, i2);
                    }
                });
            }
        }
    }

    public int setBand(int low, int high) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.setBand(low, high);
        }
    }

    public int setRdsOnOff(int onOff) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy == null) {
                return 0;
            }
            return this.mFmProxy.setRdsOnOff(onOff);
        }
    }

    public void getRdsStatus(GetRdsStatusCallbackWrapper getRdsStatusCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getRdsStatus(new IFmRadio.getRdsStatusCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$k_UoX_jSxjriG8ax_6a0TLzt60 */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getRdsStatusCallback
                    public final void onValues(int i, int i2) {
                        FmRadioHidlAdapter.lambda$getRdsStatus$3(FmRadioHidlAdapter.GetRdsStatusCallbackWrapper.this, i, i2);
                    }
                });
            }
        }
    }

    public void getRadioText(GetRadioTextCallbackWrapper getRadioTextCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getRadioText(new IFmRadio.getRadioTextCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$JfIEz6UAIf9l5PSKyThK0HKnvvs */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getRadioTextCallback
                    public final void onValues(int i, String str) {
                        FmRadioHidlAdapter.lambda$getRadioText$4(FmRadioHidlAdapter.GetRadioTextCallbackWrapper.this, i, str);
                    }
                });
            }
        }
    }

    public void getPrgmServices(GetPrgmServicesCallbackWrapper getPrgmServicesCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getPrgmServices(new IFmRadio.getPrgmServicesCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$DKoTaWN4WDbTOV8y_zOOA0lIGSA */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getPrgmServicesCallback
                    public final void onValues(int i, String str) {
                        FmRadioHidlAdapter.lambda$getPrgmServices$5(FmRadioHidlAdapter.GetPrgmServicesCallbackWrapper.this, i, str);
                    }
                });
            }
        }
    }

    public void getPrgmId(GetPrgmIdCallbackWrapper getPrgmIdCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getPrgmId(new IFmRadio.getPrgmIdCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$tWKofoYAWQGq8yQlfjxLZ_fJYJ4 */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getPrgmIdCallback
                    public final void onValues(int i, int i2) {
                        FmRadioHidlAdapter.lambda$getPrgmId$6(FmRadioHidlAdapter.GetPrgmIdCallbackWrapper.this, i, i2);
                    }
                });
            }
        }
    }

    public void getPrgmType(GetPrgmTypeCallbackWrapper getPrgmTypeCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getPrgmType(new IFmRadio.getPrgmTypeCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$vBF1G4EghxMiH_mcsY2ri6FybEk */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getPrgmTypeCallback
                    public final void onValues(int i, int i2) {
                        FmRadioHidlAdapter.lambda$getPrgmType$7(FmRadioHidlAdapter.GetPrgmTypeCallbackWrapper.this, i, i2);
                    }
                });
            }
        }
    }

    public void getAfInfo(GetAfInfoCallbackWrapper getAfInfoCallback) throws RemoteException {
        synchronized (this.mHidlLock) {
            if (this.mFmProxy != null) {
                this.mFmProxy.getAfInfo(new IFmRadio.getAfInfoCallback() {
                    /* class com.huawei.android.hardware.fmradio.$$Lambda$FmRadioHidlAdapter$sgN1qGI1AtGT8gDk7H6NQFjRlY */

                    @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadio.getAfInfoCallback
                    public final void onValues(int i, int[] iArr) {
                        FmRadioHidlAdapter.lambda$getAfInfo$8(FmRadioHidlAdapter.GetAfInfoCallbackWrapper.this, i, iArr);
                    }
                });
            }
        }
    }

    private class HidlFmRadioCallbackWrapper extends IFmRadioCallback.Stub {
        private FmRadioCallbackWrapper mFmRadioCallbackWrapper;

        HidlFmRadioCallbackWrapper(FmRadioCallbackWrapper fmRadioCallbackWrapper) {
            this.mFmRadioCallbackWrapper = fmRadioCallbackWrapper;
        }

        @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadioCallback
        public void eventNotifyCb(int result, int event) throws RemoteException {
            this.mFmRadioCallbackWrapper.eventNotifyCb(result, event);
        }

        @Override // vendor.mediatek.hardware.fm.V1_0.IFmRadioCallback
        public void getControlCb(int result, int id, int value) throws RemoteException {
            this.mFmRadioCallbackWrapper.getControlCb(result, id, value);
        }
    }
}
