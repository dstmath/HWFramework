package com.huawei.wallet.sdk.business.buscard.cloudtransferout.serveraccess;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.modle.IssuerInfoItem;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletInfoApiFactory;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.spi.ServerAccessOperatorUtils;
import com.huawei.wallet.sdk.business.buscard.spi.SPIServiceFactory;
import com.huawei.wallet.sdk.business.buscard.task.TrafficCardOperateException;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.account.NFCAccountManager;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.logger.LoggerConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.ese.ESEApiFactory;
import com.huawei.wallet.sdk.common.apdu.request.DeleteAppletRequest;
import com.huawei.wallet.sdk.common.apdu.response.DeleteAppletResponse;
import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import com.huawei.wallet.sdk.common.ta.WalletTaException;
import com.huawei.wallet.sdk.common.ta.WalletTaManager;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.device.PhoneDeviceUtil;
import java.util.HashMap;
import java.util.Map;

public class UninstallTrafficCardSAOperator {
    private Context mContext;
    private String mFlag;
    private IssuerInfoItem mInfo;
    private boolean mIsOnlyDeleteApplet;
    private String mOrderNo;
    private boolean mUpdateTA;

    public UninstallTrafficCardSAOperator(Context context, IssuerInfoItem info, boolean updateTA, boolean isOnlyDeleteApplet) {
        this.mContext = context;
        this.mInfo = info;
        this.mUpdateTA = updateTA;
        this.mIsOnlyDeleteApplet = isOnlyDeleteApplet;
    }

    public UninstallTrafficCardSAOperator(Context context, IssuerInfoItem info, boolean updateTA, boolean isOnlyDeleteApplet, String orderNo, String flag) {
        this.mContext = context;
        this.mInfo = info;
        this.mUpdateTA = updateTA;
        this.mIsOnlyDeleteApplet = isOnlyDeleteApplet;
        this.mOrderNo = orderNo;
        this.mFlag = flag;
    }

    public boolean uninstall(String source, String reason, String reasonCode, String accountType, String account) throws TrafficCardOperateException {
        String str = source;
        String str2 = reason;
        String aid = this.mInfo.getAid();
        if (!StringUtil.isEmpty(aid, true)) {
            LogX.i("SAUninstall has started, the reason is : " + str2);
            String cplc = ESEApiFactory.createESEInfoManagerApi(this.mContext).queryCplc();
            String str3 = cplc;
            String str4 = aid;
            DeleteAppletRequest request = new DeleteAppletRequest(this.mInfo.getIssuerId(), str3, str4, Build.MODEL, ProductConfigUtil.geteSEManufacturer());
            request.setAccountUserId(NFCAccountManager.getAccountUserId());
            request.setSn(PhoneDeviceUtil.getSerialNumber());
            request.setSource(str);
            request.setReason(reasonCode);
            request.setRefundAccountType(accountType);
            request.setRefundAccountNumber(account);
            setRequestInfo(aid, request, str);
            request.setOnlyDeleteApplet(this.mIsOnlyDeleteApplet);
            request.setFlag(this.mFlag);
            request.setOrderNo(this.mOrderNo);
            DeleteAppletResponse response = SPIServiceFactory.createServerAccessService(this.mContext).deleteApplet(request);
            if (response.getResultCode() == 0 || response.getResultCode() == 5002) {
                LogX.i("UninstallTrafficCardSAOperator uninstall success!");
                if (!this.mUpdateTA) {
                    return true;
                }
                return updateTaAndReport(aid, str2);
            }
            handleErr(response.getResultCode(), response.getResultDesc(), response.getErrorInfo());
            return false;
        }
        String str5 = reasonCode;
        String str6 = accountType;
        String str7 = account;
        TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(10, 10, LoggerConstant.RESULT_CODE_DELETE_TRAFFIC_CARD_FAILED, "UninstallTrafficCardSAOperator uninstall failed. aid is illegal.", null);
        throw trafficCardOperateException;
    }

    private void handleErr(int returnCode, String returnDesc, ErrorInfo errorInfo) throws TrafficCardOperateException {
        int result;
        if (returnCode != 10002) {
            switch (returnCode) {
                case 1:
                    result = 10;
                    break;
                case 2:
                    result = 11;
                    break;
                case 3:
                    result = 25;
                    break;
                default:
                    result = 99;
                    break;
            }
        } else {
            result = 2001;
        }
        LogX.e("UninstallTrafficCardSAOperator, returnCode=" + returnCode + ", result=" + result, false);
        TrafficCardOperateException trafficCardOperateException = new TrafficCardOperateException(result, result, LoggerConstant.RESULT_CODE_DELETE_TRAFFIC_CARD_FAILED, returnDesc, (String) null, errorInfo);
        throw trafficCardOperateException;
    }

    private void setRequestInfo(String aid, DeleteAppletRequest request, String source) {
        request.setAppCode(this.mInfo.getCityCode());
        AppletCardResult<CardInfo> result = AppletInfoApiFactory.createAppletCardInfoReader(this.mContext).readTrafficCardInfo(aid, this.mInfo.getProductId(), 3);
        if (result.getResultCode() == 0) {
            LogX.i("UninstallTrafficCardSAOperator, readTrafficCardInfo successs.");
            request.setTrafficCardId(result.getData().getCardNum());
            int amount = result.getData().getBalanceByFenUnit();
            if (amount < 0) {
                LogX.i("cardBalance error: " + amount);
            }
            String cardBalance = ServerAccessOperatorUtils.getInstance().encodeCardBalance(amount, PhoneDeviceUtil.getUDID812(this.mContext), PhoneDeviceUtil.getDeviceID(this.mContext), PhoneDeviceUtil.getAnotherDeviceId(this.mContext), this.mContext.getPackageName());
            if (!TextUtils.isEmpty(cardBalance)) {
                request.setCardBalance(cardBalance);
                return;
            }
            return;
        }
        LogX.i("UninstallTrafficCardSAOperator, readTrafficCardInfo err. Code : " + result.getResultCode());
        TACardInfo taCardInfo = WalletTaManager.getInstance(this.mContext).getCard(aid);
        if (taCardInfo != null && 2 == taCardInfo.getCardStatus()) {
            request.setTrafficCardId(taCardInfo.getFpanFour());
        }
    }

    private boolean updateTaAndReport(String aid, String reason) {
        try {
            WalletTaManager.getInstance(this.mContext).removeCardByAid(aid);
            LogX.i("(SA)Delete card in TA success");
            return true;
        } catch (WalletTaException.WalletTaCardNotExistException e) {
            LogX.w("UninstallTrafficCardTask updateTaAndReport WalletTaCardNotExistException, ta removeCard failed", (Throwable) e);
            return true;
        } catch (WalletTaException.WalletTaSystemErrorException e2) {
            LogX.w("UninstallTrafficCardTask updateTaAndReport WalletTaSystemErrorException, ta removeCard failed", (Throwable) e2);
            return false;
        }
    }

    private void reportErr(int resultCode, String resultDesc) {
        Map<String, String> params = new HashMap<>();
        params.put("fail_reason", resultDesc);
        params.put("fail_code", String.valueOf(resultCode));
        params.put("issuerID", String.valueOf(this.mInfo.getIssuerId()));
    }
}
