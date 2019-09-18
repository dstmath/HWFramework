package com.huawei.wallet.sdk.common.http.errorcode;

import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

public class ErrorInfo implements Serializable {
    private static final long serialVersionUID = -217188557650814492L;
    private String codeMsg;
    private String displayDetail;
    private String displayOverview;
    private String originalCode;
    private String srcTranId = null;
    private String suggestion;
    private boolean supportRetry;
    private String tempAccessSec;

    public String getCodeMsg() {
        return this.codeMsg;
    }

    public void setCodeMsg(String codeMsg2) {
        this.codeMsg = codeMsg2;
    }

    public String getDisplayDetail() {
        return this.displayDetail;
    }

    public void setDisplayDetail(String displayDetail2) {
        this.displayDetail = displayDetail2;
    }

    public String getDisplayOverview() {
        return this.displayOverview;
    }

    public void setDisplayOverview(String displayOverview2) {
        this.displayOverview = displayOverview2;
    }

    public String getSuggestion() {
        return this.suggestion;
    }

    public void setSuggestion(String suggestion2) {
        this.suggestion = suggestion2;
    }

    public String getOriginalCode() {
        return this.originalCode;
    }

    public void setOriginalCode(String originalCode2) {
        this.originalCode = originalCode2;
    }

    public boolean getSupportRetry() {
        return this.supportRetry;
    }

    public void setSupportRetry(boolean supportRetry2) {
        this.supportRetry = supportRetry2;
    }

    public String getSrcTransationId() {
        return this.srcTranId;
    }

    public void setSrcTransationId(String srcTranId2) {
        this.srcTranId = srcTranId2;
    }

    public String getTempAccessSec() {
        return this.tempAccessSec;
    }

    public void setTempAccessSec(String tempAccessSec2) {
        this.tempAccessSec = tempAccessSec2;
    }

    public static ErrorInfo build(JSONObject errorInfoObject) throws JSONException {
        ErrorInfo errorInfo = new ErrorInfo();
        if (errorInfoObject.has("codeMsg")) {
            errorInfo.setCodeMsg(errorInfoObject.getString("codeMsg"));
        }
        if (errorInfoObject.has("displayDetail")) {
            errorInfo.setDisplayDetail(errorInfoObject.getString("displayDetail"));
        }
        if (errorInfoObject.has("displayOverview")) {
            errorInfo.setDisplayOverview(errorInfoObject.getString("displayOverview"));
        }
        if (errorInfoObject.has("originalCode")) {
            errorInfo.setOriginalCode(errorInfoObject.getString("originalCode"));
        }
        if (errorInfoObject.has("suggestion")) {
            errorInfo.setSuggestion(errorInfoObject.getString("suggestion"));
        }
        if (errorInfoObject.has("supportRetry")) {
            errorInfo.setSupportRetry(errorInfoObject.getBoolean("supportRetry"));
        }
        if (errorInfoObject.has("tempAccessSec")) {
            errorInfo.setTempAccessSec(errorInfoObject.getString("tempAccessSec"));
        }
        return errorInfo;
    }

    public String toString() {
        return "ErrorInfo{codeMsg='" + this.codeMsg + '\'' + ", displayDetail='" + this.displayDetail + '\'' + ", displayOverview='" + this.displayOverview + '\'' + ", suggestion='" + this.suggestion + '\'' + ", originalCode='" + this.originalCode + '\'' + ", supportRetry=" + this.supportRetry + ", tempAccessSec=" + this.tempAccessSec + '}';
    }
}
