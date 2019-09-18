package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.api.HandleDeleteLocalCardsCallback;
import com.huawei.wallet.sdk.business.bankcard.constant.BankcardConstant;
import com.huawei.wallet.sdk.business.bankcard.manager.BankCardOperateLogic;
import com.huawei.wallet.sdk.business.bankcard.manager.CUPTsmLibDataWaiter;
import com.huawei.wallet.sdk.business.bankcard.modle.SwipeCardInfo;
import com.huawei.wallet.sdk.business.bankcard.request.WipeAllBankCardRequest;
import com.huawei.wallet.sdk.business.bankcard.response.CardSwipeResponse;
import com.huawei.wallet.sdk.business.bankcard.server.BankCardServer;
import com.huawei.wallet.sdk.business.bankcard.util.CleanCupCardOperator;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea.OverSeasManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CleanAllLocalBankCardsTask extends CUPTsmLibDataWaiter implements Runnable {
    private boolean cleanOverSeaCardResult = true;
    private final HandleDeleteLocalCardsCallback mCallback;
    private boolean mCanRetry = true;
    private final Context mContext;
    private String mSource;

    public CleanAllLocalBankCardsTask(Context context, HandleDeleteLocalCardsCallback callback) {
        super(context, BankcardConstant.OPERATE_EVENT_WIPEOUT);
        this.mContext = context;
        this.mCallback = callback;
        this.mSource = "HandSet";
        this.mCanRetry = true;
    }

    public CleanAllLocalBankCardsTask(Context context, HandleDeleteLocalCardsCallback callback, boolean canRetry) {
        super(context, BankcardConstant.OPERATE_EVENT_WIPEOUT);
        this.mContext = context;
        this.mCallback = callback;
        this.mSource = "HandSet";
        this.mCanRetry = canRetry;
    }

    public void run() {
        boolean z = false;
        LogC.i("CleanAllLocalCardsTask run", false);
        boolean cleanCUPCardResult = true;
        String countryCode = OverSeasManager.getInstance(this.mContext).getCountryCodeFromTA();
        boolean isChina = true;
        if (!TextUtils.isEmpty(countryCode)) {
            isChina = countryCode.equalsIgnoreCase("CN");
        }
        if (isChina) {
            List<TACardInfo> taCardInfos = WalletTaManager.getInstance(this.mContext).getCardList();
            if (taCardInfos == null || taCardInfos.isEmpty()) {
                LogC.i("delete all bank card task, but no local card.", false);
                notifyDeleteResult(true);
                return;
            }
            List<TACardInfo> cupCardInfos = null;
            for (TACardInfo cardinfo : taCardInfos) {
                if (cardinfo.getCardGroupType() != 1) {
                    LogC.i("DeleteAllLocalBankCardsTask, only bank card need to handle", false);
                } else if (!"A0000003330101020063020000000301".equals(cardinfo.getAid())) {
                    if (cupCardInfos == null) {
                        cupCardInfos = new ArrayList<>();
                    }
                    cupCardInfos.add(cardinfo);
                }
            }
            if (cupCardInfos != null) {
                LogC.i("CleanAllLocalBankCardsTask|run|start clean cup card", false);
                cleanCUPCardResult = cleanCUPCard(cupCardInfos);
            }
        } else {
            cleanOverSeaCard();
        }
        if (cleanCUPCardResult && this.cleanOverSeaCardResult) {
            z = true;
        }
        notifyDeleteResult(z);
    }

    private void cleanOverSeaCard() {
        WipeAllBankCardRequest request = new WipeAllBankCardRequest();
        request.setCplc(ESEInfoManager.getInstance(this.mContext).queryCplc());
        request.setEvent("10");
        CardSwipeResponse response = new BankCardServer(this.mContext).wipeAllBankCard(request);
        if (response == null || response.returnCode != 0) {
            LogC.i("DeleteAllLocalCardsTask wipeAllBankCard fail", false);
            this.cleanOverSeaCardResult = false;
        } else {
            LogX.d("DeleteAllLocalCardsTask wipeAllBankCard success.");
            ArrayList<SwipeCardInfo> list = response.getSwipeCardInfoList();
            if (list == null || list.isEmpty()) {
                LogC.i("DeleteAllLocalCardsTask wipeAllBankCard response cardList is null", false);
                return;
            }
            LogX.i("need delete list size = " + list.size());
            Iterator<SwipeCardInfo> it = list.iterator();
            while (it.hasNext()) {
                SwipeCardInfo item = it.next();
                LogX.d("CardSwipeResponse aid = " + item.getAid() + " item = " + item.getTokenID());
                if (TextUtils.equals(item.getStatus(), "6")) {
                    LogX.i("CardSwipeResponse status, has deleted");
                } else {
                    Map<String, String> params = new HashMap<>();
                    params.put("aid", item.getAid());
                    params.put(HandleOverSeaCardNullifiedTask.REF_ID, item.getTokenID());
                    params.put("issuerId", item.getIssuerId());
                    LogX.i("CardSwipeResponse do delete, status = " + item.getStatus());
                    if (!new HandleOverSeaCardNullifiedTask(this.mContext, params).modifyCard(this.mSource)) {
                        LogC.i("modifyCard fail", false);
                        this.cleanOverSeaCardResult = false;
                    }
                }
            }
        }
    }

    private boolean cleanCUPCard(List<TACardInfo> cupCardInfos) {
        if (!requestSwipeCupCard()) {
            return false;
        }
        BankCardOperateLogic.getInstance(this.mContext).registerCUPOperationListener(BankcardConstant.OPERATE_EVENT_WIPEOUT, null, this);
        List<String> refIDs = new ArrayList<>();
        for (TACardInfo t : cupCardInfos) {
            refIDs.add(t.getDpanDigest());
        }
        boolean result = waitOperationResult(refIDs);
        BankCardOperateLogic.getInstance(this.mContext).unregisterCUPOperationListener(BankcardConstant.OPERATE_EVENT_WIPEOUT, null, this);
        checkAndCleanCupTAData(refIDs);
        return result;
    }

    private boolean requestSwipeCupCard() {
        LogC.i("requestSwipeCupCard   begin", false);
        WipeAllBankCardRequest request = new WipeAllBankCardRequest();
        request.setCplc(ESEInfoManager.getInstance(this.mContext).queryCplc());
        request.setEvent("10");
        request.setBrand(11);
        CardServerBaseResponse response = new BankCardServer(this.mContext).wipeAllBankCard(request);
        if (response == null || response.returnCode != 0) {
            return false;
        }
        LogC.i("requestSwipeCupCard success.", false);
        return true;
    }

    private void notifyDeleteResult(boolean isSuccess) {
        LogC.i("notifyDeleteResult isSuccess" + isSuccess, false);
        LogC.i("notifyDeleteResult mCanRetry" + this.mCanRetry, false);
        if (!isSuccess && this.mCanRetry) {
            CleanCupCardOperator.startCleanBankCard(this.mContext, this.mCallback, false);
        } else if (this.mCallback != null) {
            this.mCallback.handleDeletelocalcardCallback(isSuccess);
        }
    }
}
