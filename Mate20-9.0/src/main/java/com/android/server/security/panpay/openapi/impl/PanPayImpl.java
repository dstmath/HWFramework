package com.android.server.security.panpay.openapi.impl;

import android.content.Context;
import android.text.TextUtils;
import android.util.Flog;
import android.util.Log;
import com.android.server.security.panpay.blackbox.Errors;
import com.android.server.security.panpay.blackbox.PanPayBlackBox;
import com.android.server.security.panpay.config.Constant;
import com.android.server.security.panpay.openapi.IPanPayOperator;
import com.android.server.security.tsmagent.constant.ServiceConfig;
import com.android.server.security.tsmagent.logic.card.tsm.CreateOrDeleteOpenSSDTsmOperator;
import com.android.server.security.tsmagent.logic.card.tsm.InitEseTsmOperator;
import com.android.server.security.tsmagent.logic.ese.ESEInfoManager;
import com.android.server.security.tsmagent.openapi.impl.TSMAgentImpl;
import com.android.server.security.tsmagent.server.card.impl.CardServer;
import com.android.server.security.tsmagent.server.wallet.response.IssueItem;
import com.android.server.security.tsmagent.server.wallet.response.QueryIssuesResponse;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.PackageUtil;
import com.android.server.security.ukey.jni.UKeyJNI;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.tsm.TSMOperator;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public class PanPayImpl implements IPanPayOperator {
    private static final String CALLPKG = "callPkg";
    private static final String DIC_NAME = "apk.signature";
    private static final String ERRORCODE = "errorCode";
    public static final int INIT_VALUE = -100;
    private static final String INTERFACE = "interface";
    public static final String OPERATOR_TYPE_CREATE_SSD = "createSSD";
    public static final String OPERATOR_TYPE_DELETE_SSD = "deleteSSD";
    private static final String PKG = "PKG";
    private static final String RESULT = "result";
    public static final int RETURN_FAILED_CONN_UNAVAILABLE = 4;
    public static final int RETURN_FAILED_NO_NETWORK = 3;
    public static final int RETURN_FAILED_UNKNOWN_ERROR = -99;
    public static final int RETURN_INVALID_CALLER_SIGN = 2;
    public static final int RETURN_INVALID_PARAMS = 1;
    public static final int RETURN_NFC_CLOSE = 6;
    public static final int RETURN_PRE_CHECK_OK = 0;
    public static final int RETURN_SUCCESS = 0;
    public static final int RETURN_TSM_ERROR = 5;
    private static final String RUNTIME = "runTime";
    private static final String SDKVERSION = "sdkVersion";
    private static final String SDKVERSION_NUM = "1.0.0";
    private static final int UKEY_UNSUPPORTED = -98;
    private static volatile PanPayImpl sInstance;
    private QueryIssuesResponse cacheIssues = null;
    private final ArrayList<String> mAuthorizedCallers = new ArrayList<>();
    private final Context mContext;

    private class StopWatch {
        private long startTime = System.currentTimeMillis();

        StopWatch() {
        }

        /* access modifiers changed from: package-private */
        public long getRuntime() {
            return System.currentTimeMillis() - this.startTime;
        }
    }

    private PanPayImpl(Context context) {
        this.mContext = context;
    }

    public static PanPayImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PanPayImpl.class) {
                if (sInstance == null) {
                    sInstance = new PanPayImpl(context);
                }
            }
        }
        return sInstance;
    }

    public int checkEligibility(String packageName, String spID) {
        int resultCode = -100;
        StopWatch watch = new StopWatch();
        if (_checkEligibility(packageName, spID)) {
            resultCode = 0;
        }
        reportBD(packageName, "checkEligibility", resultCode, watch.getRuntime(), 553);
        return resultCode;
    }

    private boolean _checkEligibility(String packageName, String spID) {
        boolean checkResult;
        boolean z = false;
        if (!userIsPrimary()) {
            HwLog.d("the user is not primary");
            return false;
        }
        if (spID.startsWith("NFCDK_")) {
            checkResult = Constant.NFCDK_SWITCH_ON;
        } else {
            if (Constant.IS_UKEY_SWITCH_ON && Constant.UKEY_VERSION >= 2) {
                z = true;
            }
            checkResult = z;
        }
        HwLog.d("packageName:" + packageName + ", spID:" + spID + ", checkResult:" + checkResult + " [ IS_UKEY_SWITCH_ON:" + Constant.IS_UKEY_SWITCH_ON + ", UKEY_VERSION:" + Constant.UKEY_VERSION + " ]");
        return checkResult;
    }

    public int checkEligibilityEx(String packageName, String serviceId, String funCallId) {
        return -100;
    }

    public int syncSeInfo(String packageName, String spID, String sign, String timeStamp) {
        int resultCode;
        int preCheck = preCheck(packageName, spID);
        if (preCheck != 0) {
            return preCheck;
        }
        StopWatch watch = new StopWatch();
        synchronized (this) {
            resultCode = new InitEseTsmOperator(this.mContext, spID, sign, timeStamp).excute(getReader(packageName, spID));
            HwLog.d("initEse  result : " + resultCode);
            if (resultCode != 0) {
                PanPayBlackBox.getInstance().appendInfo(packageName, Errors.toInfo(resultCode));
            }
        }
        reportBD(packageName, "syncSeInfo", resultCode, watch.getRuntime(), 552);
        return resultCode;
    }

    public int syncSeInfoEx(String packageName, String serviceId, String funCallId) {
        return -100;
    }

    public int createSSD(String packageName, String spID, String sign, String timeStamp, String ssdAid) {
        int resultCode;
        StopWatch watch = new StopWatch();
        synchronized (this) {
            resultCode = handleOperations(packageName, spID, ssdAid, sign, timeStamp, "createSSD");
            if (resultCode != 0) {
                PanPayBlackBox.getInstance().appendInfo(packageName, Errors.toInfo(resultCode));
            }
        }
        reportBD(packageName, "createSSD", resultCode, watch.getRuntime(), 550);
        return resultCode;
    }

    public int createSSDEx(String packageName, String serviceId, String funCallId, String ssdAid) {
        return -100;
    }

    public int deleteSSD(String packageName, String spID, String sign, String timeStamp, String ssdAid) {
        int resultCode;
        StopWatch watch = new StopWatch();
        synchronized (this) {
            resultCode = handleOperations(packageName, spID, ssdAid, sign, timeStamp, "deleteSSD");
            if (resultCode != 0) {
                PanPayBlackBox.getInstance().appendInfo(packageName, Errors.toInfo(resultCode));
            }
        }
        reportBD(packageName, "deleteSSD", resultCode, watch.getRuntime(), 551);
        return resultCode;
    }

    public int deleteSSDEx(String packageName, String serviceId, String funCallId, String ssdAid) {
        return -100;
    }

    public int installApplet(String packageName, String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int deleteApplet(String packageName, String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int lockApplet(String packageName, String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int unlockApplet(String packageName, String serviceId, String funCallId, String appletAid, String appletVersion) {
        return -100;
    }

    public int activateApplet(String packageName, String serviceId, String funCallId, String appletAid) {
        return -100;
    }

    public int commonExecute(String packageName, String serviceId, String funCallId, String spID) {
        String str = packageName;
        String str2 = spID;
        int preCheck = preCheck(str, str2);
        if (preCheck != 0) {
            return preCheck;
        }
        StopWatch watch = new StopWatch();
        TSMOperator op = TSMOperator.getInstance(this.mContext, getTsmUrl(), getReader(str, str2));
        CommonRequestParams request = new CommonRequestParams(serviceId, funCallId, getCPLC(packageName, serviceId));
        synchronized (this) {
            try {
                int resultCode = op.commonExecute(request);
                if (resultCode != 0) {
                    try {
                        PanPayBlackBox.getInstance().appendInfo(str, Errors.toInfo(resultCode));
                    } catch (Throwable th) {
                        th = th;
                        CommonRequestParams commonRequestParams = request;
                    }
                }
                CommonRequestParams commonRequestParams2 = request;
                reportBD(str, "commonExecute", resultCode, watch.getRuntime(), 559);
                return resultCode;
            } catch (Throwable th2) {
                th = th2;
                CommonRequestParams commonRequestParams3 = request;
                while (true) {
                    try {
                        break;
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
                throw th;
            }
        }
    }

    public String getCPLC(String packageName, String spID) {
        Log.d("PanPayImpl", "in getCPLC");
        if (!_checkEligibility(packageName, spID)) {
            return null;
        }
        StopWatch watch = new StopWatch();
        String cplc = ESEInfoManager.getInstance(this.mContext).queryCplc(getReader(packageName, spID));
        reportBD(packageName, "getCPLC", cplc != null ? 0 : 1, watch.getRuntime(), 556);
        return cplc;
    }

    public String getCIN(String packageName, String spID) {
        if (preCheck(packageName, spID) != 0) {
            return null;
        }
        StopWatch watch = new StopWatch();
        String result = ESEInfoManager.getInstance(this.mContext).queryCIN(getReader(packageName, spID));
        reportBD(packageName, "getCIN", result != null ? 0 : 1, watch.getRuntime(), 557);
        return result;
    }

    public String getIIN(String packageName, String spID) {
        if (preCheck(packageName, spID) != 0) {
            return null;
        }
        StopWatch watch = new StopWatch();
        String result = ESEInfoManager.getInstance(this.mContext).queryIIN(getReader(packageName, spID));
        reportBD(packageName, "getIIN", result != null ? 0 : 1, watch.getRuntime(), 558);
        return result;
    }

    public boolean getSwitch(String packageName, String spID) {
        if (preCheck(packageName, spID) != 0) {
            return false;
        }
        StopWatch watch = new StopWatch();
        boolean resultCode = UKeyJNI.isUKeySwitchDisabled(packageName) == 1;
        reportBD(packageName, "getSwitch", resultCode ? 0 : 1, watch.getRuntime(), 554);
        return resultCode;
    }

    public int setSwitch(String packageName, boolean choice, String spID) {
        int resultCode = -100;
        int preCheck = preCheck(packageName, spID);
        if (preCheck != 0) {
            return preCheck;
        }
        StopWatch watch = new StopWatch();
        String appletId = getAppletId(packageName);
        if (TextUtils.isEmpty(appletId)) {
            HwLog.e("appletId is empty");
        } else {
            resultCode = UKeyJNI.setUKeySwitchDisabled(packageName, appletId, !choice);
        }
        reportBD(packageName, "setSwitch", resultCode, watch.getRuntime(), 555);
        return resultCode;
    }

    public String[] getLastErrorInfo(String packageName, String spID) {
        ArrayList<String> errorInfo = PanPayBlackBox.getInstance().getLastInfo(packageName);
        if (errorInfo == null) {
            errorInfo = new ArrayList<>();
            errorInfo.add("no error info");
        }
        return (String[]) errorInfo.toArray(new String[errorInfo.size()]);
    }

    public int setConfig(String packageName, String spID, HashMap config) {
        if (config.get("tsm_server") != null) {
            ServiceConfig.setTsmUrl((String) config.get("tsm_server"));
        }
        if (config.get("card_server") != null) {
            ServiceConfig.setCardUrl((String) config.get("card_server"));
        }
        if (config.get("wallet_merchant_id") != null) {
            ServiceConfig.setWalletId((String) config.get("wallet_merchant_id"));
        }
        return 0;
    }

    private int handleOperations(String packageName, String spID, String ssdAid, String sign, String timeStamp, String operatorType) {
        int handleOperatorResult;
        HwLog.d("handleOperations: " + operatorType);
        int preCheck = preCheck(packageName, spID);
        if (preCheck != 0) {
            return preCheck;
        }
        synchronized (this) {
            CreateOrDeleteOpenSSDTsmOperator operator = new CreateOrDeleteOpenSSDTsmOperator(this.mContext, ssdAid, spID, sign, timeStamp, operatorType);
            handleOperatorResult = handleOperatorResult(operator.excute(getReader(packageName, spID)));
        }
        return handleOperatorResult;
    }

    private boolean userIsPrimary() {
        return TSMAgentImpl.getInstance(this.mContext).userIsPrimary();
    }

    private boolean checkNetwork() {
        return TSMAgentImpl.getInstance(this.mContext).checkNetwork();
    }

    private boolean checkCallerSignature(String packageName) {
        return TSMAgentImpl.getInstance(this.mContext).checkCallerSignature(packageName);
    }

    private int getReader(String packageName, String spID) {
        return TSMAgentImpl.getInstance(this.mContext).getReader(packageName, spID);
    }

    private int preCheck(String packageName, String spID) {
        if (!_checkEligibility(packageName, spID)) {
            return -98;
        }
        if (!checkNetwork()) {
            HwLog.w("checkNetwork failed ---------- no network");
            return 3;
        } else if (checkCallerSignature(packageName)) {
            return 0;
        } else {
            HwLog.w("checkCallerSignature failed ---------- invalid caller sign");
            return 2;
        }
    }

    private int handleOperatorResult(int result) {
        return TSMAgentImpl.getInstance(this.mContext).handleOperatorResult(result);
    }

    public String getAppletId(String packageName) {
        String appletId = "";
        try {
            if (this.cacheIssues == null) {
                this.cacheIssues = CardServer.getInstance(this.mContext).queryUkeyIssues();
            }
            if (this.cacheIssues != null) {
                appletId = getAppletIdInCached(packageName, this.cacheIssues);
            }
            if (!TextUtils.isEmpty(appletId)) {
                return appletId;
            }
            this.cacheIssues = CardServer.getInstance(this.mContext).queryUkeyIssues();
            if (this.cacheIssues != null) {
                return getAppletIdInCached(packageName, this.cacheIssues);
            }
            return appletId;
        } catch (Exception e) {
            HwLog.e(e.getMessage());
            return null;
        }
    }

    private String getAppletIdInCached(String packageName, QueryIssuesResponse cacheIssues2) {
        ArrayList<IssueItem> issueItems = cacheIssues2.issueItems;
        try {
            int length = issueItems.size();
            for (int i = 0; i < length; i++) {
                IssueItem item = issueItems.get(i);
                String name = item.getApkname();
                String appid = item.getAppletAid();
                if (name.equals(packageName)) {
                    String appletId = appid;
                    if (!TextUtils.isEmpty(appletId)) {
                        return appletId;
                    }
                }
            }
        } catch (Exception e) {
            HwLog.e(e.getMessage());
        }
        return null;
    }

    private String getTsmUrl() {
        int versionCode = PackageUtil.getVersionCode(this.mContext);
        return ServiceConfig.getTsmUrl() + "?version=" + versionCode;
    }

    private void reportBD(String packageName, String interfaceName, int resultCode, long runTime, int type) {
        JSONObject obj1 = new JSONObject();
        try {
            obj1.put(PKG, packageName);
            obj1.put(RESULT, String.valueOf(resultCode));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Flog.bdReport(this.mContext, type, obj1.toString());
        JSONObject obj = new JSONObject();
        try {
            obj.put(CALLPKG, packageName);
            obj.put(SDKVERSION, SDKVERSION_NUM);
            obj.put(INTERFACE, interfaceName);
            if (resultCode == 0) {
                obj.put(RESULT, "1");
            } else {
                obj.put(RESULT, "0");
            }
            obj.put(RUNTIME, runTime);
            if (resultCode == 0) {
                obj.put(ERRORCODE, "null");
            } else {
                obj.put(ERRORCODE, String.valueOf(resultCode));
            }
        } catch (JSONException e2) {
            e2.printStackTrace();
        }
        Flog.bdReport(this.mContext, 560, obj.toString());
    }
}
