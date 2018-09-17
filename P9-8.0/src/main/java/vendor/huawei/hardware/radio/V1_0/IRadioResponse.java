package vendor.huawei.hardware.radio.V1_0;

import android.hardware.radio.V1_0.ActivityStatsInfo;
import android.hardware.radio.V1_0.Call;
import android.hardware.radio.V1_0.CallForwardInfo;
import android.hardware.radio.V1_0.CardStatus;
import android.hardware.radio.V1_0.CarrierRestrictions;
import android.hardware.radio.V1_0.CdmaBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.CellInfo;
import android.hardware.radio.V1_0.DataRegStateResult;
import android.hardware.radio.V1_0.GsmBroadcastSmsConfigInfo;
import android.hardware.radio.V1_0.HardwareConfig;
import android.hardware.radio.V1_0.IccIoResult;
import android.hardware.radio.V1_0.LastCallFailCauseInfo;
import android.hardware.radio.V1_0.LceDataInfo;
import android.hardware.radio.V1_0.LceStatusInfo;
import android.hardware.radio.V1_0.NeighboringCell;
import android.hardware.radio.V1_0.OperatorInfo;
import android.hardware.radio.V1_0.RadioCapability;
import android.hardware.radio.V1_0.RadioError;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.hardware.radio.V1_0.SendSmsResult;
import android.hardware.radio.V1_0.SetupDataCallResult;
import android.hardware.radio.V1_0.SignalStrength;
import android.hardware.radio.V1_0.VoiceRegStateResult;
import android.hidl.base.V1_0.DebugInfo;
import android.hidl.base.V1_0.IBase;
import android.os.HwBinder;
import android.os.HwBlob;
import android.os.HwParcel;
import android.os.IHwBinder;
import android.os.IHwBinder.DeathRecipient;
import android.os.IHwInterface;
import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.internal.telephony.AbstractPhoneBase;
import com.android.internal.telephony.RadioNVItems;
import com.huawei.internal.telephony.HwRadarUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public interface IRadioResponse extends android.hardware.radio.V1_0.IRadioResponse {
    public static final String kInterfaceName = "vendor.huawei.hardware.radio@1.0::IRadioResponse";

    public static abstract class Stub extends HwBinder implements IRadioResponse {
        public IHwBinder asBinder() {
            return this;
        }

        public final ArrayList<String> interfaceChain() {
            return new ArrayList(Arrays.asList(new String[]{IRadioResponse.kInterfaceName, android.hardware.radio.V1_0.IRadioResponse.kInterfaceName, IBase.kInterfaceName}));
        }

        public final String interfaceDescriptor() {
            return IRadioResponse.kInterfaceName;
        }

        public final ArrayList<byte[]> getHashChain() {
            return new ArrayList(Arrays.asList(new byte[][]{new byte[]{(byte) -52, (byte) -96, (byte) 0, (byte) -43, (byte) -38, (byte) -10, (byte) -77, (byte) -22, (byte) 5, (byte) 122, (byte) -89, (byte) 47, (byte) 26, (byte) 39, (byte) 9, (byte) 126, (byte) 31, (byte) 121, (byte) 21, (byte) 98, (byte) 29, (byte) 21, (byte) -14, (byte) -36, (byte) 18, (byte) 19, (byte) 126, (byte) 21, (byte) -33, (byte) 62, (byte) -49, (byte) -104}, new byte[]{(byte) 45, (byte) -125, (byte) 58, (byte) -18, (byte) -48, (byte) -51, (byte) 29, (byte) 89, (byte) 67, (byte) 122, (byte) -54, (byte) 33, (byte) 11, (byte) -27, (byte) -112, (byte) -87, (byte) 83, (byte) -49, (byte) 50, (byte) -68, (byte) -74, (byte) 104, (byte) 60, (byte) -42, (byte) 61, (byte) 8, (byte) -105, (byte) 98, (byte) -90, (byte) 67, (byte) -5, (byte) 73}, new byte[]{(byte) -67, (byte) -38, (byte) -74, (byte) 24, (byte) 77, (byte) 122, (byte) 52, (byte) 109, (byte) -90, (byte) -96, (byte) 125, (byte) -64, (byte) -126, (byte) -116, (byte) -15, (byte) -102, (byte) 105, (byte) 111, (byte) 76, (byte) -86, (byte) 54, (byte) 17, (byte) -59, (byte) 31, (byte) 46, (byte) 20, (byte) 86, (byte) 90, (byte) 20, (byte) -76, (byte) 15, (byte) -39}}));
        }

        public final void setHALInstrumentation() {
        }

        public final boolean linkToDeath(DeathRecipient recipient, long cookie) {
            return true;
        }

        public final void ping() {
        }

        public final DebugInfo getDebugInfo() {
            DebugInfo info = new DebugInfo();
            info.pid = -1;
            info.ptr = 0;
            info.arch = 0;
            return info;
        }

        public final void notifySyspropsChanged() {
            SystemProperties.reportSyspropChanged();
        }

        public final boolean unlinkToDeath(DeathRecipient recipient) {
            return true;
        }

        public IHwInterface queryLocalInterface(String descriptor) {
            if (IRadioResponse.kInterfaceName.equals(descriptor)) {
                return this;
            }
            return null;
        }

        public void registerAsService(String serviceName) throws RemoteException {
            registerService(serviceName);
        }

        public String toString() {
            return interfaceDescriptor() + "@Stub";
        }

        public void onTransact(int _hidl_code, HwParcel _hidl_request, HwParcel _hidl_reply, int _hidl_flags) throws RemoteException {
            RadioResponseInfo info;
            SendSmsResult sms;
            SetupDataCallResult dcResponse;
            IccIoResult iccIo;
            IccIoResult result;
            RadioCapability rc;
            LceStatusInfo statusInfo;
            switch (_hidl_code) {
                case 1:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    CardStatus cardStatus = new CardStatus();
                    cardStatus.readFromParcel(_hidl_request);
                    getIccCardStatusResponse(info, cardStatus);
                    return;
                case 2:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    supplyIccPinForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 3:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    supplyIccPukForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 4:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    supplyIccPin2ForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 5:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    supplyIccPuk2ForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 6:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    changeIccPinForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 7:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    changeIccPin2ForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 8:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    supplyNetworkDepersonalizationResponse(info, _hidl_request.readInt32());
                    return;
                case 9:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCurrentCallsResponse(info, Call.readVectorFromParcel(_hidl_request));
                    return;
                case 10:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    dialResponse(info);
                    return;
                case 11:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getIMSIForAppResponse(info, _hidl_request.readString());
                    return;
                case 12:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    hangupConnectionResponse(info);
                    return;
                case 13:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    hangupWaitingOrBackgroundResponse(info);
                    return;
                case 14:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    hangupForegroundResumeBackgroundResponse(info);
                    return;
                case 15:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    switchWaitingOrHoldingAndActiveResponse(info);
                    return;
                case 16:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    conferenceResponse(info);
                    return;
                case 17:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    rejectCallResponse(info);
                    return;
                case 18:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    LastCallFailCauseInfo failCauseinfo = new LastCallFailCauseInfo();
                    failCauseinfo.readFromParcel(_hidl_request);
                    getLastCallFailCauseResponse(info, failCauseinfo);
                    return;
                case 19:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    SignalStrength sigStrength = new SignalStrength();
                    sigStrength.readFromParcel(_hidl_request);
                    getSignalStrengthResponse(info, sigStrength);
                    return;
                case 20:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    VoiceRegStateResult voiceRegResponse = new VoiceRegStateResult();
                    voiceRegResponse.readFromParcel(_hidl_request);
                    getVoiceRegistrationStateResponse(info, voiceRegResponse);
                    return;
                case 21:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    DataRegStateResult dataRegResponse = new DataRegStateResult();
                    dataRegResponse.readFromParcel(_hidl_request);
                    getDataRegistrationStateResponse(info, dataRegResponse);
                    return;
                case 22:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getOperatorResponse(info, _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 23:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setRadioPowerResponse(info);
                    return;
                case 24:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendDtmfResponse(info);
                    return;
                case 25:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sms = new SendSmsResult();
                    sms.readFromParcel(_hidl_request);
                    sendSmsResponse(info, sms);
                    return;
                case 26:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sms = new SendSmsResult();
                    sms.readFromParcel(_hidl_request);
                    sendSMSExpectMoreResponse(info, sms);
                    return;
                case 27:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    dcResponse = new SetupDataCallResult();
                    dcResponse.readFromParcel(_hidl_request);
                    setupDataCallResponse(info, dcResponse);
                    return;
                case 28:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    iccIo = new IccIoResult();
                    iccIo.readFromParcel(_hidl_request);
                    iccIOForAppResponse(info, iccIo);
                    return;
                case 29:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendUssdResponse(info);
                    return;
                case 30:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    cancelPendingUssdResponse(info);
                    return;
                case 31:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getClirResponse(info, _hidl_request.readInt32(), _hidl_request.readInt32());
                    return;
                case 32:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setClirResponse(info);
                    return;
                case 33:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCallForwardStatusResponse(info, CallForwardInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 34:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCallForwardResponse(info);
                    return;
                case 35:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCallWaitingResponse(info, _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 36:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCallWaitingResponse(info);
                    return;
                case 37:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    acknowledgeLastIncomingGsmSmsResponse(info);
                    return;
                case 38:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    acceptCallResponse(info);
                    return;
                case 39:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    deactivateDataCallResponse(info);
                    return;
                case 40:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getFacilityLockForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 41:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setFacilityLockForAppResponse(info, _hidl_request.readInt32());
                    return;
                case 42:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setBarringPasswordResponse(info);
                    return;
                case 43:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getNetworkSelectionModeResponse(info, _hidl_request.readBool());
                    return;
                case 44:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setNetworkSelectionModeAutomaticResponse(info);
                    return;
                case 45:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setNetworkSelectionModeManualResponse(info);
                    return;
                case 46:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getAvailableNetworksResponse(info, OperatorInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 47:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    startDtmfResponse(info);
                    return;
                case RadioError.NO_SMS_TO_ACK /*48*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    stopDtmfResponse(info);
                    return;
                case 49:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getBasebandVersionResponse(info, _hidl_request.readString());
                    return;
                case 50:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    separateConnectionResponse(info);
                    return;
                case 51:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setMuteResponse(info);
                    return;
                case 52:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getMuteResponse(info, _hidl_request.readBool());
                    return;
                case 53:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getClipResponse(info, _hidl_request.readInt32());
                    return;
                case 54:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getDataCallListResponse(info, SetupDataCallResult.readVectorFromParcel(_hidl_request));
                    return;
                case 55:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setSuppServiceNotificationsResponse(info);
                    return;
                case 56:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    writeSmsToSimResponse(info, _hidl_request.readInt32());
                    return;
                case 57:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    deleteSmsOnSimResponse(info);
                    return;
                case 58:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setBandModeResponse(info);
                    return;
                case 59:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getAvailableBandModesResponse(info, _hidl_request.readInt32Vector());
                    return;
                case RadioError.NETWORK_NOT_READY /*60*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendEnvelopeResponse(info, _hidl_request.readString());
                    return;
                case RadioError.NOT_PROVISIONED /*61*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendTerminalResponseToSimResponse(info);
                    return;
                case RadioError.NO_SUBSCRIPTION /*62*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    handleStkCallSetupRequestFromSimResponse(info);
                    return;
                case 63:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    explicitCallTransferResponse(info);
                    return;
                case 64:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setPreferredNetworkTypeResponse(info);
                    return;
                case 65:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getPreferredNetworkTypeResponse(info, _hidl_request.readInt32());
                    return;
                case 66:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getNeighboringCidsResponse(info, NeighboringCell.readVectorFromParcel(_hidl_request));
                    return;
                case HwRadarUtils.RADAR_LEVEL_C /*67*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setLocationUpdatesResponse(info);
                    return;
                case 68:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCdmaSubscriptionSourceResponse(info);
                    return;
                case 69:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCdmaRoamingPreferenceResponse(info);
                    return;
                case 70:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCdmaRoamingPreferenceResponse(info, _hidl_request.readInt32());
                    return;
                case 71:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setTTYModeResponse(info);
                    return;
                case 72:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getTTYModeResponse(info, _hidl_request.readInt32());
                    return;
                case 73:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setPreferredVoicePrivacyResponse(info);
                    return;
                case 74:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getPreferredVoicePrivacyResponse(info, _hidl_request.readBool());
                    return;
                case 75:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendCDMAFeatureCodeResponse(info);
                    return;
                case 76:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendBurstDtmfResponse(info);
                    return;
                case 77:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sms = new SendSmsResult();
                    sms.readFromParcel(_hidl_request);
                    sendCdmaSmsResponse(info, sms);
                    return;
                case 78:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    acknowledgeLastIncomingCdmaSmsResponse(info);
                    return;
                case 79:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getGsmBroadcastConfigResponse(info, GsmBroadcastSmsConfigInfo.readVectorFromParcel(_hidl_request));
                    return;
                case RadioNVItems.RIL_NV_LTE_NEXT_SCAN /*80*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setGsmBroadcastConfigResponse(info);
                    return;
                case 81:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setGsmBroadcastActivationResponse(info);
                    return;
                case RadioNVItems.RIL_NV_LTE_BSR_MAX_TIME /*82*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCdmaBroadcastConfigResponse(info, CdmaBroadcastSmsConfigInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 83:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCdmaBroadcastConfigResponse(info);
                    return;
                case 84:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCdmaBroadcastActivationResponse(info);
                    return;
                case 85:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCDMASubscriptionResponse(info, _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 86:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    writeSmsToRuimResponse(info, _hidl_request.readInt32());
                    return;
                case 87:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    deleteSmsOnRuimResponse(info);
                    return;
                case 88:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getDeviceIdentityResponse(info, _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString(), _hidl_request.readString());
                    return;
                case 89:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    exitEmergencyCallbackModeResponse(info);
                    return;
                case 90:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getSmscAddressResponse(info, _hidl_request.readString());
                    return;
                case 91:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setSmscAddressResponse(info);
                    return;
                case 92:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    reportSmsMemoryStatusResponse(info);
                    return;
                case 93:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    reportStkServiceIsRunningResponse(info);
                    return;
                case 94:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCdmaSubscriptionSourceResponse(info, _hidl_request.readInt32());
                    return;
                case 95:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    requestIsimAuthenticationResponse(info, _hidl_request.readString());
                    return;
                case 96:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    acknowledgeIncomingGsmSmsWithPduResponse(info);
                    return;
                case 97:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    iccIo = new IccIoResult();
                    iccIo.readFromParcel(_hidl_request);
                    sendEnvelopeWithStatusResponse(info, iccIo);
                    return;
                case 98:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getVoiceRadioTechnologyResponse(info, _hidl_request.readInt32());
                    return;
                case 99:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCellInfoListResponse(info, CellInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 100:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setCellInfoListRateResponse(info);
                    return;
                case 101:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setInitialAttachApnResponse(info);
                    return;
                case 102:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getImsRegistrationStateResponse(info, _hidl_request.readBool(), _hidl_request.readInt32());
                    return;
                case 103:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sms = new SendSmsResult();
                    sms.readFromParcel(_hidl_request);
                    sendImsSmsResponse(info, sms);
                    return;
                case AbstractPhoneBase.EVENT_ECC_NUM /*104*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    result = new IccIoResult();
                    result.readFromParcel(_hidl_request);
                    iccTransmitApduBasicChannelResponse(info, result);
                    return;
                case AbstractPhoneBase.EVENT_GET_IMSI_DONE /*105*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    iccOpenLogicalChannelResponse(info, _hidl_request.readInt32(), _hidl_request.readInt8Vector());
                    return;
                case 106:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    iccCloseLogicalChannelResponse(info);
                    return;
                case 107:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    result = new IccIoResult();
                    result.readFromParcel(_hidl_request);
                    iccTransmitApduLogicalChannelResponse(info, result);
                    return;
                case AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE /*108*/:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    nvReadItemResponse(info, _hidl_request.readString());
                    return;
                case 109:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    nvWriteItemResponse(info);
                    return;
                case 110:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    nvWriteCdmaPrlResponse(info);
                    return;
                case 111:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    nvResetConfigResponse(info);
                    return;
                case 112:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setUiccSubscriptionResponse(info);
                    return;
                case 113:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setDataAllowedResponse(info);
                    return;
                case 114:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getHardwareConfigResponse(info, HardwareConfig.readVectorFromParcel(_hidl_request));
                    return;
                case 115:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    result = new IccIoResult();
                    result.readFromParcel(_hidl_request);
                    requestIccSimAuthenticationResponse(info, result);
                    return;
                case 116:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setDataProfileResponse(info);
                    return;
                case 117:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    requestShutdownResponse(info);
                    return;
                case 118:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    rc = new RadioCapability();
                    rc.readFromParcel(_hidl_request);
                    getRadioCapabilityResponse(info, rc);
                    return;
                case 119:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    rc = new RadioCapability();
                    rc.readFromParcel(_hidl_request);
                    setRadioCapabilityResponse(info, rc);
                    return;
                case 120:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    statusInfo = new LceStatusInfo();
                    statusInfo.readFromParcel(_hidl_request);
                    startLceServiceResponse(info, statusInfo);
                    return;
                case 121:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    statusInfo = new LceStatusInfo();
                    statusInfo.readFromParcel(_hidl_request);
                    stopLceServiceResponse(info, statusInfo);
                    return;
                case 122:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    LceDataInfo lceInfo = new LceDataInfo();
                    lceInfo.readFromParcel(_hidl_request);
                    pullLceDataResponse(info, lceInfo);
                    return;
                case 123:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    ActivityStatsInfo activityInfo = new ActivityStatsInfo();
                    activityInfo.readFromParcel(_hidl_request);
                    getModemActivityInfoResponse(info, activityInfo);
                    return;
                case 124:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setAllowedCarriersResponse(info, _hidl_request.readInt32());
                    return;
                case 125:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    boolean allAllowed = _hidl_request.readBool();
                    CarrierRestrictions carriers = new CarrierRestrictions();
                    carriers.readFromParcel(_hidl_request);
                    getAllowedCarriersResponse(info, allAllowed, carriers);
                    return;
                case 126:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    sendDeviceStateResponse(info);
                    return;
                case 127:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setIndicationFilterResponse(info);
                    return;
                case 128:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    setSimCardPowerResponse(info);
                    return;
                case 129:
                    _hidl_request.enforceInterface(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
                    acknowledgeRequest(_hidl_request.readInt32());
                    return;
                case 130:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    int msgId = _hidl_request.readInt32();
                    RspMsgPayload payload = new RspMsgPayload();
                    payload.readFromParcel(_hidl_request);
                    RspMsg(info, msgId, payload);
                    return;
                case 131:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    RILDeviceVersionResponse deviceVersion = new RILDeviceVersionResponse();
                    deviceVersion.readFromParcel(_hidl_request);
                    getDeviceVersionResponse(info, deviceVersion);
                    return;
                case 132:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    RILDsFlowInfoResponse dsFlowInfo = new RILDsFlowInfoResponse();
                    dsFlowInfo.readFromParcel(_hidl_request);
                    getDsFlowInfoResponse(info, dsFlowInfo);
                    return;
                case 133:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    RILPreferredPLMNSelector preferredplmnselector = new RILPreferredPLMNSelector();
                    preferredplmnselector.readFromParcel(_hidl_request);
                    getPolListResponse(info, preferredplmnselector);
                    return;
                case 134:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    RILRADIOSYSINFO sysInfo = new RILRADIOSYSINFO();
                    sysInfo.readFromParcel(_hidl_request);
                    getSystemInfoExResponse(info, sysInfo);
                    return;
                case 135:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCurrentImsCallsResponse(info, RILImsCall.readVectorFromParcel(_hidl_request));
                    return;
                case 136:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    RILUICCAUTHRESPONSE uiccAuthRst = new RILUICCAUTHRESPONSE();
                    uiccAuthRst.readFromParcel(_hidl_request);
                    uiccAuthResponse(info, uiccAuthRst);
                    return;
                case 137:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getAvailableCsgIdsResponse(info, CsgNetworkInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 138:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    manualSelectionCsgIdResponse(info);
                    return;
                case 139:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    dcResponse = new SetupDataCallResult();
                    dcResponse.readFromParcel(_hidl_request);
                    setupDataCallEmergencyResponse(info, dcResponse);
                    return;
                case 140:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    deactivateDataCallEmergencyResponse(info);
                    return;
                case 141:
                    _hidl_request.enforceInterface(IRadioResponse.kInterfaceName);
                    info = new RadioResponseInfo();
                    info.readFromParcel(_hidl_request);
                    getCellInfoListOtdoaResponse(info, CellInfo.readVectorFromParcel(_hidl_request));
                    return;
                case 256067662:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<String> _hidl_out_descriptors = interfaceChain();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeStringVector(_hidl_out_descriptors);
                    _hidl_reply.send();
                    return;
                case 256131655:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.send();
                    return;
                case 256136003:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    String _hidl_out_descriptor = interfaceDescriptor();
                    _hidl_reply.writeStatus(0);
                    _hidl_reply.writeString(_hidl_out_descriptor);
                    _hidl_reply.send();
                    return;
                case 256398152:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    ArrayList<byte[]> _hidl_out_hashchain = getHashChain();
                    _hidl_reply.writeStatus(0);
                    HwBlob hwBlob = new HwBlob(16);
                    int _hidl_vec_size = _hidl_out_hashchain.size();
                    hwBlob.putInt32(8, _hidl_vec_size);
                    hwBlob.putBool(12, false);
                    hwBlob = new HwBlob(_hidl_vec_size * 32);
                    for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                        long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                        for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                            hwBlob.putInt8(_hidl_array_offset_1, ((byte[]) _hidl_out_hashchain.get(_hidl_index_0))[_hidl_index_1_0]);
                            _hidl_array_offset_1++;
                        }
                    }
                    hwBlob.putBlob(0, hwBlob);
                    _hidl_reply.writeBuffer(hwBlob);
                    _hidl_reply.send();
                    return;
                case 256462420:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    setHALInstrumentation();
                    return;
                case 257049926:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    DebugInfo _hidl_out_info = getDebugInfo();
                    _hidl_reply.writeStatus(0);
                    _hidl_out_info.writeToParcel(_hidl_reply);
                    _hidl_reply.send();
                    return;
                case 257120595:
                    _hidl_request.enforceInterface(IBase.kInterfaceName);
                    notifySyspropsChanged();
                    return;
                default:
                    return;
            }
        }
    }

    public static final class Proxy implements IRadioResponse {
        private IHwBinder mRemote;

        public Proxy(IHwBinder remote) {
            this.mRemote = (IHwBinder) Objects.requireNonNull(remote);
        }

        public IHwBinder asBinder() {
            return this.mRemote;
        }

        public String toString() {
            try {
                return interfaceDescriptor() + "@Proxy";
            } catch (RemoteException e) {
                return "[class or subclass of vendor.huawei.hardware.radio@1.0::IRadioResponse]@Proxy";
            }
        }

        public void getIccCardStatusResponse(RadioResponseInfo info, CardStatus cardStatus) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            cardStatus.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(1, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPinForAppResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(2, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPukForAppResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(3, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPin2ForAppResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(4, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyIccPuk2ForAppResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(5, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void changeIccPinForAppResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(6, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void changeIccPin2ForAppResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(7, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void supplyNetworkDepersonalizationResponse(RadioResponseInfo info, int remainingRetries) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(remainingRetries);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(8, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCurrentCallsResponse(RadioResponseInfo info, ArrayList<Call> calls) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            Call.writeVectorToParcel(_hidl_request, calls);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(9, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void dialResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(10, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getIMSIForAppResponse(RadioResponseInfo info, String imsi) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(imsi);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(11, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hangupConnectionResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(12, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hangupWaitingOrBackgroundResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(13, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void hangupForegroundResumeBackgroundResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(14, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void switchWaitingOrHoldingAndActiveResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(15, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void conferenceResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(16, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void rejectCallResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(17, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getLastCallFailCauseResponse(RadioResponseInfo info, LastCallFailCauseInfo failCauseinfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            failCauseinfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(18, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSignalStrengthResponse(RadioResponseInfo info, SignalStrength sigStrength) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sigStrength.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(19, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVoiceRegistrationStateResponse(RadioResponseInfo info, VoiceRegStateResult voiceRegResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            voiceRegResponse.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(20, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDataRegistrationStateResponse(RadioResponseInfo info, DataRegStateResult dataRegResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            dataRegResponse.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(21, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getOperatorResponse(RadioResponseInfo info, String longName, String shortName, String numeric) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(longName);
            _hidl_request.writeString(shortName);
            _hidl_request.writeString(numeric);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(22, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRadioPowerResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(23, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendDtmfResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(24, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendSmsResponse(RadioResponseInfo info, SendSmsResult sms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(25, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendSMSExpectMoreResponse(RadioResponseInfo info, SendSmsResult sms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(26, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setupDataCallResponse(RadioResponseInfo info, SetupDataCallResult dcResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            dcResponse.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(27, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccIOForAppResponse(RadioResponseInfo info, IccIoResult iccIo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            iccIo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(28, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendUssdResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(29, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void cancelPendingUssdResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(30, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getClirResponse(RadioResponseInfo info, int n, int m) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(n);
            _hidl_request.writeInt32(m);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(31, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setClirResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(32, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCallForwardStatusResponse(RadioResponseInfo info, ArrayList<CallForwardInfo> callForwardInfos) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            CallForwardInfo.writeVectorToParcel(_hidl_request, callForwardInfos);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(33, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCallForwardResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(34, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCallWaitingResponse(RadioResponseInfo info, boolean enable, int serviceClass) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeBool(enable);
            _hidl_request.writeInt32(serviceClass);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(35, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCallWaitingResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(36, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeLastIncomingGsmSmsResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(37, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acceptCallResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(38, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deactivateDataCallResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(39, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getFacilityLockForAppResponse(RadioResponseInfo info, int response) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(response);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(40, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setFacilityLockForAppResponse(RadioResponseInfo info, int retry) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(retry);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(41, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setBarringPasswordResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(42, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNetworkSelectionModeResponse(RadioResponseInfo info, boolean manual) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeBool(manual);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(43, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setNetworkSelectionModeAutomaticResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(44, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setNetworkSelectionModeManualResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(45, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableNetworksResponse(RadioResponseInfo info, ArrayList<OperatorInfo> networkInfos) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            OperatorInfo.writeVectorToParcel(_hidl_request, networkInfos);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(46, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void startDtmfResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(47, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void stopDtmfResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(48, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getBasebandVersionResponse(RadioResponseInfo info, String version) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(version);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(49, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void separateConnectionResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(50, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setMuteResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(51, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getMuteResponse(RadioResponseInfo info, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(52, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getClipResponse(RadioResponseInfo info, int status) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(status);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(53, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDataCallListResponse(RadioResponseInfo info, ArrayList<SetupDataCallResult> dcResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            SetupDataCallResult.writeVectorToParcel(_hidl_request, dcResponse);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(54, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSuppServiceNotificationsResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(55, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void writeSmsToSimResponse(RadioResponseInfo info, int index) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(index);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(56, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deleteSmsOnSimResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(57, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setBandModeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(58, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableBandModesResponse(RadioResponseInfo info, ArrayList<Integer> bandModes) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32Vector(bandModes);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(59, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendEnvelopeResponse(RadioResponseInfo info, String commandResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(commandResponse);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(60, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendTerminalResponseToSimResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(61, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void handleStkCallSetupRequestFromSimResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(62, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void explicitCallTransferResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(63, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPreferredNetworkTypeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(64, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPreferredNetworkTypeResponse(RadioResponseInfo info, int nwType) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(nwType);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(65, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getNeighboringCidsResponse(RadioResponseInfo info, ArrayList<NeighboringCell> cells) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            NeighboringCell.writeVectorToParcel(_hidl_request, cells);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(66, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setLocationUpdatesResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(67, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaSubscriptionSourceResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(68, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaRoamingPreferenceResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(69, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaRoamingPreferenceResponse(RadioResponseInfo info, int type) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(type);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(70, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setTTYModeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(71, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getTTYModeResponse(RadioResponseInfo info, int mode) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(mode);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(72, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setPreferredVoicePrivacyResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(73, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPreferredVoicePrivacyResponse(RadioResponseInfo info, boolean enable) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeBool(enable);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(74, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendCDMAFeatureCodeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(75, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendBurstDtmfResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(76, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendCdmaSmsResponse(RadioResponseInfo info, SendSmsResult sms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(77, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeLastIncomingCdmaSmsResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(78, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getGsmBroadcastConfigResponse(RadioResponseInfo info, ArrayList<GsmBroadcastSmsConfigInfo> configs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            GsmBroadcastSmsConfigInfo.writeVectorToParcel(_hidl_request, configs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(79, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setGsmBroadcastConfigResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(80, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setGsmBroadcastActivationResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(81, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaBroadcastConfigResponse(RadioResponseInfo info, ArrayList<CdmaBroadcastSmsConfigInfo> configs) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            CdmaBroadcastSmsConfigInfo.writeVectorToParcel(_hidl_request, configs);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(82, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaBroadcastConfigResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(83, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCdmaBroadcastActivationResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(84, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCDMASubscriptionResponse(RadioResponseInfo info, String mdn, String hSid, String hNid, String min, String prl) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(mdn);
            _hidl_request.writeString(hSid);
            _hidl_request.writeString(hNid);
            _hidl_request.writeString(min);
            _hidl_request.writeString(prl);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(85, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void writeSmsToRuimResponse(RadioResponseInfo info, int index) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(index);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(86, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deleteSmsOnRuimResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(87, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDeviceIdentityResponse(RadioResponseInfo info, String imei, String imeisv, String esn, String meid) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(imei);
            _hidl_request.writeString(imeisv);
            _hidl_request.writeString(esn);
            _hidl_request.writeString(meid);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(88, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void exitEmergencyCallbackModeResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(89, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSmscAddressResponse(RadioResponseInfo info, String smsc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(smsc);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(90, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSmscAddressResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(91, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void reportSmsMemoryStatusResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(92, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void reportStkServiceIsRunningResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(93, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCdmaSubscriptionSourceResponse(RadioResponseInfo info, int source) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(source);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(94, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestIsimAuthenticationResponse(RadioResponseInfo info, String response) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(response);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(95, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeIncomingGsmSmsWithPduResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(96, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendEnvelopeWithStatusResponse(RadioResponseInfo info, IccIoResult iccIo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            iccIo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(97, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getVoiceRadioTechnologyResponse(RadioResponseInfo info, int rat) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(rat);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(98, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCellInfoListResponse(RadioResponseInfo info, ArrayList<CellInfo> cellInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            CellInfo.writeVectorToParcel(_hidl_request, cellInfo);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(99, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setCellInfoListRateResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(100, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setInitialAttachApnResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(101, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getImsRegistrationStateResponse(RadioResponseInfo info, boolean isRegistered, int ratFamily) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeBool(isRegistered);
            _hidl_request.writeInt32(ratFamily);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(102, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendImsSmsResponse(RadioResponseInfo info, SendSmsResult sms) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sms.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(103, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccTransmitApduBasicChannelResponse(RadioResponseInfo info, IccIoResult result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            result.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(AbstractPhoneBase.EVENT_ECC_NUM, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccOpenLogicalChannelResponse(RadioResponseInfo info, int channelId, ArrayList<Byte> selectResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(channelId);
            _hidl_request.writeInt8Vector(selectResponse);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(AbstractPhoneBase.EVENT_GET_IMSI_DONE, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccCloseLogicalChannelResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(106, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void iccTransmitApduLogicalChannelResponse(RadioResponseInfo info, IccIoResult result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            result.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(107, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvReadItemResponse(RadioResponseInfo info, String result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeString(result);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(AbstractPhoneBase.EVENT_GET_LTE_RELEASE_VERSION_DONE, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvWriteItemResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(109, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvWriteCdmaPrlResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(110, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void nvResetConfigResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(111, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setUiccSubscriptionResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(112, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDataAllowedResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(113, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getHardwareConfigResponse(RadioResponseInfo info, ArrayList<HardwareConfig> config) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HardwareConfig.writeVectorToParcel(_hidl_request, config);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(114, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestIccSimAuthenticationResponse(RadioResponseInfo info, IccIoResult result) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            result.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(115, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setDataProfileResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(116, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void requestShutdownResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(117, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getRadioCapabilityResponse(RadioResponseInfo info, RadioCapability rc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            rc.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(118, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setRadioCapabilityResponse(RadioResponseInfo info, RadioCapability rc) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            rc.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(119, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void startLceServiceResponse(RadioResponseInfo info, LceStatusInfo statusInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            statusInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(120, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void stopLceServiceResponse(RadioResponseInfo info, LceStatusInfo statusInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            statusInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(121, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void pullLceDataResponse(RadioResponseInfo info, LceDataInfo lceInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            lceInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(122, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getModemActivityInfoResponse(RadioResponseInfo info, ActivityStatsInfo activityInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            activityInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(123, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setAllowedCarriersResponse(RadioResponseInfo info, int numAllowed) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(numAllowed);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(124, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAllowedCarriersResponse(RadioResponseInfo info, boolean allAllowed, CarrierRestrictions carriers) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeBool(allAllowed);
            carriers.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(125, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void sendDeviceStateResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(126, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setIndicationFilterResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(127, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setSimCardPowerResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(128, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void acknowledgeRequest(int serial) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(android.hardware.radio.V1_0.IRadioResponse.kInterfaceName);
            _hidl_request.writeInt32(serial);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(129, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void RspMsg(RadioResponseInfo info, int msgId, RspMsgPayload payload) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            _hidl_request.writeInt32(msgId);
            payload.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(130, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDeviceVersionResponse(RadioResponseInfo info, RILDeviceVersionResponse deviceVersion) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            deviceVersion.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(131, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getDsFlowInfoResponse(RadioResponseInfo info, RILDsFlowInfoResponse dsFlowInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            dsFlowInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(132, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getPolListResponse(RadioResponseInfo info, RILPreferredPLMNSelector preferredplmnselector) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            preferredplmnselector.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(133, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getSystemInfoExResponse(RadioResponseInfo info, RILRADIOSYSINFO sysInfo) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            sysInfo.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(134, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCurrentImsCallsResponse(RadioResponseInfo info, ArrayList<RILImsCall> imsCalls) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            RILImsCall.writeVectorToParcel(_hidl_request, imsCalls);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(135, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void uiccAuthResponse(RadioResponseInfo info, RILUICCAUTHRESPONSE uiccAuthRst) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            uiccAuthRst.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(136, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getAvailableCsgIdsResponse(RadioResponseInfo info, ArrayList<CsgNetworkInfo> csgInfos) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            CsgNetworkInfo.writeVectorToParcel(_hidl_request, csgInfos);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(137, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void manualSelectionCsgIdResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(138, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void setupDataCallEmergencyResponse(RadioResponseInfo info, SetupDataCallResult dcResponse) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            dcResponse.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(139, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void deactivateDataCallEmergencyResponse(RadioResponseInfo info) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(140, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public void getCellInfoListOtdoaResponse(RadioResponseInfo info, ArrayList<CellInfo> cellInfoList) throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IRadioResponse.kInterfaceName);
            info.writeToParcel(_hidl_request);
            CellInfo.writeVectorToParcel(_hidl_request, cellInfoList);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(141, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<String> interfaceChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256067662, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<String> _hidl_out_descriptors = _hidl_reply.readStringVector();
                return _hidl_out_descriptors;
            } finally {
                _hidl_reply.release();
            }
        }

        public String interfaceDescriptor() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256136003, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                String _hidl_out_descriptor = _hidl_reply.readString();
                return _hidl_out_descriptor;
            } finally {
                _hidl_reply.release();
            }
        }

        public ArrayList<byte[]> getHashChain() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256398152, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                ArrayList<byte[]> _hidl_out_hashchain = new ArrayList();
                HwBlob _hidl_blob = _hidl_reply.readBuffer(16);
                int _hidl_vec_size = _hidl_blob.getInt32(8);
                HwBlob childBlob = _hidl_reply.readEmbeddedBuffer((long) (_hidl_vec_size * 32), _hidl_blob.handle(), 0, true);
                _hidl_out_hashchain.clear();
                for (int _hidl_index_0 = 0; _hidl_index_0 < _hidl_vec_size; _hidl_index_0++) {
                    Object _hidl_vec_element = new byte[32];
                    long _hidl_array_offset_1 = (long) (_hidl_index_0 * 32);
                    for (int _hidl_index_1_0 = 0; _hidl_index_1_0 < 32; _hidl_index_1_0++) {
                        _hidl_vec_element[_hidl_index_1_0] = childBlob.getInt8(_hidl_array_offset_1);
                        _hidl_array_offset_1++;
                    }
                    _hidl_out_hashchain.add(_hidl_vec_element);
                }
                return _hidl_out_hashchain;
            } finally {
                _hidl_reply.release();
            }
        }

        public void setHALInstrumentation() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256462420, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean linkToDeath(DeathRecipient recipient, long cookie) throws RemoteException {
            return this.mRemote.linkToDeath(recipient, cookie);
        }

        public void ping() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(256921159, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public DebugInfo getDebugInfo() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257049926, _hidl_request, _hidl_reply, 0);
                _hidl_reply.verifySuccess();
                _hidl_request.releaseTemporaryStorage();
                DebugInfo _hidl_out_info = new DebugInfo();
                _hidl_out_info.readFromParcel(_hidl_reply);
                return _hidl_out_info;
            } finally {
                _hidl_reply.release();
            }
        }

        public void notifySyspropsChanged() throws RemoteException {
            HwParcel _hidl_request = new HwParcel();
            _hidl_request.writeInterfaceToken(IBase.kInterfaceName);
            HwParcel _hidl_reply = new HwParcel();
            try {
                this.mRemote.transact(257120595, _hidl_request, _hidl_reply, 1);
                _hidl_request.releaseTemporaryStorage();
            } finally {
                _hidl_reply.release();
            }
        }

        public boolean unlinkToDeath(DeathRecipient recipient) throws RemoteException {
            return this.mRemote.unlinkToDeath(recipient);
        }
    }

    void RspMsg(RadioResponseInfo radioResponseInfo, int i, RspMsgPayload rspMsgPayload) throws RemoteException;

    IHwBinder asBinder();

    void deactivateDataCallEmergencyResponse(RadioResponseInfo radioResponseInfo) throws RemoteException;

    void getAvailableCsgIdsResponse(RadioResponseInfo radioResponseInfo, ArrayList<CsgNetworkInfo> arrayList) throws RemoteException;

    void getCellInfoListOtdoaResponse(RadioResponseInfo radioResponseInfo, ArrayList<CellInfo> arrayList) throws RemoteException;

    void getCurrentImsCallsResponse(RadioResponseInfo radioResponseInfo, ArrayList<RILImsCall> arrayList) throws RemoteException;

    DebugInfo getDebugInfo() throws RemoteException;

    void getDeviceVersionResponse(RadioResponseInfo radioResponseInfo, RILDeviceVersionResponse rILDeviceVersionResponse) throws RemoteException;

    void getDsFlowInfoResponse(RadioResponseInfo radioResponseInfo, RILDsFlowInfoResponse rILDsFlowInfoResponse) throws RemoteException;

    ArrayList<byte[]> getHashChain() throws RemoteException;

    void getPolListResponse(RadioResponseInfo radioResponseInfo, RILPreferredPLMNSelector rILPreferredPLMNSelector) throws RemoteException;

    void getSystemInfoExResponse(RadioResponseInfo radioResponseInfo, RILRADIOSYSINFO rilradiosysinfo) throws RemoteException;

    ArrayList<String> interfaceChain() throws RemoteException;

    String interfaceDescriptor() throws RemoteException;

    boolean linkToDeath(DeathRecipient deathRecipient, long j) throws RemoteException;

    void manualSelectionCsgIdResponse(RadioResponseInfo radioResponseInfo) throws RemoteException;

    void notifySyspropsChanged() throws RemoteException;

    void ping() throws RemoteException;

    void setHALInstrumentation() throws RemoteException;

    void setupDataCallEmergencyResponse(RadioResponseInfo radioResponseInfo, SetupDataCallResult setupDataCallResult) throws RemoteException;

    void uiccAuthResponse(RadioResponseInfo radioResponseInfo, RILUICCAUTHRESPONSE riluiccauthresponse) throws RemoteException;

    boolean unlinkToDeath(DeathRecipient deathRecipient) throws RemoteException;

    static IRadioResponse asInterface(IHwBinder binder) {
        if (binder == null) {
            return null;
        }
        IHwInterface iface = binder.queryLocalInterface(kInterfaceName);
        if (iface != null && (iface instanceof IRadioResponse)) {
            return (IRadioResponse) iface;
        }
        IRadioResponse proxy = new Proxy(binder);
        try {
            for (String descriptor : proxy.interfaceChain()) {
                if (descriptor.equals(kInterfaceName)) {
                    return proxy;
                }
            }
        } catch (RemoteException e) {
        }
        return null;
    }

    static IRadioResponse castFrom(IHwInterface iface) {
        return iface == null ? null : asInterface(iface.asBinder());
    }

    static IRadioResponse getService(String serviceName) throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, serviceName));
    }

    static IRadioResponse getService() throws RemoteException {
        return asInterface(HwBinder.getService(kInterfaceName, "default"));
    }
}
