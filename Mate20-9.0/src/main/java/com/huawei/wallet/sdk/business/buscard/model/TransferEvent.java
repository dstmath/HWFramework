package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.unionpay.tsmservice.data.Constant;
import org.json.JSONException;
import org.json.JSONObject;

public class TransferEvent extends TransferEventBase {
    public static final String EVENT_AGREE_TRANSFER_IN = "2";
    public static final String EVENT_APPLY_TRANSFER_OUT = "1";
    public static final String EVENT_CANCEL_TRANSFER = "5";
    public static final String EVENT_INVALID = "6";
    public static final String EVENT_TRANSFER_IN = "4";
    public static final String EVENT_TRANSFER_OUT = "3";
    public static final String NO_EVENT_ID = "no_event_id";
    public static final int THIS_IS_NEW_DEVICE = 2;
    public static final int THIS_IS_OLD_DEVICE = 1;
    public static final int TRANSFER_EVENT_RETURN_FAILED = -2;
    public static final int TRANSFER_EVENT_RETURN_SUCCESS_HAS_EVENT = 0;
    public static final int TRANSFER_EVENT_RETURN_SUCCESS_NO_EVENT = -1;
    private String aid;
    private String appMoveCode;
    private String balance;
    private String cardName;
    private String conflictedCardName;
    private String cycle;
    private int deviceBelongTo;
    private int expireDaysInLeft;
    private String fee;
    private String payTimePoint;
    private int returnCode;

    public TransferEvent() {
    }

    public int getExpireDaysInLeft() {
        return this.expireDaysInLeft;
    }

    public void setExpireDaysInLeft(int expireDaysInLeft2) {
        this.expireDaysInLeft = expireDaysInLeft2;
    }

    public TransferEvent(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("eventid")) {
            setEventId(jsonObject.getString("eventid"));
        }
        if (jsonObject.has("userid")) {
            setUserId(jsonObject.getString("userid"));
        }
        if (jsonObject.has(ServerAccessApplyAPDURequest.ReqKey.ISSUERID)) {
            setIssuerId(jsonObject.getString(ServerAccessApplyAPDURequest.ReqKey.ISSUERID));
        }
        if (jsonObject.has(Constants.FIELD_APPLET_CONFIG_STATUS)) {
            setStatus(jsonObject.getString(Constants.FIELD_APPLET_CONFIG_STATUS));
        }
        if (jsonObject.has(Constant.KEY_DEVICE_TYPE)) {
            setDeviceType(jsonObject.getString(Constant.KEY_DEVICE_TYPE));
        }
        if (jsonObject.has("oldCplc")) {
            setOldCplc(jsonObject.getString("oldCplc"));
        }
        if (jsonObject.has("oldTerminal")) {
            setOldTerminal(jsonObject.getString("oldTerminal"));
        }
        if (jsonObject.has("newCplc")) {
            setNewCplc(jsonObject.getString("newCplc"));
        }
        if (jsonObject.has("newTerminal")) {
            setNewTerminal(jsonObject.getString("newTerminal"));
        }
        if (jsonObject.has("oldCardNumber")) {
            setOldCardNumber(jsonObject.getString("oldCardNumber"));
        }
        if (jsonObject.has("newCardNumber")) {
            setNewCardNumber(jsonObject.getString("newCardNumber"));
        }
        if (jsonObject.has("appMoveCode")) {
            this.appMoveCode = jsonObject.getString("appMoveCode");
        }
        if (jsonObject.has("expireDaysInLeft")) {
            setExpireDaysInLeft(jsonObject.getInt("expireDaysInLeft"));
        }
        parseAndSetValue(jsonObject);
        parseReserved(jsonObject);
    }

    private void parseAndSetValue(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("cycle")) {
            setCycle(jsonObject.getString("cycle"));
        }
        if (jsonObject.has("fee")) {
            setFee(jsonObject.getString("fee"));
        }
        if (jsonObject.has("payTimePoint")) {
            setPayTimePoint(jsonObject.getString("payTimePoint"));
        }
        if (jsonObject.has(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE)) {
            setBalance(jsonObject.getString(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE));
        }
    }

    private void parseReserved(JSONObject jsonObject) throws JSONException {
        if (jsonObject.has("reserved")) {
            JSONObject reservedJsonObj = new JSONObject(jsonObject.getString("reserved"));
            if (reservedJsonObj.has("cityCode")) {
                setCityCode(reservedJsonObj.getString("cityCode"));
            }
        }
    }

    public String getAppMoveCode() {
        return this.appMoveCode;
    }

    public void setAppMoveCode(String appMoveCode2) {
        this.appMoveCode = appMoveCode2;
    }

    public String getCardName() {
        return this.cardName;
    }

    public void setCardName(String cardName2) {
        this.cardName = cardName2;
    }

    public int getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(int returnCode2) {
        this.returnCode = returnCode2;
    }

    public int getDeviceBelongTo() {
        return this.deviceBelongTo;
    }

    public void setDeviceBelongTo(int deviceBelongTo2) {
        this.deviceBelongTo = deviceBelongTo2;
    }

    public void setAid(String aid2) {
        this.aid = aid2;
    }

    public String getAid() {
        return this.aid;
    }

    public String getCycle() {
        return this.cycle;
    }

    public void setCycle(String cycle2) {
        this.cycle = cycle2;
    }

    public String getFee() {
        return this.fee;
    }

    public void setFee(String fee2) {
        this.fee = fee2;
    }

    public String getPayTimePoint() {
        return this.payTimePoint;
    }

    public void setPayTimePoint(String payTimePoint2) {
        this.payTimePoint = payTimePoint2;
    }

    public void setBalance(String balance2) {
        this.balance = balance2;
    }

    public String getBalance() {
        return this.balance;
    }

    public String getConflictedCardName() {
        return this.conflictedCardName;
    }

    public void setConflictedCardName(String conflictedCardName2) {
        this.conflictedCardName = conflictedCardName2;
    }

    public String toString() {
        return "TransferEvent{eventId='" + getEventId() + '\'' + ", userId='" + getUserId() + '\'' + ", issuerId='" + getIssuerId() + '\'' + ", status='" + getStatus() + '\'' + ", deviceType='" + getDeviceType() + '\'' + ", oldCplc='" + getOldCplc() + '\'' + ", oldTerminal='" + getOldTerminal() + '\'' + ", newCplc='" + getNewCplc() + '\'' + ", newTerminal='" + getNewTerminal() + '\'' + ", oldCardNumber='" + getOldCardNumber() + '\'' + ", newCardNumber='" + getNewCardNumber() + '\'' + ", appMoveCode='" + getAppMoveCode() + '\'' + ", cityCode='" + getCityCode() + '\'' + '}';
    }
}
