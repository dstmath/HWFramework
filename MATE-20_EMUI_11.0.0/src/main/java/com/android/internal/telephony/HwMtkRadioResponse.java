package com.android.internal.telephony;

import android.hardware.radio.V1_0.Call;
import android.hardware.radio.V1_0.RadioResponseInfo;
import android.os.AsyncResult;
import android.os.Message;
import android.os.RemoteException;
import android.telephony.Rlog;
import com.android.internal.telephony.uicc.HwIccUtils;
import java.util.ArrayList;
import vendor.huawei.hardware.mtkradio.V1_0.CallForwardInfoEx;
import vendor.huawei.hardware.mtkradio.V1_0.OperatorInfoWithAct;
import vendor.huawei.hardware.mtkradio.V1_0.PhbEntryExt;
import vendor.huawei.hardware.mtkradio.V1_0.PhbEntryStructure;
import vendor.huawei.hardware.mtkradio.V1_0.PhbMemStorageResponse;
import vendor.huawei.hardware.mtkradio.V1_0.RspMsgPayload;
import vendor.huawei.hardware.mtkradio.V1_0.SignalStrengthWithWcdmaEcio;
import vendor.huawei.hardware.mtkradio.V1_0.SmsMemStatus;
import vendor.huawei.hardware.mtkradio.V1_0.SmsParams;
import vendor.huawei.hardware.mtkradio.V1_0.VsimEvent;
import vendor.huawei.hardware.mtkradio.V1_5.IMtkRadioExResponse;

public class HwMtkRadioResponse extends IMtkRadioExResponse.Stub {
    private static final String LOG_TAG = "HwMtkRadioResponse";
    private HwMtkRIL mMtkRil;

    public HwMtkRadioResponse(RIL ril) {
        this.mMtkRil = (HwMtkRIL) ril;
    }

    static void sendMessageResponse(Message msg, Object ret) {
        if (msg != null) {
            AsyncResult.forMessage(msg, ret, (Throwable) null);
            msg.sendToTarget();
        }
    }

    public void setClipResponse(RadioResponseInfo responseInfo) {
    }

    public void getColpResponse(RadioResponseInfo responseInfo, int n, int m) {
    }

    public void getColrResponse(RadioResponseInfo responseInfo, int status) {
    }

    public void sendCnapResponse(RadioResponseInfo responseInfo, int n, int m) {
    }

    public void setColpResponse(RadioResponseInfo responseInfo) {
    }

    public void setColrResponse(RadioResponseInfo responseInfo) {
    }

    public void queryCallForwardInTimeSlotStatusResponse(RadioResponseInfo responseInfo, ArrayList<CallForwardInfoEx> arrayList) {
    }

    public void setCallForwardInTimeSlotResponse(RadioResponseInfo responseInfo) {
    }

    public void runGbaAuthenticationResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void setPdnReuseResponse(RadioResponseInfo responseInfo) {
    }

    public void setOverrideApnResponse(RadioResponseInfo responseInfo) {
    }

    public void setPdnNameReuseResponse(RadioResponseInfo responseInfo) {
    }

    public void setTrmResponse(RadioResponseInfo responseInfo) {
    }

    public void getATRResponse(RadioResponseInfo info, String response) {
    }

    public void getIccidResponse(RadioResponseInfo info, String response) {
    }

    public void setSimPowerResponse(RadioResponseInfo info) {
    }

    public void setNetworkSelectionModeManualWithActResponse(RadioResponseInfo responseInfo) {
    }

    public void getAvailableNetworksWithActResponse(RadioResponseInfo responseInfo, ArrayList<OperatorInfoWithAct> arrayList) {
    }

    public void getSignalStrengthWithWcdmaEcioResponse(RadioResponseInfo responseInfo, SignalStrengthWithWcdmaEcio signalStrength) {
    }

    public void cancelAvailableNetworksResponse(RadioResponseInfo responseInfo) {
    }

    public void setModemPowerResponse(RadioResponseInfo responseInfo) {
    }

    public void getSmsParametersResponse(RadioResponseInfo responseInfo, SmsParams params) {
    }

    public void setSmsParametersResponse(RadioResponseInfo responseInfo) {
    }

    public void setEtwsResponse(RadioResponseInfo responseInfo) {
    }

    public void removeCbMsgResponse(RadioResponseInfo responseInfo) {
    }

    public void getSmsMemStatusResponse(RadioResponseInfo responseInfo, SmsMemStatus params) {
    }

    public void setGsmBroadcastLangsResponse(RadioResponseInfo responseInfo) {
    }

    public void getGsmBroadcastLangsResponse(RadioResponseInfo responseInfo, String langs) {
    }

    public void getGsmBroadcastActivationRsp(RadioResponseInfo responseInfo, int activation) {
    }

    public void setSmsFwkReadyRsp(RadioResponseInfo responseInfo) {
    }

    public void sendEmbmsAtCommandResponse(RadioResponseInfo responseInfo, String result) {
    }

    public void hangupAllResponse(RadioResponseInfo responseInfo) throws RemoteException {
    }

    public void setCallIndicationResponse(RadioResponseInfo responseInfo) {
    }

    public void emergencyDialResponse(RadioResponseInfo responseInfo) {
    }

    public void setEccServiceCategoryResponse(RadioResponseInfo responseInfo) {
    }

    public void setEccListResponse(RadioResponseInfo responseInfo) {
    }

    public void setVoicePreferStatusResponse(RadioResponseInfo responseInfo) {
    }

    public void setEccNumResponse(RadioResponseInfo responseInfo) {
    }

    public void getEccNumResponse(RadioResponseInfo responseInfo) {
    }

    public void currentStatusResponse(RadioResponseInfo responseInfo) {
    }

    public void eccPreferredRatResponse(RadioResponseInfo responseInfo) {
    }

    public void setApcModeResponse(RadioResponseInfo responseInfo) {
    }

    public void getApcInfoResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void triggerModeSwitchByEccResponse(RadioResponseInfo responseInfo) {
    }

    public void getSmsRuimMemoryStatusResponse(RadioResponseInfo responseInfo, SmsMemStatus memStatus) {
    }

    public void setFdModeResponse(RadioResponseInfo responseInfo) {
    }

    public void setResumeRegistrationResponse(RadioResponseInfo responseInfo) {
    }

    public void storeModemTypeResponse(RadioResponseInfo responseInfo) {
    }

    public void reloadModemTypeResponse(RadioResponseInfo responseInfo) {
    }

    public void handleStkCallSetupRequestFromSimWithResCodeResponse(RadioResponseInfo responseInfo) {
    }

    public void queryPhbStorageInfoResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void writePhbEntryResponse(RadioResponseInfo responseInfo) {
    }

    public void readPhbEntryResponse(RadioResponseInfo responseInfo, ArrayList<PhbEntryStructure> arrayList) {
    }

    public void getCurrentCallsResponse(RadioResponseInfo responseInfo, ArrayList<Call> arrayList) {
    }

    public void getCurrentCallsResponse_1_2(RadioResponseInfo responseInfo, ArrayList<android.hardware.radio.V1_2.Call> arrayList) {
    }

    public void queryUPBCapabilityResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void editUPBEntryResponse(RadioResponseInfo responseInfo) {
    }

    public void deleteUPBEntryResponse(RadioResponseInfo responseInfo) {
    }

    public void readUPBGasListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void readUPBGrpEntryResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void writeUPBGrpEntryResponse(RadioResponseInfo responseInfo) {
    }

    public void getPhoneBookStringsLengthResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void getPhoneBookMemStorageResponse(RadioResponseInfo responseInfo, PhbMemStorageResponse phbMemStorage) {
    }

    public void setPhoneBookMemStorageResponse(RadioResponseInfo responseInfo) {
    }

    public void readPhoneBookEntryExtResponse(RadioResponseInfo responseInfo, ArrayList<PhbEntryExt> arrayList) {
    }

    public void writePhoneBookEntryExtResponse(RadioResponseInfo responseInfo) {
    }

    public void queryUPBAvailableResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void readUPBEmailEntryResponse(RadioResponseInfo responseInfo, String email) {
    }

    public void readUPBSneEntryResponse(RadioResponseInfo responseInfo, String sne) {
    }

    public void readUPBAnrEntryResponse(RadioResponseInfo responseInfo, ArrayList<PhbEntryStructure> arrayList) {
    }

    public void readUPBAasListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void setPhonebookReadyResponse(RadioResponseInfo responseInfo) {
    }

    public void resetRadioResponse(RadioResponseInfo responseInfo) {
    }

    public void restartRILDResponse(RadioResponseInfo responseInfo) {
    }

    public void getFemtocellListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void abortFemtocellListResponse(RadioResponseInfo responseInfo) {
    }

    public void selectFemtocellResponse(RadioResponseInfo responseInfo) {
    }

    public void queryFemtoCellSystemSelectionModeResponse(RadioResponseInfo responseInfo, int mode) {
    }

    public void setFemtoCellSystemSelectionModeResponse(RadioResponseInfo responseInfo) {
    }

    public void syncDataSettingsToMdResponse(RadioResponseInfo responseInfo) {
    }

    public void resetMdDataRetryCountResponse(RadioResponseInfo responseInfo) {
    }

    public void setRemoveRestrictEutranModeResponse(RadioResponseInfo responseInfo) {
    }

    public void setLteAccessStratumReportResponse(RadioResponseInfo responseInfo) {
    }

    public void setLteUplinkDataTransferResponse(RadioResponseInfo responseInfo) {
    }

    public void queryNetworkLockResponse(RadioResponseInfo info, int catagory, int state, int retry_cnt, int autolock_cnt, int num_set, int total_set, int key_state) {
    }

    public void setNetworkLockResponse(RadioResponseInfo info) {
    }

    public void supplyDepersonalizationResponse(RadioResponseInfo responseInfo, int retriesRemaining) {
    }

    public void setRxTestConfigResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void getRxTestResultResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void getPOLCapabilityResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void getCurrentPOLListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void setPOLEntryResponse(RadioResponseInfo responseInfo) {
    }

    public void setRoamingEnableResponse(RadioResponseInfo responseInfo) {
    }

    public void getRoamingEnableResponse(RadioResponseInfo responseInfo, ArrayList<Integer> arrayList) {
    }

    public void setLteReleaseVersionResponse(RadioResponseInfo responseInfo) {
    }

    public void getLteReleaseVersionResponse(RadioResponseInfo responseInfo, int mode) {
    }

    public void vsimNotificationResponse(RadioResponseInfo info, VsimEvent event) {
    }

    public void vsimOperationResponse(RadioResponseInfo info) {
    }

    public void setE911StateResponse(RadioResponseInfo responseInfo) {
    }

    public void setServiceStateToModemResponse(RadioResponseInfo responseInfo) {
    }

    public void sendRequestRawResponse(RadioResponseInfo responseInfo, ArrayList<Byte> data) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            byte[] ret = null;
            if (rri.error == 0) {
                ret = RIL.arrayListToPrimitiveArray(data);
                sendMessageResponse(rr.mResult, ret);
            }
            this.mMtkRil.processResponseDone(rr, rri, ret);
        }
    }

    public void getCardTrayInfoResponse(RadioResponseInfo responseInfo, ArrayList<Byte> cardTrayInfo) {
        if (cardTrayInfo == null) {
            logi("getCardTrayInfoResponse: cardTrayInfo is null.");
            processRadioResponse(responseInfo, null);
            return;
        }
        processRadioResponse(responseInfo, RIL.arrayListToPrimitiveArray(cardTrayInfo));
    }

    public void sendRequestStringsResponse(RadioResponseInfo responseInfo, ArrayList<String> data) {
        RadioResponse.responseStringArrayList(this.mMtkRil, switchResponseInfo(responseInfo), data);
    }

    public void dataConnectionAttachResponse(RadioResponseInfo responseInfo) {
    }

    public void dataConnectionDetachResponse(RadioResponseInfo responseInfo) {
    }

    public void resetAllConnectionsResponse(RadioResponseInfo responseInfo) {
    }

    public void reportAirplaneModeResponse(RadioResponseInfo responseInfo) {
    }

    public void reportSimModeResponse(RadioResponseInfo responseInfo) {
    }

    public void setSilentRebootResponse(RadioResponseInfo responseInfo) {
    }

    public void setPropImsHandoverResponse(RadioResponseInfo responseInfo) {
    }

    public void setOperatorConfigurationResponse(RadioResponseInfo responseInfo) {
    }

    public void hangupWithReasonResponse(RadioResponseInfo responseInfo) {
    }

    public void setVendorSettingResponse(RadioResponseInfo responseInfo) {
    }

    public void getPlmnNameFromSE13TableResponse(RadioResponseInfo responseInfo, String name) {
    }

    public void enableCAPlusBandWidthFilterResponse(RadioResponseInfo responseInfo) {
    }

    public void setGwsdModeResponse(RadioResponseInfo responseInfo) {
    }

    public void setCallValidTimerResponse(RadioResponseInfo responseInfo) {
    }

    public void setIgnoreSameNumberIntervalResponse(RadioResponseInfo responseInfo) {
    }

    public void setKeepAliveByPDCPCtrlPDUResponse(RadioResponseInfo responseInfo) {
    }

    public void setKeepAliveByIpDataResponse(RadioResponseInfo responseInfo) {
    }

    public void enableDsdaIndicationResponse(RadioResponseInfo responseInfo) {
    }

    public void getDsdaStatusResponse(RadioResponseInfo responseInfo, int mode) {
    }

    public void registerCellQltyReportResponse(RadioResponseInfo responseInfo) {
    }

    public void getSuggestedPlmnListResponse(RadioResponseInfo responseInfo, ArrayList<String> arrayList) {
    }

    public void setEccModeResponse(RadioResponseInfo responseInfo) {
    }

    public void activateUiccCardRsp(RadioResponseInfo responseInfo, int simPowerOnOffResponse) {
    }

    public void deactivateUiccCardRsp(RadioResponseInfo responseInfo, int simPowerOnOffResponse) {
    }

    public void getCurrentUiccCardProvisioningStatusRsp(RadioResponseInfo responseInfo, int simPowerOnOffStatus) {
    }

    public void modifyModemTypeResponse(RadioResponseInfo responseInfo, int applyType) {
    }

    public void cfgA2offsetResponse(RadioResponseInfo responseInfo) {
    }

    public void cfgB1offsetResponse(RadioResponseInfo responseInfo) {
    }

    public void enableSCGfailureResponse(RadioResponseInfo responseInfo) {
    }

    public void disableNRResponse(RadioResponseInfo responseInfo) {
    }

    public void setTxPowerResponse(RadioResponseInfo responseInfo) {
    }

    public void setSearchStoredFreqInfoResponse(RadioResponseInfo responseInfo) {
    }

    public void setSearchRatResponse(RadioResponseInfo responseInfo) {
    }

    public void setBgsrchDeltaSleepTimerResponse(RadioResponseInfo responseInfo) {
    }

    public void RspMsg(RadioResponseInfo responseInfo, int msgId, RspMsgPayload payload) {
        Object ret = null;
        if (payload == null) {
            logi("got null payload msgId = " + msgId);
            return;
        }
        logi("RspMsg msgId = " + msgId);
        if (msgId != 611) {
            logi("got invalid msgId = " + msgId);
        } else {
            ret = responseICCID(payload.strData);
        }
        if (1 != 0) {
            if (responseInfo.error != 0) {
                ret = null;
            }
            processRadioResponse(responseInfo, ret);
        }
    }

    private Object responseICCID(String response) {
        return HwIccUtils.hexStringToBcd(response);
    }

    public void setCsconEnabledResponse(RadioResponseInfo responseInfo) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("setCsconEnabledResponse, rr is null");
    }

    public void getCsconEnabledResponse(RadioResponseInfo responseInfo, int isEnabled) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, Integer.valueOf(isEnabled));
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("getCsconEnabledResponse, rr is null");
    }

    public void getSuppServPropertyResponse(RadioResponseInfo responseInfo, String value) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("getSuppServPropertyResponse, rr is null");
    }

    public void setSuppServPropertyResponse(RadioResponseInfo responseInfo) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("setSuppServPropertyResponse, rr is null");
    }

    public void processRadioResponse(RadioResponseInfo responseInfo, Object ret) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, ret);
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("processRadioResponse, rr is null");
    }

    public void supplyDeviceNetworkDepersonalizationResponse(RadioResponseInfo responseInfo, int remainingRetries) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, Integer.valueOf(remainingRetries));
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("processRadioResponse, rr is null");
    }

    public void getCapOfRecPseBaseStationResponse(RadioResponseInfo responseInfo) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("processRadioResponse, rr is null");
    }

    private RadioResponseInfo switchResponseInfo(RadioResponseInfo mtkRadioresponseInfo) {
        RadioResponseInfo rri = new RadioResponseInfo();
        rri.serial = mtkRadioresponseInfo.serial;
        rri.error = mtkRadioresponseInfo.error;
        rri.type = mtkRadioresponseInfo.type;
        return rri;
    }

    public void setTxPowerStatusResponse(RadioResponseInfo responseInfo) {
        RadioResponseInfo rri = switchResponseInfo(responseInfo);
        RILRequest rr = this.mMtkRil.processResponse(rri);
        if (rr != null) {
            if (rri.error == 0) {
                sendMessageResponse(rr.mResult, null);
            }
            this.mMtkRil.processResponseDone(rr, rri, null);
            return;
        }
        logi("setTXPowerEnableResponse, rr is null");
    }

    private void logi(String msg) {
        Rlog.i("HwMtkRadioResponse[" + this.mMtkRil.mPhoneId + "]", msg);
    }

    public void deactivateNrScgCommunicationResponse(RadioResponseInfo responseInfo) {
    }

    public void getDeactivateNrScgCommunicationResponse(RadioResponseInfo responseInfo, int deactivate, int allowSCGAdd) {
    }

    public void setMaxUlSpeedResponse(RadioResponseInfo responseInfo) {
    }

    public void sendSarIndicatorResponse(RadioResponseInfo responseInfo) {
    }
}
