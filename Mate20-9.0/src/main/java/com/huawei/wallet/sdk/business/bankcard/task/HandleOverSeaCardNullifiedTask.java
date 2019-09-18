package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.bankcard.request.DeleteOverSeaCardRequest;
import com.huawei.wallet.sdk.business.bankcard.response.NullifyCardResponse;
import com.huawei.wallet.sdk.business.bankcard.server.BankCardServer;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HandleOverSeaCardNullifiedTask {
    public static final String AID = "aid";
    public static final String ISSUER_ID = "issuerId";
    public static final String REF_ID = "refId";
    private static final String TAG = HandleOverSeaCardNullifiedTask.class.getSimpleName();
    private Context mContext;
    private Map<String, String> params;

    public HandleOverSeaCardNullifiedTask(Context context, Map<String, String> params2) {
        this.mContext = context;
        this.params = params2;
    }

    public boolean modifyCard(String source) {
        NullifyCardResponse nullifyResponse;
        String str = source;
        LogX.i(TAG + " modifyCard ,source is :" + str);
        if (this.params == null) {
            LogX.w(TAG + " modifyCard ,params is null ");
            return false;
        }
        String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
        if (StringUtil.isEmpty(cplc, true)) {
            NullifyCardResponse nullifyResponse2 = new NullifyCardResponse();
            nullifyResponse2.returnCode = -10;
            LogX.i(TAG + " modifyCard ,cplc is empty");
            nullifyResponse = nullifyResponse2;
        } else {
            DeleteOverSeaCardRequest request = new DeleteOverSeaCardRequest();
            request.setCplc(cplc);
            request.setRefId(this.params.get(REF_ID));
            request.setSource(str);
            request.setAppletAid(this.params.get("aid"));
            request.setIssuerId(this.params.get("issuerId"));
            nullifyResponse = new BankCardServer(this.mContext).deleteOverSeaCard(request);
            if (nullifyResponse == null || nullifyResponse.returnCode != 0) {
                if (nullifyResponse == null) {
                    nullifyResponse = new NullifyCardResponse();
                }
                nullifyResponse.setResultCode(1);
                nullifyResponse.setResultDesc("client check, invalid param");
            } else {
                String transactionId = nullifyResponse.getTransactionId();
                List<ServerAccessAPDU> apduList = nullifyResponse.getApduList();
                if (apduList == null || apduList.isEmpty()) {
                    nullifyResponse.setResultCode(0);
                } else {
                    HashMap hashMap = new HashMap();
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
                    hashMap.put("cplc", request.getCplc());
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.TOKENREFID, request.getRefId());
                    hashMap.put(ExecuteApduTask.DEVICE_MODEL, Build.MODEL);
                    hashMap.put(ExecuteApduTask.MANU_FACTURER, ProductConfigUtil.geteSEManufacturer());
                    hashMap.put(ExecuteApduTask.NEXT_STEP, nullifyResponse.getNextStep());
                    hashMap.put(ExecuteApduTask.PARTNER_ID, "");
                    hashMap.put(ExecuteApduTask.TRANSACTION_ID, transactionId);
                    hashMap.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
                    hashMap.put(ServerAccessApplyAPDURequest.ReqKey.COMMANDID, "delete.app");
                    HashMap hashMap2 = hashMap;
                    ExecuteApduTask.getInstance(this.mContext).executeCommand(apduList, hashMap, nullifyResponse, true, false);
                }
            }
        }
        if (nullifyResponse.getResultCode() == 0) {
            LogX.i(TAG + " modifyCard ,delete applet ok :" + str);
            return true;
        }
        LogX.w(TAG + " modifyCard ,delete applet failed ,resultCode is " + nullifyResponse.getResultCode());
        return false;
    }
}
