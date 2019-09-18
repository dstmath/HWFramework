package com.huawei.wallet.sdk.common.apdu.request;

import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;
import java.util.Map;

public class ServerAccessApplyAPDURequest extends ServerAccessBaseRequest {
    private int apduCount = 0;
    private List<ServerAccessAPDU> apduList = null;
    private String commandId;
    private String currentStep;
    private String enrollmentId = null;
    private String partnerId;
    private String tokeReId = null;
    private String transactionId = null;

    public static class ReqKey {
        public static final String AID = "appletAid";
        public static final String COMMANDID = "commandId";
        public static final String CPLC = "cplc";
        public static final String ENROLLMENTID = "enrollmentId";
        public static final String ISSUERID = "issuerid";
        public static final String TOKENREFID = "tokenRefId";
    }

    public ServerAccessApplyAPDURequest() {
    }

    public ServerAccessApplyAPDURequest(String transactionId2, Map<String, String> param, int apduCount2, List<ServerAccessAPDU> apduList2, String deviceModel, String seChipManuFacturer) {
        setIssuerId(param.get(ReqKey.ISSUERID));
        setAppletAid(param.get(ReqKey.AID));
        setCplc(param.get("cplc"));
        if (!StringUtil.isEmpty(param.get(ReqKey.TOKENREFID), false)) {
            setTokenReId(param.get(ReqKey.TOKENREFID));
        }
        if (!StringUtil.isEmpty(param.get(ReqKey.ENROLLMENTID), false)) {
            setEnrollmentId(param.get(ReqKey.ENROLLMENTID));
        }
        if (!StringUtil.isEmpty(param.get(ReqKey.COMMANDID), false)) {
            setCommandId(param.get(ReqKey.COMMANDID));
        }
        setDeviceModel(deviceModel);
        setSeChipManuFacturer(seChipManuFacturer);
        this.transactionId = transactionId2;
        this.apduCount = apduCount2;
        this.apduList = apduList2;
    }

    public String getCommandId() {
        return this.commandId;
    }

    public void setCommandId(String commandId2) {
        this.commandId = commandId2;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId2) {
        this.transactionId = transactionId2;
    }

    public int getApduCount() {
        return this.apduCount;
    }

    public void setApduCount(int apduCount2) {
        this.apduCount = apduCount2;
    }

    public List<ServerAccessAPDU> getApduList() {
        return this.apduList;
    }

    public void setApduList(List<ServerAccessAPDU> apduList2) {
        this.apduList = apduList2;
    }

    public String getCurrentStep() {
        return this.currentStep;
    }

    public void setCurrentStep(String currentStep2) {
        this.currentStep = currentStep2;
    }

    public String getPartnerId() {
        return this.partnerId;
    }

    public void setPartnerId(String partnerId2) {
        this.partnerId = partnerId2;
    }

    public String getTokenReId() {
        return this.tokeReId;
    }

    public void setTokenReId(String tokeReId2) {
        this.tokeReId = tokeReId2;
    }

    public String getEnrollmentId() {
        return this.enrollmentId;
    }

    public void setEnrollmentId(String enrollmentId2) {
        this.enrollmentId = enrollmentId2;
    }
}
