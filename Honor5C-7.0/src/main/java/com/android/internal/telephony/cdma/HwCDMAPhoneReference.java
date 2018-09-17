package com.android.internal.telephony.cdma;

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Message;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.Rlog;
import com.android.internal.telephony.AbstractGsmCdmaPhone.CDMAPhoneReference;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.GsmCdmaPhone;
import com.android.internal.telephony.HwPhoneReferenceBase;
import com.android.internal.telephony.OperatorInfo;
import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimUtilsInner;

public class HwCDMAPhoneReference extends HwPhoneReferenceBase implements CDMAPhoneReference {
    private static final String LOG_TAG = "HwCDMAPhoneReference";
    private static final int MEID_LENGTH = 14;
    private static CDMAPhoneUtils cdmaPhoneUtils;
    private int mLteReleaseVersion;
    private String mPESN;
    private GsmCdmaPhone mPhone;
    private String subTag;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.cdma.HwCDMAPhoneReference.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.cdma.HwCDMAPhoneReference.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.cdma.HwCDMAPhoneReference.<clinit>():void");
    }

    public HwCDMAPhoneReference(GsmCdmaPhone cdmaPhone) {
        super(cdmaPhone);
        this.mPhone = cdmaPhone;
        this.subTag = "HwCDMAPhoneReference[" + this.mPhone.getPhoneId() + "]";
    }

    public String getMeid() {
        logd("[HwCDMAPhoneReference]getMeid() = xxxxxx");
        return cdmaPhoneUtils.getMeid(this.mPhone);
    }

    public String getPesn() {
        return this.mPESN;
    }

    public void registerForLineControlInfo(Handler h, int what, Object obj) {
        logd("some apk registerForLineControlInfo");
        this.mPhone.mCT.registerForLineControlInfo(h, what, obj);
    }

    public void unregisterForLineControlInfo(Handler h) {
        logd("some apk unregisterForLineControlInfo");
        this.mPhone.mCT.unregisterForLineControlInfo(h);
    }

    public void afterHandleMessage(Message msg) {
        logd("handleMessage what = " + msg.what);
        switch (msg.what) {
            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                logd("[HwGSMPhoneReference]Phone.EVENT_RADIO_AVAILABLE");
                logd("Radio available, get lte release version");
                this.mPhone.mCi.getLteReleaseVersion(this.mPhone.obtainMessage(108));
            case HwVSimConstants.EVENT_SET_APN_READY_DONE /*21*/:
                logd("handleMessage EVENT_GET_DEVICE_IDENTITY_DONE");
                if (cdmaPhoneUtils.getMeid(this.mPhone) != null && cdmaPhoneUtils.getMeid(this.mPhone).length() > MEID_LENGTH) {
                    cdmaPhoneUtils.setMeid(this.mPhone, cdmaPhoneUtils.getMeid(this.mPhone).substring(cdmaPhoneUtils.getMeid(this.mPhone).length() - 14));
                }
                AsyncResult ar = msg.obj;
                if (ar.exception == null) {
                    String[] respId = ar.result;
                    if (respId != null) {
                        logd("handleMessage respId.length = " + respId.length);
                    }
                    if (respId != null && respId.length >= 4) {
                        logd("handleMessage mPESN = xxxxxx");
                        this.mPESN = respId[2];
                    }
                }
            default:
                logd("unhandle event");
        }
    }

    public void closeRrc() {
        try {
            this.mPhone.mCi.getClass().getMethod("closeRrc", new Class[0]).invoke(this.mPhone.mCi, new Object[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchVoiceCallBackgroundState(int state) {
        this.mPhone.mCT.switchVoiceCallBackgroundState(state);
    }

    public void riseCdmaCutoffFreq(boolean on) {
        this.mPhone.mCi.riseCdmaCutoffFreq(on, null);
    }

    public boolean beforeHandleMessage(Message msg) {
        boolean msgHandled;
        logd("beforeHandleMessage what = " + msg.what);
        switch (msg.what) {
            case 108:
                logd("onGetLteReleaseVersionDone:");
                AsyncResult ar = msg.obj;
                msgHandled = true;
                if (ar.exception == null) {
                    int[] resultint = ar.result;
                    if (resultint.length != 0) {
                        logd("onGetLteReleaseVersionDone: result=" + resultint[0]);
                        switch (resultint[0]) {
                            case HwVSimUtilsInner.UE_OPERATION_MODE_VOICE_CENTRIC /*0*/:
                                this.mLteReleaseVersion = 0;
                                break;
                            case HwVSimUtilsInner.UE_OPERATION_MODE_DATA_CENTRIC /*1*/:
                                this.mLteReleaseVersion = 1;
                                break;
                            default:
                                this.mLteReleaseVersion = -1;
                                break;
                        }
                    }
                }
                logd("Error in get lte release version:" + ar.exception);
                break;
                break;
            case 111:
                logd("beforeHandleMessage handled->EVENT_SET_MODE_TO_AUTO ");
                msgHandled = true;
                this.mPhone.setNetworkSelectionModeAutomatic(null);
                break;
            case HwVSimEventHandler.EVENT_HOTPLUG_SWITCHMODE /*1000*/:
                logd("beforeHandleMessage handled->RETRY_GET_DEVICE_ID ");
                msgHandled = true;
                if (msg.arg2 != 2) {
                    logd("EVENT_RETRY_GET_DEVICE_ID msg.arg2:" + msg.arg2 + ", error!!");
                    break;
                }
                logd("start retry get DEVICE_ID_MASK_ALL");
                this.mPhone.mCi.getDeviceIdentity(this.mPhone.obtainMessage(21, msg.arg1, 0, null));
                break;
            default:
                return super.beforeHandleMessage(msg);
        }
        return msgHandled;
    }

    public boolean isCTSimCard(int slotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(slotId);
    }

    public void setLTEReleaseVersion(boolean state, Message response) {
        this.mPhone.mCi.setLTEReleaseVersion(state, response);
    }

    public int getLteReleaseVersion() {
        logd("getLteReleaseVersion: " + this.mLteReleaseVersion);
        return this.mLteReleaseVersion;
    }

    public boolean isChinaTelecom(int slotId) {
        return HwTelephonyManagerInner.getDefault().isChinaTelecom(slotId);
    }

    public void selectNetworkManually(OperatorInfo network, Message response) {
        loge("selectNetworkManually: not possible in CDMA");
        if (response != null) {
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void setNetworkSelectionModeAutomatic(Message response) {
        loge("method setNetworkSelectionModeAutomatic is NOT supported in CDMA!");
        if (response != null) {
            Rlog.e(LOG_TAG, "setNetworkSelectionModeAutomatic: not possible in CDMA- Posting exception");
            AsyncResult.forMessage(response).exception = new CommandException(Error.REQUEST_NOT_SUPPORTED);
            response.sendToTarget();
        }
    }

    public void registerForHWBuffer(Handler h, int what, Object obj) {
        this.mPhone.mCi.registerForHWBuffer(h, what, obj);
    }

    public void unregisterForHWBuffer(Handler h) {
        this.mPhone.mCi.unregisterForHWBuffer(h);
    }

    public void sendHWSolicited(Message reqMsg, int event, byte[] reqData) {
        this.mPhone.mCi.sendHWBufferSolicited(reqMsg, event, reqData);
    }

    public boolean cmdForECInfo(int event, int action, byte[] buf) {
        return this.mPhone.mCi.cmdForECInfo(event, action, buf);
    }

    private void logd(String msg) {
        Rlog.d(this.subTag, msg);
    }

    private void loge(String msg) {
        Rlog.e(this.subTag, msg);
    }
}
