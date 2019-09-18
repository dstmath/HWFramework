package com.huawei.wallet.sdk.business.bankcard.manager;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.api.CUPOperationListener;
import com.huawei.wallet.sdk.business.bankcard.constant.BankcardConstant;
import com.huawei.wallet.sdk.business.bankcard.modle.PushCUPOperateMessage;
import com.huawei.wallet.sdk.business.bankcard.request.QueryAidRequest;
import com.huawei.wallet.sdk.business.bankcard.request.QueryUnionPayPushRequest;
import com.huawei.wallet.sdk.business.bankcard.response.PushMessageParser;
import com.huawei.wallet.sdk.business.bankcard.response.QueryAidResponse;
import com.huawei.wallet.sdk.business.bankcard.response.QueryUnionPayPushResponse;
import com.huawei.wallet.sdk.business.bankcard.server.BankCardServer;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.apdu.ese.impl.ESEInfoManager;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import java.util.List;

public abstract class CUPTsmLibDataWaiter implements CUPOperationListener {
    private static final int FIRST_WAIT_OPERATION_START_TIME_OUT = 6000;
    private static final int OPERATION_FINISHED = 1;
    private static final int OPERATION_NOT_START = -1;
    private static final int OPERATION_STARTED = 0;
    private static final int WAITING_TIMES = 6;
    private static final int WAIT_OPERATION_START_TIME_OUT = 3000;
    private static final int WAIT_OPERATION_TIME_OUT = 60000;
    protected final Context mContext;
    private int mOperationResult = -1;
    private int mOperationStatus = -1;
    protected final String mOperationType;

    public CUPTsmLibDataWaiter(Context context, String operation) {
        this.mContext = context;
        this.mOperationType = operation;
    }

    /* access modifiers changed from: protected */
    public boolean waitOperationResult(List<String> refIDs) {
        waitDeleteStarted(refIDs);
        return waitAndHandleDeleteResult();
    }

    private synchronized boolean waitAndHandleDeleteResult() {
        if (-1 == this.mOperationStatus) {
            return false;
        }
        try {
            LogC.d("check delete operation result: " + this.mOperationStatus, false);
            while (this.mOperationStatus == 0) {
                wait(60000);
                LogC.d("after wait for a while, the delete operation result: " + this.mOperationStatus, false);
                if (this.mOperationResult != 0 && this.mOperationResult != -99) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            LogC.e("install cup card wait the download result, but interrupted.", false);
        }
        if (this.mOperationResult == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean checkAndCleanCupTAData(List<String> refIDs) {
        boolean result = true;
        if (refIDs != null && !refIDs.isEmpty()) {
            for (String s : refIDs) {
                if (WalletTaManager.getInstance(this.mContext).getCard(s) != null) {
                    QueryAidRequest request = new QueryAidRequest();
                    request.setCplc(ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc());
                    request.setCardRefId(s);
                    QueryAidResponse response = new BankCardServer(this.mContext).queryAidOnCUP(request);
                    if (response != null) {
                        LogC.i("QueryAidResponse, resultCode: " + response.returnCode, false);
                        if (response.returnCode == -5) {
                            removeTaInfo(s);
                        } else {
                            result = false;
                        }
                    }
                }
            }
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public boolean removeTaInfo(String refID) {
        try {
            WalletTaManager.getInstance(this.mContext).removeCard(refID);
            return true;
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogC.e("WalletTaCardNotExistException, mRefID : " + refID, false);
            return true;
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogC.e("WalletTaSystemErrorException, mRefID : " + refID, false);
            return false;
        }
    }

    private synchronized void waitDeleteStarted(List<String> refIDs) {
        int waiterCounter = 0;
        do {
            if (waiterCounter == 0) {
                try {
                    wait(6000);
                } catch (InterruptedException e) {
                    LogC.e("wait delete operation time out.", false);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                wait(3000);
            }
            waiterCounter++;
            if (-1 == this.mOperationStatus) {
                LogC.d("wait delete operation start, quire tsmlibData", false);
                if (getTsmLibData(refIDs)) {
                    wait(3000);
                }
            }
            if (waiterCounter > 6 || this.mOperationStatus == 0) {
                LogC.d("wait delete operation start, or timeout.", false);
            }
        } while (1 != this.mOperationStatus);
        LogC.d("wait delete operation start, or timeout.", false);
    }

    /* access modifiers changed from: protected */
    public boolean getTsmLibData(List<String> refIDs) {
        QueryUnionPayPushRequest request = new QueryUnionPayPushRequest();
        request.cplc = ESEInfoManager.getInstance(this.mContext).queryCplc();
        QueryUnionPayPushResponse response = new BankCardServer(this.mContext).queryUnionPayPush(request);
        if (response != null && response.returnCode == 0) {
            String pushMsg = response.getPushMsg();
            if (pushMsg != null) {
                Object pushMsgObject = new PushMessageParser().parsePushMessage(pushMsg);
                if (pushMsgObject instanceof PushCUPOperateMessage) {
                    return handlePushMessage((PushCUPOperateMessage) pushMsgObject, refIDs);
                }
            }
        }
        return false;
    }

    private boolean handlePushMessage(PushCUPOperateMessage pushMsgObject, List<String> refIDs) {
        if (pushMsgObject.getVirtualCards() == null || !this.mOperationType.equals(pushMsgObject.getEvent()) || (!BankcardConstant.OPERATE_EVENT_WIPEOUT.equals(this.mOperationType) && !pushMsgObject.getVirtualCards().equals(refIDs))) {
            LogC.i("query CUP tsmlibdata unstatified!", false);
            return false;
        }
        LogC.i("startCUPOperateService, mRefIDs size : " + refIDs.size(), false);
        if (this.mOperationStatus == -1) {
            BankCardOperateServiceManager.startCUPOperateService(this.mContext, this.mOperationType, pushMsgObject.getSsid(), pushMsgObject.getSign(), pushMsgObject.getVirtualCards());
        }
        return true;
    }

    public void operateStart(String event) {
        LogC.d("operateStart now.", false);
        synchronized (this) {
            this.mOperationStatus = 0;
            notifyAll();
        }
    }

    public void operateFinished(int resultCode, String event) {
        LogC.d("operateFinished result: " + resultCode, false);
        synchronized (this) {
            this.mOperationStatus = 1;
            this.mOperationResult = resultCode;
            notifyAll();
        }
    }
}
