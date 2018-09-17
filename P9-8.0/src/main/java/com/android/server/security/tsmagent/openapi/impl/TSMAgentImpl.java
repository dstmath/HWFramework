package com.android.server.security.tsmagent.openapi.impl;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.security.tsmagent.logic.card.tsm.CreateOrDeleteOpenSSDTsmOperator;
import com.android.server.security.tsmagent.logic.card.tsm.InitEseTsmOperator;
import com.android.server.security.tsmagent.logic.ese.ESEInfoManager;
import com.android.server.security.tsmagent.openapi.ITSMOperator;
import com.android.server.security.tsmagent.server.card.impl.CardServer;
import com.android.server.security.tsmagent.server.wallet.request.QueryDicsRequset;
import com.android.server.security.tsmagent.server.wallet.response.DicItem;
import com.android.server.security.tsmagent.server.wallet.response.QueryDicsResponse;
import com.android.server.security.tsmagent.utils.HwLog;
import com.android.server.security.tsmagent.utils.NetworkUtil;
import com.android.server.security.tsmagent.utils.PackageSignatureUtil;
import com.android.server.security.tsmagent.utils.StringUtil;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

public class TSMAgentImpl implements ITSMOperator {
    private static final String DIC_NAME = "apk.signature";
    private static volatile TSMAgentImpl sInstance;
    private final ArrayList<String> mAuthorizedCallers = new ArrayList();
    private final Context mContext;

    private TSMAgentImpl(Context context) {
        this.mContext = context;
    }

    public static TSMAgentImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TSMAgentImpl.class) {
                if (sInstance == null) {
                    sInstance = new TSMAgentImpl(context);
                }
            }
        }
        return sInstance;
    }

    public int createSSD(String packageName, String spID, String ssdAid, String sign, String timeStamp) {
        return handleOperations(packageName, spID, ssdAid, sign, timeStamp, ITSMOperator.OPERATOR_TYPE_CREATE_SSD);
    }

    public int deleteSSD(String packageName, String spID, String ssdAid, String sign, String timeStamp) {
        return handleOperations(packageName, spID, ssdAid, sign, timeStamp, ITSMOperator.OPERATOR_TYPE_DELETE_SSD);
    }

    public String getCplc(String packageName) {
        if (sechulePreCheck(packageName) != 0) {
            return null;
        }
        String queryCplc;
        synchronized (this) {
            queryCplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        }
        return queryCplc;
    }

    public int initEse(String packageName, String spID, String sign, String timeStamp) {
        int preCheckResult = sechulePreCheck(packageName);
        if (preCheckResult != 0) {
            return preCheckResult;
        }
        int resultCode;
        synchronized (this) {
            resultCode = new InitEseTsmOperator(this.mContext, spID, sign, timeStamp).excute();
            HwLog.d("initEse  result : " + resultCode);
        }
        return resultCode;
    }

    private int handleOperations(String packageName, String spID, String ssdAid, String sign, String timeStamp, String operatorType) {
        HwLog.d("handleOperations: " + operatorType);
        if (checkParams(packageName, spID, ssdAid, sign, timeStamp)) {
            int preCheckResult = sechulePreCheck(packageName);
            if (preCheckResult != 0) {
                HwLog.w("sechulePreCheck failed, error code: " + preCheckResult);
                return preCheckResult;
            }
            int handleOperatorResult;
            synchronized (this) {
                handleOperatorResult = handleOperatorResult(new CreateOrDeleteOpenSSDTsmOperator(this.mContext, ssdAid, spID, sign, timeStamp, operatorType).excute());
            }
            return handleOperatorResult;
        }
        HwLog.w("checkParams failed");
        return 1;
    }

    private boolean checkParams(String packageName, String spID, String ssdAid, String sign, String timeStamp) {
        if (StringUtil.isTrimedEmpty(packageName) || StringUtil.isTrimedEmpty(spID) || StringUtil.isTrimedEmpty(ssdAid) || StringUtil.isTrimedEmpty(sign) || StringUtil.isTrimedEmpty(timeStamp)) {
            return false;
        }
        return true;
    }

    public int sechulePreCheck(String pkgName) {
        if (!checkSupportFeature()) {
            HwLog.w("checkSupportFeature failed");
            return -1;
        } else if (!checkNetwork()) {
            HwLog.w("checkNetwork no network");
            return 3;
        } else if (checkCallerSignature(pkgName)) {
            return 0;
        } else {
            return 2;
        }
    }

    private boolean checkSupportFeature() {
        HwLog.d("checkSupportFeature");
        return "CN".equalsIgnoreCase(SystemProperties.get("ro.product.locale.region", ""));
    }

    public boolean checkNetwork() {
        HwLog.d("checkNetwork");
        return NetworkUtil.isNetworkConnected(this.mContext);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x0090 A:{Splitter: B:19:0x0069, ExcHandler: java.security.AccessControlException (e java.security.AccessControlException)} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0090 A:{Splitter: B:19:0x0069, ExcHandler: java.security.AccessControlException (e java.security.AccessControlException)} */
    /* JADX WARNING: Missing block: B:29:0x0091, code:
            com.android.server.security.tsmagent.utils.HwLog.w("HwNFCOpenApiImpl checkCaller failed. pkg : " + r14);
     */
    /* JADX WARNING: Missing block: B:30:0x00a9, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkCallerSignature(String callerPkgName) {
        HwLog.d("checkCallerSignature");
        if (this.mAuthorizedCallers.contains(callerPkgName)) {
            HwLog.d("operateSSD checkCaller success end");
            return true;
        }
        QueryDicsRequset request = new QueryDicsRequset();
        request.itemName = callerPkgName;
        request.dicName = DIC_NAME;
        QueryDicsResponse response = new CardServer(this.mContext).queryDics(request);
        if (response == null || response.returnCode != 0) {
            HwLog.w("operateSSD checkCaller failed. query server failed. retCode = " + (response == null ? "response is null" : Integer.valueOf(response.returnCode)));
            return false;
        } else if (response.dicItems.size() <= 0) {
            HwLog.w("operateSSD checkCaller failed. dicItems size <= 0.");
            return false;
        } else {
            try {
                List<String> signs = PackageSignatureUtil.getInstalledAppHashList(this.mContext, callerPkgName);
                if (signs != null && signs.size() > 0) {
                    StringBuilder sBuilder = new StringBuilder();
                    for (String sign : signs) {
                        sBuilder.append(sign);
                    }
                    String signStr = sBuilder.toString();
                    int size = response.dicItems.size();
                    for (int i = 0; i < size; i++) {
                        DicItem item = (DicItem) response.dicItems.get(i);
                        if (callerPkgName.equals(item.getParent()) && signStr.equals(item.getValue())) {
                            this.mAuthorizedCallers.add(callerPkgName);
                            HwLog.d("operateSSD checkCaller success end");
                            return true;
                        }
                    }
                }
                HwLog.w("checkCaller failed. The caller pkg is not allowed. pkg = " + callerPkgName);
                return false;
            } catch (AccessControlException e) {
            }
        }
    }

    public int handleOperatorResult(int result) {
        int returnCode;
        switch (result) {
            case -99:
                returnCode = -99;
                break;
            case -2:
                returnCode = 4;
                break;
            case -1:
                returnCode = 3;
                break;
            case 0:
                returnCode = 0;
                break;
            default:
                HwLog.w("HwNFCOpenApiImpl operate ssd failed. tsm error. result : " + result);
                returnCode = 5;
                break;
        }
        HwLog.d("handleOperatorResult : " + result);
        return returnCode;
    }
}
