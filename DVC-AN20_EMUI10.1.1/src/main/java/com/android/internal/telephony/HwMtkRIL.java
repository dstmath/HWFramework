package com.android.internal.telephony;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.os.Registrant;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.HwTelephonyManager;
import android.telephony.Rlog;
import com.android.ims.HwImsManagerInner;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.uicc.IccUtils;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import vendor.huawei.hardware.mtkradio.V1_0.IMtkRadioEx;

public class HwMtkRIL extends RIL {
    private static final int CLOSE_SWITCH = 0;
    private static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = {"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final int HW_VOLTE_USER_SWITCH_OFF = 0;
    private static final int HW_VOLTE_USER_SWITCH_ON = 1;
    static final String[] MTK_HIDL_SERVICE_NAME = {"mtkSlot1", "mtkSlot2", "mtkSlot3", "mtkSlot4"};
    private static final int NR_MODE_NSA = 1;
    private static final int NR_MODE_UNKNOWN = 0;
    private static final String NR_OPTION_MODE = "nr_option_mode";
    private static final int OPEN_SWITCH = 1;
    static final String RILJ_LOG_TAG = "HwMtkRILJ";
    private final int REGISTER_TYPE_MAX_TX_POWER;
    protected Registrant mCsconModeInfoRegistrant;
    public Integer mInstanceId;
    protected Context mMtkContext;
    boolean mMtkRilJIntiDone;
    HwMtkRadioIndication mRadioIndicationMtk;
    volatile IMtkRadioEx mRadioProxyMtk;
    HwMtkRadioResponse mRadioResponseMtk;

    public interface MtkRILCommand {
        void excute(IMtkRadioEx iMtkRadioEx, int i) throws RemoteException, RuntimeException;
    }

    @VisibleForTesting
    public HwMtkRIL(Context context, int preferredNetworkType, int cdmaSubscription) {
        this(context, preferredNetworkType, cdmaSubscription, null);
    }

    public HwMtkRIL(Context context, int preferredNetworkType, int cdmaSubscription, Integer instanceId) {
        super(context, preferredNetworkType, cdmaSubscription, instanceId);
        this.mRadioProxyMtk = null;
        this.mMtkRilJIntiDone = false;
        this.REGISTER_TYPE_MAX_TX_POWER = 4;
        mtkRiljLog("constructor: sub = " + instanceId);
        this.mMtkContext = context;
        this.mInstanceId = instanceId;
        this.mRadioResponseMtk = new HwMtkRadioResponse(this);
        this.mRadioIndicationMtk = new HwMtkRadioIndication(this);
        this.mMtkRilJIntiDone = true;
        getMtkRadioProxy(null);
    }

    private IMtkRadioEx getMtkRadioProxy(Message result) {
        if (!this.mMtkRilJIntiDone) {
            return null;
        }
        if (!this.mIsMobileNetworkSupported) {
            if (result != null) {
                AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                result.sendToTarget();
            }
            return null;
        } else if (this.mRadioProxyMtk != null) {
            return this.mRadioProxyMtk;
        } else {
            try {
                this.mRadioProxyMtk = IMtkRadioEx.getService(MTK_HIDL_SERVICE_NAME[this.mPhoneId == null ? 0 : this.mPhoneId.intValue()], false);
                if (this.mRadioProxyMtk != null) {
                    this.mRadioProxyMtk.linkToDeath(this.mRadioProxyDeathRecipient, this.mRadioProxyCookie.incrementAndGet());
                    this.mRadioProxyMtk.setResponseFunctionsMtk(this.mRadioResponseMtk, this.mRadioIndicationMtk);
                } else {
                    mtkRiljLoge("getMtkRadioProxy: mRadioProxyMtk == null");
                }
            } catch (RemoteException | RuntimeException e) {
                this.mRadioProxyMtk = null;
                mtkRiljLoge("getMtkRadioProxy getService/setResponseFunctions got RemoteException | RuntimeException");
            }
            if (this.mRadioProxyMtk == null) {
                if (result != null) {
                    AsyncResult.forMessage(result, (Object) null, CommandException.fromRilErrno(1));
                    result.sendToTarget();
                }
                this.mRilHandler.sendMessageDelayed(this.mRilHandler.obtainMessage(6, Long.valueOf(this.mRadioProxyCookie.get())), (long) IRADIO_GET_SERVICE_DELAY_MILLIS);
            }
            return this.mRadioProxyMtk;
        }
    }

    public void setImsSwitch(boolean on) {
        setImsSwitch(on, true);
    }

    protected static String retToString(int req, Object ret) {
        return RIL.retToString(req, ret);
    }

    public void getICCID(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(2075, result, getmRILDefaultWorkSourceHw());
            addRequestEx(rr);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getIccid(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(rr.mRequest), e, rr);
            }
        }
    }

    public void setLteReleaseVersion(int mode, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(2108, result, getmRILDefaultWorkSourceHw());
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " mode = " + mode);
            try {
                radioProxy.setLteReleaseVersion(rr.mSerial, mode);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(rr.mRequest), e, rr);
            }
        }
    }

    public void getLteReleaseVersion(Message result) {
        IMtkRadioEx radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(2109, result, getmRILDefaultWorkSourceHw());
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest));
            try {
                radioProxy.getLteReleaseVersion(rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(rr.mRequest), e, rr);
            }
        }
    }

    public void invokeOemRilRequestRaw(byte[] data, Message response) {
        IMtkRadioEx radioProxy = getMtkRadioProxy(response);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(59, response, getmRILDefaultWorkSourceHw());
            addRequestEx(rr);
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + "[" + IccUtils.bytesToHexString(data) + "]");
            try {
                radioProxy.sendRequestRaw(rr.mSerial, primitiveArrayToArrayList(data));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(rr.mRequest), e, rr);
            }
        }
    }

    public void invokeOemRilRequestStrings(String[] strings, Message result) {
        IMtkRadioEx radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(60, result, getmRILDefaultWorkSourceHw());
            addRequestEx(rr);
            String logStr = "";
            for (int i = 0; i < strings.length; i++) {
                logStr = logStr + strings[i] + " ";
            }
            mtkRiljLog(rr.serialString() + "> " + requestToString(rr.mRequest) + " strings = " + logStr);
            try {
                radioProxy.sendRequestStrings(rr.mSerial, new ArrayList(Arrays.asList(strings)));
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(rr.mRequest), e, rr);
            }
        }
    }

    private void mtkRiljLog(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.i(RILJ_LOG_TAG, sb.toString());
    }

    private void mtkRiljLoge(String msg) {
        String str;
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        if (this.mPhoneId != null) {
            str = " [SUB" + this.mPhoneId + "]";
        } else {
            str = "";
        }
        sb.append(str);
        Rlog.e(RILJ_LOG_TAG, sb.toString());
    }

    public void registerCsconModeInfo(Handler h, int what, Object obj) {
        this.mCsconModeInfoRegistrant = new Registrant(h, what, obj);
    }

    public void unregisterCsconModeInfo(Handler h) {
        Registrant registrant = this.mCsconModeInfoRegistrant;
        if (registrant != null && registrant.getHandler() == h) {
            this.mCsconModeInfoRegistrant.clear();
            this.mCsconModeInfoRegistrant = null;
        }
    }

    public void invokeIRadio(int requestId, Message result, MtkRILCommand cmd) {
        IMtkRadioEx radioProxy = getMtkRadioProxy(result);
        if (radioProxy != null) {
            RILRequest rr = RILRequest.obtain(requestId, result, getmRILDefaultWorkSourceHw());
            addRequestEx(rr);
            mtkRiljLog(rr.serialString() + "> " + requestToString(requestId));
            try {
                cmd.excute(radioProxy, rr.mSerial);
            } catch (RemoteException | RuntimeException e) {
                handleRadioProxyExceptionForRREx(requestToString(requestId), e, rr);
            }
        }
    }

    public void setCsconEnabled(final int enable, Message result) {
        mtkRiljLog("setCsconEnabled enable == " + enable);
        invokeIRadio(2183, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass1 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                radio.setCsconEnabled(serial, enable);
            }
        });
    }

    public void getCsconEnabled(Message result) {
        mtkRiljLog("getCsconEnabled");
        invokeIRadio(2184, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass2 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                radio.getCsconEnabled(serial);
            }
        });
    }

    public void resetMTKRadioProxy() {
        getMtkRadioProxy(null);
    }

    public void clearMTKRadioProxy() {
        this.mRadioProxyMtk = null;
    }

    public String getHwPrlVersion() {
        return SystemProperties.get("persist.radio.hwprlversion", "0");
    }

    public String getHwUimid() {
        return SystemProperties.get("persist.radio.hwuimid", "0");
    }

    public void queryServiceCellBand(Message result) {
        mtkRiljLog("queryServiceCellBand no need operation");
        sendResponseToTarget(result, 2);
    }

    public boolean getAntiFakeBaseStation(Message response) {
        mtkRiljLog("getAntiFakeBaseStation ");
        invokeIRadio(2180, response, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass3 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                radio.getCapOfRecPseBaseStation(serial);
            }
        });
        return true;
    }

    public void getCardTrayInfo(Message result) {
        mtkRiljLog("getCardTrayInfo");
        invokeIRadio(2181, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass4 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                radio.getCardTrayInfo(serial);
            }
        });
    }

    private boolean setTXPowerEnable(final int enable, Message result) {
        mtkRiljLog("setTXPowerRequest enable == " + enable);
        invokeIRadio(2198, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass5 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                radio.setTxPowerStatus(serial, enable);
            }
        });
        return true;
    }

    public boolean openSwitchOfUploadAntOrMaxTxPower(int mask) {
        if (mask == 4) {
            return setTXPowerEnable(1, null);
        }
        return false;
    }

    public boolean closeSwitchOfUploadAntOrMaxTxPower(int mask) {
        if (mask == 4) {
            return setTXPowerEnable(0, null);
        }
        return false;
    }

    public void deactivateNrScgCommunication(final boolean isDeactivate, final boolean isAllowSCGAdd, Message result) {
        riljLog("deactivateNrScgCommunication isDeactivate == " + isDeactivate);
        invokeIRadio(2200, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass6 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_1.IMtkRadioEx radioProxy11 = vendor.huawei.hardware.mtkradio.V1_1.IMtkRadioEx.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.deactivateNrScgCommunication(serial, isDeactivate, isAllowSCGAdd);
                }
            }
        });
    }

    public void getDeactivateNrScgCommunication(Message result) {
        riljLog("getDeactivateNrScgCommunication");
        invokeIRadio(2201, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass7 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_1.IMtkRadioEx radioProxy11 = vendor.huawei.hardware.mtkradio.V1_1.IMtkRadioEx.castFrom(radio);
                if (radioProxy11 != null) {
                    radioProxy11.getDeactivateNrScgCommunication(serial);
                }
            }
        });
    }

    public void setMaxUlSpeed(final int ulSpeed, Message result) {
        riljLog("setMaxUlSpeed");
        invokeIRadio(2202, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass8 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_2.IMtkRadioEx radioProxy12 = vendor.huawei.hardware.mtkradio.V1_2.IMtkRadioEx.castFrom(radio);
                if (radioProxy12 != null) {
                    radioProxy12.setMaxUlSpeed(serial, ulSpeed);
                }
            }
        });
    }

    public void smartRatSwitch(final int mode, final int rat, Message result) {
        riljLog("smartRatSwitch");
        invokeIRadio(2203, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass9 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_4.IMtkRadioEx radioProxy14 = vendor.huawei.hardware.mtkradio.V1_4.IMtkRadioEx.castFrom(radio);
                if (radioProxy14 != null) {
                    radioProxy14.smartRatSwitch(serial, mode, rat);
                }
            }
        });
    }

    public void getSmartRatSwitch(final int mode, Message result) {
        riljLog("getSmartRatSwitch");
        invokeIRadio(2204, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass10 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_4.IMtkRadioEx radioProxy14 = vendor.huawei.hardware.mtkradio.V1_4.IMtkRadioEx.castFrom(radio);
                if (radioProxy14 != null) {
                    radioProxy14.getSmartRatSwitch(serial, mode);
                }
            }
        });
    }

    public void setSmartSceneSwitch(final int mode, final int tGear, final int lGear, Message result) {
        riljLog("setSmartSceneSwitch");
        invokeIRadio(2205, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass11 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_4.IMtkRadioEx radioProxy14 = vendor.huawei.hardware.mtkradio.V1_4.IMtkRadioEx.castFrom(radio);
                if (radioProxy14 != null) {
                    radioProxy14.setSmartSceneSwitch(serial, mode, tGear, lGear);
                }
            }
        });
    }

    public void sendSarIndicator(final int cmdType, final String parameter, Message result) {
        riljLog("sendSarIndicator");
        invokeIRadio(2206, result, new MtkRILCommand() {
            /* class com.android.internal.telephony.HwMtkRIL.AnonymousClass12 */

            @Override // com.android.internal.telephony.HwMtkRIL.MtkRILCommand
            public void excute(IMtkRadioEx radio, int serial) throws RemoteException, RuntimeException {
                vendor.huawei.hardware.mtkradio.V1_5.IMtkRadioEx radioProxy15 = vendor.huawei.hardware.mtkradio.V1_5.IMtkRadioEx.castFrom(radio);
                if (radioProxy15 != null) {
                    radioProxy15.sendSarIndicator(serial, cmdType, parameter);
                }
            }
        });
    }

    public void getSignalStrength(Message result) {
        if (!HwTelephonyManager.getDefault().isNrSupported()) {
            HwMtkRIL.super.getSignalStrength(result);
        } else if (getRILReference() != null) {
            getRILReference().getSignalStrength(result);
        }
    }

    public void setNrSwitch(boolean on, Message onComplete) {
        mtkRiljLog("setNrSwitch : " + on);
        if (this.mContext != null && !isShowVolteSwitchInNsa(this.mContext, this.mInstanceId.intValue()) && HwImsManagerInner.isVolteEnabledByPlatform(this.mContext, this.mInstanceId.intValue())) {
            if (on) {
                setImsSwitch(on, false);
            } else {
                setImsSwitch(HwImsManagerInner.isEnhanced4gLteModeSettingEnabledByUser(this.mContext, this.mInstanceId.intValue()), false);
            }
            if (onComplete != null) {
                onComplete.sendToTarget();
            }
        } else if (onComplete != null) {
            onComplete.sendToTarget();
        }
    }

    private boolean isShowVolteSwitchInNsa(Context context, int slotId) {
        Boolean isVolteSwitchInNsa;
        if (context == null || Settings.System.getInt(context.getContentResolver(), NR_OPTION_MODE, 0) != 1 || (isVolteSwitchInNsa = (Boolean) HwCfgFilePolicy.getValue("show_volte_switch_in_nsa", slotId, Boolean.class)) == null || !isVolteSwitchInNsa.booleanValue()) {
            return false;
        }
        mtkRiljLog("isShowVolteSwitchInNsa is true, slotId -> " + slotId);
        return true;
    }

    private void setImsSwitch(boolean on, boolean isSaveDB) {
        mtkRiljLog("setImsSwitch " + on + "isSaveDB" + isSaveDB);
        if (this.mContext == null) {
            mtkRiljLog("setImsSwitch, context is null");
            return;
        }
        if (isSaveDB) {
            Settings.System.putInt(this.mContext.getContentResolver(), HwImsManagerInner.isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[this.mInstanceId.intValue()] : HW_VOLTE_USER_SWITCH, on ? 1 : 0);
        }
        HwImsManagerInner.setVolteSwitch(this.mContext, this.mInstanceId.intValue(), on);
    }
}
