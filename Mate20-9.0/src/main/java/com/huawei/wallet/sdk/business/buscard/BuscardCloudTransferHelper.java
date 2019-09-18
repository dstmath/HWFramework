package com.huawei.wallet.sdk.business.buscard;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.wallet.sdk.WalletFactory;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoServerItem;
import com.huawei.wallet.sdk.business.buscard.api.CardOperateLogic;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletInfoApiFactory;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.result.TransferOutTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.base.result.UninstallTrafficCardCallback;
import com.huawei.wallet.sdk.business.buscard.model.TransferOutTrafficCardResultHandler;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.logger.LoggerConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.CardProductInfoServerItem;
import com.huawei.wallet.sdk.common.apdu.model.RequestParam;
import com.huawei.wallet.sdk.common.apdu.request.QueryCardProductInfoRequest;
import com.huawei.wallet.sdk.common.apdu.request.QueryIssuerInfoRequest;
import com.huawei.wallet.sdk.common.apdu.response.QueryCardProductInfoResponse;
import com.huawei.wallet.sdk.common.apdu.response.QueryIssuerInfoResponse;
import com.huawei.wallet.sdk.common.dbmanager.CardProductInfoItem;
import com.huawei.wallet.sdk.common.http.service.CommonService;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BuscardCloudTransferHelper {
    public static final int BUSCARD_DELETE_BY_THIRDAPP = 2;
    public static final int BUSCARD_DELETE_BY_WALEET = 1;
    public static final int BUSCARD_NOT_SUPPORT_DELETE = 0;
    public static final int BUSCARD_SUPPORT_DELETE = 1;
    private static final int OPERATION_FAILED = -1;
    private static final int OPERATION_SUCCESS = 0;
    /* access modifiers changed from: private */
    public static BuscardResultCallback buscardResultCallback;
    private static final Object cardCountListLock = new Object();
    private static HashMap<String, IssuerInfoItem> issuerInfoItems = new HashMap<>();
    private static Map<String, CardProductInfoItem> mCardProductInfos = new HashMap();
    /* access modifiers changed from: private */
    public static Context mContext;
    /* access modifiers changed from: private */
    public static volatile int mDealCardCount = 0;
    public static final Double mVersionCode = Double.valueOf(1.00013E7d);
    /* access modifiers changed from: private */
    public static boolean mhasDealFailed = false;

    static class BuscardResultCallback {
        private boolean mHasTry = false;
        private TransferOutTrafficCardResultHandler mResultHandler;

        BuscardResultCallback(TransferOutTrafficCardResultHandler resultHandler) {
            this.mResultHandler = resultHandler;
        }

        public void handleResult(int resultCode) {
            if (resultCode == 0) {
                this.mResultHandler.handleResult(resultCode);
                LogC.i("transferBuscardToCloud OPERATION_SUCCESS", false);
                return;
            }
            LogC.i("transferBuscardToCloud OPERATION_FAILED mHasTry is " + this.mHasTry, false);
            if (this.mHasTry) {
                this.mResultHandler.handleResult(resultCode);
                return;
            }
            this.mHasTry = true;
            BuscardCloudTransferHelper.transferBuscardToCloudImp(BuscardCloudTransferHelper.mContext, BuscardCloudTransferHelper.buscardResultCallback);
        }
    }

    public static void addDealCardCount(int num) {
        synchronized (cardCountListLock) {
            mDealCardCount = num;
        }
    }

    public static void reduceDealCardCount() {
        synchronized (cardCountListLock) {
            mDealCardCount--;
        }
    }

    public static IssuerInfoItem getIssuerInfo(String issuerId) {
        return issuerInfoItems.get(issuerId);
    }

    public static CardProductInfoItem getProductInfo(String productId) {
        return mCardProductInfos.get(productId);
    }

    public static void transferBuscardToCloud(Context activity, TransferOutTrafficCardCallback callback) {
        mContext = activity;
        buscardResultCallback = new BuscardResultCallback(new TransferOutTrafficCardResultHandler(new Handler(mContext.getMainLooper()), callback));
        new Thread(new Runnable() {
            public void run() {
                BuscardCloudTransferHelper.transferBuscardToCloudImp(BuscardCloudTransferHelper.mContext, BuscardCloudTransferHelper.buscardResultCallback);
            }
        }).start();
    }

    public static ArrayList<TACardInfo> getBusCardList(ArrayList<TACardInfo> cardInfos) {
        int deleteMode;
        int isSupportDelete;
        ArrayList<TACardInfo> busCardList = new ArrayList<>();
        Iterator<TACardInfo> it = cardInfos.iterator();
        while (it.hasNext()) {
            TACardInfo itemTA = it.next();
            if (itemTA.getCardType() == 11 && (itemTA.getCardStatus() == 2 || itemTA.getCardStatus() == 15 || itemTA.getCardStatus() == 22 || itemTA.getCardStatus() == 21)) {
                IssuerInfoItem issuerInfoItem = issuerInfoItems.get(itemTA.getIssuerId());
                boolean notSuppportTransferDelete = WalletFactory.getInstance(mContext, null).isNotSupportTranfserBusCardDelete();
                if (issuerInfoItem != null) {
                    boolean isTransfer = false;
                    try {
                        if (issuerInfoItem.mMinSupportCloudTransferWalletVersion != null) {
                            LogC.i("transferBuscardToCloud issuerid is  " + issuerInfoItem.getIssuerId() + "issuerInfoItem.mMinSupportCloudTransferWalletVersion : " + issuerInfoItem.mMinSupportCloudTransferWalletVersion + " mVersionCode : " + mVersionCode, false);
                            Double minVersion = Double.valueOf(Double.parseDouble(issuerInfoItem.mMinSupportCloudTransferWalletVersion));
                            isTransfer = minVersion.doubleValue() > 0.0d && mVersionCode.doubleValue() >= minVersion.doubleValue();
                        }
                        boolean isTransfer2 = isTransfer;
                        if (isTransfer2) {
                            busCardList.add(itemTA);
                        } else if (!isTransfer2 && notSuppportTransferDelete) {
                            try {
                                if (Integer.valueOf(checkBalance(issuerInfoItem)).intValue() >= 0) {
                                    String minSupportDeleteWalletVersion = issuerInfoItem.getMinSupportDeleteWalletVersion();
                                    if (minSupportDeleteWalletVersion != null) {
                                        Double minSupportVersion = Double.valueOf(Double.parseDouble(minSupportDeleteWalletVersion));
                                        isSupportDelete = (minSupportVersion.doubleValue() <= 0.0d || mVersionCode.doubleValue() < minSupportVersion.doubleValue()) ? 0 : 1;
                                        deleteMode = issuerInfoItem.getDeleteMode();
                                    } else {
                                        isSupportDelete = issuerInfoItem.getIsSupportDelete();
                                        deleteMode = 1;
                                    }
                                    String deviceType = PhoneDeviceUtil.getDeviceType();
                                    if ("t_sh_01".equals(issuerInfoItem) && (TextUtils.isEmpty(deviceType) || (!deviceType.contains("KNT") && !deviceType.contains("FRD")))) {
                                        isSupportDelete = 0;
                                    }
                                    if (isSupportDelete == 1 && deleteMode == 1) {
                                        busCardList.add(itemTA);
                                    }
                                } else {
                                    LogC.i("transferBuscardToCloud balance overdrawn  ", false);
                                }
                            } catch (TrafficCardOperateException e) {
                                LogC.i("transferBuscardToCloud balance to int wrong  ", false);
                            }
                        }
                    } catch (NumberFormatException e2) {
                        LogC.d("isSupportCloudTransfer transfer or delere error", false);
                    }
                } else {
                    LogC.d("transferBuscardToCloud itemTA.getIssuerId() " + itemTA.getIssuerId() + " issuerinfo is null", false);
                }
            }
        }
        return busCardList;
    }

    public static void transferBuscardToCloudImp(Context activity, BuscardResultCallback buscardResultCallback2) {
        final BuscardResultCallback buscardResultCallback3 = buscardResultCallback2;
        if (issuerInfoItems == null || issuerInfoItems.size() == 0) {
            QueryIssuerInfoRequest request = new QueryIssuerInfoRequest();
            request.timeStamp = 0;
            QueryIssuerInfoResponse response = new CommonService(activity, AddressNameMgr.MODULE_NAME_WALLET).queryIssuerInfo(request);
            int sz = response.issueInfos.size();
            LogC.i("transferBuscardToCloud syncIssuerInfoFromServer queryIssuerInfo response.returnCode : " + response.returnCode + " size : " + sz, false);
            if (response.returnCode == 0) {
                for (IssuerInfoServerItem info : response.issueInfos) {
                    IssuerInfoItem item = new IssuerInfoItem(info);
                    issuerInfoItems.put(item.getIssuerId(), item);
                }
            } else {
                buscardResultCallback3.handleResult(-1);
                return;
            }
        } else {
            Context context = activity;
        }
        if (mCardProductInfos == null || mCardProductInfos.size() == 0) {
            Map<String, IssuerInfoItem> issuerInfosMap = issuerInfoItems;
            QueryCardProductInfoRequest productInfoRequest = new QueryCardProductInfoRequest();
            Set<RequestParam> allTrafficCardParam = getAllTrafficCardReqestParams(issuerInfosMap);
            productInfoRequest.setTimeStamp(0);
            productInfoRequest.setFilters(convertParams(allTrafficCardParam));
            QueryCardProductInfoResponse productInfoResponse = new CommonService(mContext, AddressNameMgr.MODULE_NAME_WALLET).queryCardProductInfoList(productInfoRequest);
            int size = productInfoResponse.items.size();
            if (productInfoResponse.returnCode == 0) {
                for (CardProductInfoServerItem info2 : productInfoResponse.items) {
                    CardProductInfoItem item2 = new CardProductInfoItem(info2);
                    mCardProductInfos.put(item2.getProductId(), item2);
                }
                LogC.i("syncCardProductInfoFromServer queryCardProductInfoList response.returnCode : " + productInfoResponse.returnCode + " size : " + size, false);
            } else {
                buscardResultCallback3.handleResult(-1);
                return;
            }
        }
        ArrayList<TACardInfo> cardInfos = getBusCardList(WalletTaManager.getInstance(activity).getCardList());
        if (cardInfos == null || cardInfos.isEmpty()) {
            LogC.d("transferBuscardToCloud cardInfos is null or empty", false);
            buscardResultCallback3.handleResult(0);
            return;
        }
        LogC.d("transferBuscardToCloud cardInfos size is " + cardInfos, false);
        addDealCardCount(cardInfos.size());
        mhasDealFailed = false;
        Iterator<TACardInfo> it = cardInfos.iterator();
        while (it.hasNext()) {
            TACardInfo itemTA = it.next();
            final IssuerInfoItem issuerInfoItem = issuerInfoItems.get(itemTA.getIssuerId());
            boolean isTransfer = false;
            try {
                if (issuerInfoItem.mMinSupportCloudTransferWalletVersion != null) {
                    LogC.i("transferBuscardToCloud issuerid is  " + issuerInfoItem.getIssuerId() + "issuerInfoItem.mMinSupportCloudTransferWalletVersion : " + issuerInfoItem.mMinSupportCloudTransferWalletVersion + " mVersionCode : " + mVersionCode, false);
                    Double minVersion = Double.valueOf(Double.parseDouble(issuerInfoItem.mMinSupportCloudTransferWalletVersion));
                    isTransfer = minVersion.doubleValue() > 0.0d && mVersionCode.doubleValue() >= minVersion.doubleValue();
                }
                if (isTransfer) {
                    LogC.i("transferBuscardToCloud Transfer begin issuerid is  " + issuerInfoItem.getIssuerId(), false);
                    CardOperateLogic.getInstance(activity).cloudTransferOut(null, itemTA.getIssuerId(), new TransferOutTrafficCardCallback() {
                        public void transferOutCallback(int resultCode) {
                            BuscardCloudTransferHelper.reduceDealCardCount();
                            if (resultCode != 0) {
                                boolean unused = BuscardCloudTransferHelper.mhasDealFailed = true;
                            }
                            LogC.i("transferBuscardToCloud Transfer issuerid is " + issuerInfoItem.getIssuerId() + " mDealCardCount is " + BuscardCloudTransferHelper.mDealCardCount + "resultcode is " + resultCode, false);
                            if (BuscardCloudTransferHelper.mDealCardCount == 0) {
                                if (BuscardCloudTransferHelper.mhasDealFailed) {
                                    buscardResultCallback3.handleResult(-1);
                                } else {
                                    buscardResultCallback3.handleResult(0);
                                }
                            }
                        }
                    });
                } else {
                    LogC.i("transferBuscardToCloud Delete begin issuerid is  " + issuerInfoItem.getIssuerId(), false);
                    CardOperateLogic.getInstance(activity).uninstallTrafficCard(itemTA.getIssuerId(), new UninstallTrafficCardCallback() {
                        public void uninstallTrafficCardCallback(int resultCode) {
                            BuscardCloudTransferHelper.reduceDealCardCount();
                            if (resultCode != 0) {
                                boolean unused = BuscardCloudTransferHelper.mhasDealFailed = true;
                            }
                            LogC.i("transferBuscardToCloud Transfer issuerid  is " + issuerInfoItem.getIssuerId() + "is  mDealCardCount" + BuscardCloudTransferHelper.mDealCardCount + "resultcode is " + resultCode, false);
                            if (BuscardCloudTransferHelper.mDealCardCount == 0) {
                                if (BuscardCloudTransferHelper.mhasDealFailed) {
                                    buscardResultCallback3.handleResult(-1);
                                } else {
                                    buscardResultCallback3.handleResult(0);
                                }
                            }
                        }
                    }, true, "", "resetsdk and uninstall card", "1", null, null);
                }
            } catch (NumberFormatException e) {
                LogC.d("isSupportCloudTransfer transfer or delere error", false);
            }
        }
        LogC.d("transferBuscardToCloud cardInfos end", false);
    }

    static void syncCardProductInfoFromServer(boolean isNeedResetTimeStamp) {
        Map<String, IssuerInfoItem> issuerInfosMap = issuerInfoItems;
        QueryCardProductInfoRequest request = new QueryCardProductInfoRequest();
        Set<RequestParam> allTrafficCardParam = getAllTrafficCardReqestParams(issuerInfosMap);
        request.setTimeStamp(0);
        request.setFilters(convertParams(allTrafficCardParam));
        QueryCardProductInfoResponse response = new CommonService(mContext, AddressNameMgr.MODULE_NAME_WALLET).queryCardProductInfoList(request);
        int sz = response.items.size();
        if (response.returnCode == 0) {
            for (CardProductInfoServerItem info : response.items) {
                CardProductInfoItem item = new CardProductInfoItem(info);
                mCardProductInfos.put(item.getProductId(), item);
            }
        }
        LogC.i("syncCardProductInfoFromServer queryCardProductInfoList response.returnCode : " + response.returnCode + " size : " + sz, false);
    }

    private static Set<RequestParam> getAllTrafficCardReqestParams(Map<String, IssuerInfoItem> issuerInfosInDB) {
        Set<RequestParam> params = new HashSet<>();
        if (issuerInfosInDB == null || issuerInfosInDB.isEmpty()) {
            return params;
        }
        for (IssuerInfoItem issuerInfo : issuerInfosInDB.values()) {
            if (issuerInfo != null && issuerInfo.getIssuerType() == 2) {
                params.add(new RequestParam(issuerInfo.getProductId(), issuerInfo.getMode(), 11, issuerInfo.getIssuerId()));
            }
        }
        return params;
    }

    private static Set<Map<String, String>> convertParams(Set<RequestParam> params) {
        Set<Map<String, String>> filter = new HashSet<>();
        if (params == null) {
            return filter;
        }
        for (RequestParam p : params) {
            if (p != null) {
                filter.add(p.convert2Map());
            }
        }
        return filter;
    }

    private static String checkBalance(IssuerInfoItem mInfo) throws TrafficCardOperateException {
        AppletCardResult<CardInfo> appletCardResult = AppletInfoApiFactory.createAppletCardInfoReader(mContext).readTrafficCardInfo(mInfo.getAid(), mInfo.getProductId(), 2);
        if (appletCardResult.getResultCode() == 0) {
            return String.valueOf(appletCardResult.getData().getBalanceByFenUnit());
        }
        LogX.e("CloudTransferOutTrafficCardSAOperator transferOut, balance overdrawn. or read balance failed");
        TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(TransferOutTrafficCardCallback.RETURN_CARD_BALANCE_OVERDRAWN, TransferOutTrafficCardCallback.RETURN_CARD_BALANCE_OVERDRAWN, LoggerConstant.RESULT_CODE_TRANSFER_OUT_OTHER_FAIL, "CloudTransferOutTrafficCardSAOperator transferOut, balance overdrawn. or read balance failed", null);
        throw trafficCardOperateException;
    }
}
