package com.android.server.security.tsmagent.openapi.impl;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Flog;
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
import com.android.server.wifipro.WifiProCommonUtils;
import java.security.AccessControlException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class TSMAgentImpl implements ITSMOperator {
    private static final String DIC_NAME = "apk.signature";
    private static volatile TSMAgentImpl sInstance;
    private final ArrayList<String> mAuthorizedCallers = new ArrayList<>();
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
        int retVal = handleOperations(packageName, spID, ssdAid, sign, timeStamp, "createSSD");
        JSONObject obj = new JSONObject();
        try {
            obj.put("PKG", packageName);
            obj.put("result", String.valueOf(retVal));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Flog.bdReport(this.mContext, 550, obj.toString());
        return retVal;
    }

    public int deleteSSD(String packageName, String spID, String ssdAid, String sign, String timeStamp) {
        int retVal = handleOperations(packageName, spID, ssdAid, sign, timeStamp, "deleteSSD");
        JSONObject obj = new JSONObject();
        try {
            obj.put("PKG", packageName);
            obj.put("result", String.valueOf(retVal));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Flog.bdReport(this.mContext, 551, obj.toString());
        return retVal;
    }

    public String getCplc(String packageName) {
        String queryCplc;
        if (sechulePreCheck(packageName) != 0) {
            return null;
        }
        synchronized (this) {
            queryCplc = ESEInfoManager.getInstance(this.mContext).queryCplc(-1);
        }
        return queryCplc;
    }

    public int initEse(String packageName, String spID, String sign, String timeStamp) {
        int resultCode;
        int preCheckResult = sechulePreCheck(packageName);
        if (preCheckResult != 0) {
            return preCheckResult;
        }
        synchronized (this) {
            resultCode = new InitEseTsmOperator(this.mContext, spID, sign, timeStamp).excute(getReader(packageName, spID));
            HwLog.d("initEse  result : " + resultCode);
            JSONObject obj = new JSONObject();
            try {
                obj.put("PKG", packageName);
                obj.put("result", String.valueOf(resultCode));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Flog.bdReport(this.mContext, 552, obj.toString());
        }
        return resultCode;
    }

    private int handleOperations(String packageName, String spID, String ssdAid, String sign, String timeStamp, String operatorType) {
        int handleOperatorResult;
        HwLog.d("handleOperations: " + operatorType);
        if (!checkParams(packageName, spID, ssdAid, sign, timeStamp)) {
            HwLog.w("checkParams failed");
            return 1;
        }
        int preCheckResult = sechulePreCheck(packageName);
        if (preCheckResult != 0) {
            HwLog.w("sechulePreCheck failed, error code: " + preCheckResult);
            return preCheckResult;
        }
        synchronized (this) {
            CreateOrDeleteOpenSSDTsmOperator createOrDeleteOpenSSDTsmOperator = new CreateOrDeleteOpenSSDTsmOperator(this.mContext, ssdAid, spID, sign, timeStamp, operatorType);
            handleOperatorResult = handleOperatorResult(createOrDeleteOpenSSDTsmOperator.excute(getReader(packageName, spID)));
        }
        return handleOperatorResult;
    }

    private boolean checkParams(String packageName, String spID, String ssdAid, String sign, String timeStamp) {
        return !StringUtil.isTrimedEmpty(packageName) && !StringUtil.isTrimedEmpty(spID) && !StringUtil.isTrimedEmpty(ssdAid) && !StringUtil.isTrimedEmpty(sign) && !StringUtil.isTrimedEmpty(timeStamp);
    }

    public int getReader(String packageName, String spID) {
        if (StringUtil.isTrimedEmpty(packageName) || StringUtil.isTrimedEmpty(spID)) {
            HwLog.d("spID use card 1: -1");
            return -1;
        }
        int esetype = SystemProperties.getInt("ro.config.se_esetype", 2);
        HwLog.d("esetype: " + esetype);
        if (esetype != 3) {
            HwLog.d("spID use card: -1");
            return -1;
        } else if (spID.startsWith("NFCDK_")) {
            HwLog.d("NFCDK_ spID use card: 0");
            return 0;
        } else {
            HwLog.d("UKEY spID use card: 2");
            return 2;
        }
    }

    private int sechulePreCheck(String pkgName) {
        if (!checkSupportFeature()) {
            HwLog.w("checkSupportFeature failed ---------- not CN");
            return -1;
        } else if (!checkNetwork()) {
            HwLog.w("checkNetwork failed ---------- no network");
            return 3;
        } else if (checkCallerSignature(pkgName)) {
            return 0;
        } else {
            HwLog.w("checkCallerSignature failed ---------- invalid caller sign");
            return 2;
        }
    }

    public boolean userIsPrimary() {
        if (UserHandle.getUserId(Binder.getCallingUid()) == 0) {
            return true;
        }
        return false;
    }

    private boolean checkSupportFeature() {
        HwLog.d("checkSupportFeature");
        return "CN".equalsIgnoreCase(SystemProperties.get(WifiProCommonUtils.KEY_PROP_LOCALE, ""));
    }

    public boolean checkNetwork() {
        HwLog.d("checkNetwork");
        return NetworkUtil.isNetworkConnected(this.mContext);
    }

    public boolean checkCallerSignature(String callerPkgName) {
        HwLog.d("checkCallerSignature");
        String signsBuilder = getSignsBuilder(callerPkgName);
        ArrayList<String> arrayList = this.mAuthorizedCallers;
        if (arrayList.contains(callerPkgName + signsBuilder)) {
            HwLog.d("checkCaller success ---------- end");
            return true;
        }
        QueryDicsRequset request = new QueryDicsRequset();
        request.itemName = callerPkgName;
        request.dicName = DIC_NAME;
        QueryDicsResponse response = new CardServer(this.mContext).queryDics(request);
        if (response == null || response.returnCode != 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("checkCaller failed ---------- query server failed. retCode = ");
            sb.append(response == null ? "response is null" : Integer.valueOf(response.returnCode));
            HwLog.w(sb.toString());
            return false;
        } else if (response.dicItems.size() <= 0) {
            HwLog.w("checkCaller failed ---------- dicItems size <= 0");
            return false;
        } else {
            int i = 0;
            int size = response.dicItems.size();
            while (i < size) {
                DicItem item = response.dicItems.get(i);
                if (callerPkgName == null || signsBuilder == null || !callerPkgName.equals(item.getParent()) || !signsBuilder.equals(item.getValue())) {
                    i++;
                } else {
                    ArrayList<String> arrayList2 = this.mAuthorizedCallers;
                    arrayList2.add(callerPkgName + signsBuilder);
                    HwLog.d("checkCaller success ---------- end");
                    return true;
                }
            }
            HwLog.w("checkCaller failed ---------- The caller pkg is not allowed. pkg = " + callerPkgName);
            return false;
        }
    }

    private String getSignsBuilder(String callerPkgName) {
        try {
            List<String> signs = PackageSignatureUtil.getInstalledAppHashList(this.mContext, callerPkgName);
            if (signs == null || signs.size() <= 0) {
                return null;
            }
            StringBuilder sBuilder = new StringBuilder();
            for (String sign : signs) {
                sBuilder.append(sign);
            }
            return sBuilder.toString();
        } catch (AccessControlException | NoSuchAlgorithmException | CertificateException e) {
            HwLog.w("checkCaller failed ---------- can not get hashList. pkg：" + callerPkgName);
            return null;
        }
    }

    public int handleOperatorResult(int result) {
        int returnCode;
        if (result != -99) {
            switch (result) {
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
        } else {
            returnCode = -99;
        }
        HwLog.d("handleOperatorResult : " + result);
        return returnCode;
    }
}
