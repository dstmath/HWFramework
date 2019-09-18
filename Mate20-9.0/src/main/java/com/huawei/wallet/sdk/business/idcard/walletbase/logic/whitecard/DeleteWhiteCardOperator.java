package com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard;

import android.content.Context;
import android.os.Build;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.base.BaseOperator;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.business.idcard.walletbase.whitecard.BaseResultHandler;
import com.huawei.wallet.sdk.common.apdu.base.BaseCallback;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ErrorInfoCreator;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.List;

public class DeleteWhiteCardOperator extends BaseOperator {
    private static final String TAG = "DeleteWhiteCardOperator";

    public DeleteWhiteCardOperator(Context context, BaseResultHandler handle) {
        super(context, handle);
    }

    public void deleteWhiteCard(String passTypeId, String passId, String aid) {
        LogC.i(TAG, "deleteWhiteCard: ------------" + passTypeId, false);
        if (!deleteApplet(passTypeId, aid)) {
            handleResult(BaseCallback.RESULT_FAILED_DELETE_PASS, ErrorInfoCreator.buildSimpleErrorInfo(BaseCallback.RESULT_FAILED_DELETE_PASS));
            return;
        }
        if (!deleteTa(aid)) {
            handleResult(-4, ErrorInfoCreator.buildSimpleErrorInfo(-4));
        } else {
            handleResult(0, ErrorInfoCreator.buildSimpleErrorInfo(0));
        }
        LogC.i(TAG, "finish to deleteWhiteCard", false);
    }

    private boolean deleteTa(String aid) {
        LogC.i(TAG, "deleteTa: start---", false);
        boolean isRemoveSuccess = false;
        if (WalletTaManager.getInstance(this.mContext).getCardInfoByAid(aid) != null) {
            try {
                WalletTaManager.getInstance(this.mContext).removeCardByAid(aid);
                isRemoveSuccess = true;
            } catch (WalletTaException.WalletTaCardNotExistException e) {
                LogX.e("WalletTaCardNotExistException ", e.getMessage());
                isRemoveSuccess = true;
            } catch (WalletTaException.WalletTaSystemErrorException e2) {
                LogX.e("WalletTaSystemErrorException ", e2.getMessage());
            }
        } else {
            isRemoveSuccess = true;
            LogC.i(TAG, "deleteTa: already deleted TA", false);
        }
        LogC.i(TAG, "deleteTa: end ---", false);
        return isRemoveSuccess;
    }

    private boolean deleteApplet(String passTypeId, String aid) {
        boolean deleteSuccess = false;
        LogC.i("Begin to deleteApplet for whitecard", false);
        String cplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        LogC.i("check cplc is null when deleteApplet for whitecard:" + StringUtil.isEmpty(cplc, true), false);
        String seChipType = ProductConfigUtil.geteSEManufacturer();
        String passTypeGroup = getPassTypeGroup(passTypeId);
        if (StringUtil.isEmpty(passTypeGroup, true)) {
            LogC.e("No passTypeGroup, delete failed", false);
            return false;
        }
        ServerAccessDeleteAppletRequest request = new ServerAccessDeleteAppletRequest(passTypeGroup, cplc, aid, Build.MODEL, seChipType);
        ServerAccessDeleteAppletResponse response = this.cardServer.deleteApplet(request);
        if (response.returnCode != 0) {
            LogC.i(TAG, "deleteApplet: fail ; code == " + response.returnCode, false);
            handleResult(response.returnCode, response.getErrorInfo());
        } else {
            LogC.i(TAG, "deleteApplet: success", false);
            String transactionId = response.getTransactionId();
            List<ServerAccessAPDU> apduList = response.getApduList();
            if (apduList == null) {
                List<ServerAccessAPDU> list = apduList;
            } else if (apduList.isEmpty()) {
                List<ServerAccessAPDU> list2 = apduList;
            } else {
                List<ServerAccessAPDU> list3 = apduList;
                deleteSuccess = executeCommand(this.mContext, transactionId, apduList, response.returnCode, passTypeGroup, aid, response.getNextStep());
                LogC.i(TAG, "deleteApplet: executeCommand " + deleteSuccess, false);
            }
            deleteSuccess = true;
        }
        LogC.e("deleteApplet result:" + deleteSuccess, false);
        return deleteSuccess;
    }
}
