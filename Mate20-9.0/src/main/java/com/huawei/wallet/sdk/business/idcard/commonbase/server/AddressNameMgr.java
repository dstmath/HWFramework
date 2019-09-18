package com.huawei.wallet.sdk.business.idcard.commonbase.server;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.constant.BankCardServerCmdConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea.OverSeasManager;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;
import com.huawei.wallet.sdk.common.http.service.ServerCmdConstant;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.PropertyUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddressNameMgr {
    private static final String BANKCARD_SERVER_URL = "https://bank-drcn.wallet.hicloud.com/WiseCloudWalletFinTechService/app/gateway";
    public static final String MODULE_NAME_BANKCARD = "BankCard";
    public static final String MODULE_NAME_TRANSPORTATIONCARD = "TransportationCard";
    public static final String MODULE_NAME_TSM = "TSM";
    public static final String MODULE_NAME_WALLET = "Wallet";
    public static final String MODULE_NAME_WALLETPASS = "WalletPass";
    public static final String MODULE_NAME_WISECLOUDVIRTUALCARD = "VirtualCard";
    public static final String PROP_NAME_BANKCARD = "ro.config.bank_card_server_sdk";
    public static final String PROP_NAME_LOCALE_REGION = "ro.product.locale.region";
    public static final String PROP_NAME_TRANSPORTATIONCARD = "ro.config.transportation_card_server";
    public static final String PROP_NAME_TSM = "ro.config.tsm_server_sdk";
    public static final String PROP_NAME_WALLET = "ro.config.wallet_server_sdk";
    public static final String PROP_NAME_WAllETPASS = "ro.config.wallet_pass_server_sdk";
    public static final String PROP_NAME_WISECLOUDVIRTUALCARD = "ro.config.virtual_card_server_sdk";
    public static final String SERVER_NAME_BANKCARD = "BANKCARD";
    public static final String SERVER_NAME_TRANSPORTATIONCARD = "TRANSPORTATIONCARD";
    public static final String SERVER_NAME_WALLET = "WALLET";
    public static final String SERVER_NAME_WAllETPASS = "WALLETPASS";
    public static final String SERVER_NAME_WISECLOUDVIRTUALCARD = "VIRTUALCARD";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TRANSPORTATION_SERVER_URL = "https://trans-drcn.wallet.hicloud.com/WalletTransCardService/app/gateway";
    public static final String TSMVERSIONS = "2.0.6";
    public static final String TSM_SERVER_URL = "https://tsm.hicloud.com:9001/TSMAPKP/HwTSMServer/applicationBusiness.action";
    public static final String VERSIONCODES = "801082040";
    private static final String WALLET_SERVER_URL = "https://nfcws.hicloud.com/Wallet/wallet/gateway.action";
    public static final String WAllETPASS_SERVER_URL = "https://pass-drcn.wallet.hicloud.com/WiseCloudWalletPassService/app/gateway";
    public static final String WISECLOUDVIRTUALCARD_SERVER_URL = "https://vcardmgt-drcn.wallet.hicloud.com/WiseCloudVirtualCardMgmtService/app/gateway";
    private static volatile AddressNameMgr instance;
    private Map<String, List<AddressName>> serverAddressCfgMap = new HashMap();

    private AddressNameMgr() {
    }

    public static AddressNameMgr getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new AddressNameMgr();
                }
            }
        }
        return instance;
    }

    public void initConfig(Context context) {
        LogC.i("Begin to initConfig", false);
        if (this.serverAddressCfgMap != null && this.serverAddressCfgMap.size() == 0) {
            List<AddressName> transportationCardList = new ArrayList<>();
            transportationCardList.add(new AddressName("nfc.set.card", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("post.event.cardenroll", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("query.rule.rf", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(ServerCmdConstant.CREATE_ORDER, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("download.install.app", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("delete.app", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("personalized", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(ServerCmdConstant.QUERY_ORDER, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("recharge", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("refund", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("get.rechargecard", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("if.cardmove.permitted", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("post.event.cardmove", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("get.event.cardmove", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("query.issuer.notice", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("post.event.recharge", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(ServerCmdConstant.CARDMOVE_OUT, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("cardmove.in", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("cardmove.report.cardnumber", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("query.service.mode", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("query.lingnantong.appcode", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("cutover", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("activated", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("get.apdu", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("nfc.get.paytype", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("query.rule.issuer", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("nfc.se.get.ssdaid", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_CREATE_SSD, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_INSTALL_APP, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_DEL_APP, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_DEL_SSD, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_LOCK_APP, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_UNLOCK_APP, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_INFO_INIT, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_UPDATE_APP, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_SYNC_INFO, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_UNLOCK_ESE, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("get.refundorder", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("nfc.transcard.remove.check", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("nfc.transcard.backup", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName("nfc.transcard.restore", SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            transportationCardList.add(new AddressName(ServerCmdConstant.QUERY_ORDER_RESULT, SERVER_NAME_TRANSPORTATIONCARD, MODULE_NAME_TRANSPORTATIONCARD));
            this.serverAddressCfgMap.put(MODULE_NAME_TRANSPORTATIONCARD, transportationCardList);
            List<AddressName> bankCardList = new ArrayList<>();
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_CREATE_SSD, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_INSTALL_APP, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_DEL_APP, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_DEL_SSD, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_LOCK_APP, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_UNLOCK_APP, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_INFO_INIT, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_UPDATE_APP, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_SYNC_INFO, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(TsmOperationConstant.TASK_COMMANDER_UNLOCK_ESE, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("download.install.app", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("delete.app", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("query.rule.issuer", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("personalized", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.set.card", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.checkCard", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.cardRoll", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.get.verification", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.verifing", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.delete.card", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.get.products", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(BankCardServerCmdConstant.QUERY_AID_CMD, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.get.uShieldIssuers", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName(BankCardServerCmdConstant.QUERY_UNION_PAY_PUSH_CMD, SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("post.bankcards.queue", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("wipe.device", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("verify.sign", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("post.event.cardenroll", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.apdu", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.creditCards", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.bankcards.queue", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.get.pan", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.enroll.pan", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.provision.token", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.confirm.prov", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.get.step.up.option", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.req.validate.code", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.validate", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.get.trans.his", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("visa.get.metaData", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("refund.action", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("queryAccount.action", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("coin.query.account", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("onlinePay.sign.info", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("loan.get.company", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("quickPass.get.company", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.qrCode.apply", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("onlinePay.queryMerchants", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.encrypt.info", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.bankCardInfo", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("nfc.get.insideAuthUrl", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("onlinePay.getPayOrder", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("onlinePay.queryOrder", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.alipay.userinfo", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("post.events.qr", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.applications", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("get.dics", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            bankCardList.add(new AddressName("query.trans.history", SERVER_NAME_BANKCARD, MODULE_NAME_BANKCARD));
            this.serverAddressCfgMap.put(MODULE_NAME_BANKCARD, bankCardList);
            List<AddressName> walletList = new ArrayList<>();
            walletList.add(new AddressName("query.huaweipay.usability", "WALLET", MODULE_NAME_WALLET));
            walletList.add(new AddressName("nfc.get.issuers", "WALLET", MODULE_NAME_WALLET));
            this.serverAddressCfgMap.put(MODULE_NAME_WALLET, walletList);
            List<AddressName> virtualCardList = new ArrayList<>();
            virtualCardList.add(new AddressName(ServerCmdConstant.GET_RANDOM_CMD, SERVER_NAME_WISECLOUDVIRTUALCARD, MODULE_NAME_WISECLOUDVIRTUALCARD));
            virtualCardList.add(new AddressName("nfc.se.reset", SERVER_NAME_WISECLOUDVIRTUALCARD, MODULE_NAME_WISECLOUDVIRTUALCARD));
            virtualCardList.add(new AddressName(ServerCmdConstant.DIPLOMA_UPLOAD, SERVER_NAME_WISECLOUDVIRTUALCARD, MODULE_NAME_WISECLOUDVIRTUALCARD));
            this.serverAddressCfgMap.put(MODULE_NAME_WISECLOUDVIRTUALCARD, virtualCardList);
            List<AddressName> walletPassList = new ArrayList<>();
            walletPassList.add(new AddressName("delete.app", "WALLETPASS", MODULE_NAME_WALLETPASS));
            walletPassList.add(new AddressName("get.apdu", "WALLETPASS", MODULE_NAME_WALLETPASS));
            walletPassList.add(new AddressName("query.pass.type", "WALLETPASS", MODULE_NAME_WALLETPASS));
            this.serverAddressCfgMap.put(MODULE_NAME_WALLETPASS, walletPassList);
            LogC.i("End to initConfig, map size=" + this.serverAddressCfgMap.size(), false);
        }
    }

    public String getAddress(String cmdName, String moduleName, Map<String, String> map, Context mContext) {
        initConfig(mContext);
        String serverUrl = "";
        if (TextUtils.equals(moduleName, MODULE_NAME_TRANSPORTATIONCARD)) {
            String serverAddressName = getAddressName(cmdName, MODULE_NAME_TRANSPORTATIONCARD, null);
            serverUrl = getGrsUrlSync(mContext, serverAddressName) + "?clientVersion=" + VERSIONCODES;
        } else if (TextUtils.equals(moduleName, MODULE_NAME_BANKCARD)) {
            String serverAddressName2 = getAddressName(cmdName, MODULE_NAME_BANKCARD, null);
            serverUrl = getGrsUrlSync(mContext, serverAddressName2) + "?clientVersion=" + VERSIONCODES;
        } else if (TextUtils.equals(moduleName, MODULE_NAME_WISECLOUDVIRTUALCARD)) {
            String serverAddressName3 = getAddressName(cmdName, MODULE_NAME_WISECLOUDVIRTUALCARD, null);
            serverUrl = getGrsUrlSync(mContext, serverAddressName3) + "?clientVersion=" + VERSIONCODES;
        } else if (TextUtils.equals(moduleName, MODULE_NAME_WALLETPASS)) {
            String serverAddressName4 = getAddressName(cmdName, MODULE_NAME_WALLETPASS, null);
            serverUrl = getGrsUrlSync(mContext, serverAddressName4) + "?clientVersion=" + VERSIONCODES;
        } else if (TextUtils.equals(moduleName, MODULE_NAME_WALLET)) {
            String serverAddressName5 = getAddressName(cmdName, MODULE_NAME_WALLET, null);
            serverUrl = getGrsUrlSync(mContext, serverAddressName5) + "?clientVersion=" + VERSIONCODES;
        } else if (TextUtils.equals(moduleName, "TSM")) {
            String serverAddressName6 = getAddressName(cmdName, "TSM", null);
            serverUrl = getGrsUrlSync(mContext, serverAddressName6) + "?version=" + "2.0.6";
        }
        LogC.d("AddressNameMgr,serverUrl = " + serverUrl, true);
        return serverUrl;
    }

    public String getAddress(String module, Context mContext) {
        String serverUrl;
        initConfig(mContext);
        if (TextUtils.equals(module, "TSM")) {
            serverUrl = getGrsUrlSync(mContext, module) + "?version=" + "2.0.6";
        } else {
            serverUrl = getGrsUrlSync(mContext, module) + "?clientVersion=" + VERSIONCODES;
        }
        LogC.d("AddressNameMgr,getGrsUrlSync(),serverUrl=" + serverUrl, true);
        return serverUrl;
    }

    public static String getGrsUrlSync(Context context, String moduleServerName) {
        String grsUrl = OverSeasManager.getInstance(context).getGrsUrlSync(moduleServerName);
        if (TextUtils.isEmpty(grsUrl)) {
            if (!"CN".equalsIgnoreCase(OverSeasManager.getInstance(context).getCountryCodeFromTA())) {
                return null;
            }
            if (TextUtils.equals(moduleServerName, SERVER_NAME_TRANSPORTATIONCARD)) {
                grsUrl = PropertyUtils.getProperty(PROP_NAME_TRANSPORTATIONCARD, TRANSPORTATION_SERVER_URL);
            } else if (TextUtils.equals(moduleServerName, SERVER_NAME_BANKCARD)) {
                grsUrl = PropertyUtils.getProperty(PROP_NAME_BANKCARD, BANKCARD_SERVER_URL);
            } else if (TextUtils.equals(moduleServerName, SERVER_NAME_WISECLOUDVIRTUALCARD)) {
                grsUrl = PropertyUtils.getProperty(PROP_NAME_WISECLOUDVIRTUALCARD, WISECLOUDVIRTUALCARD_SERVER_URL);
            } else if (TextUtils.equals(moduleServerName, "WALLETPASS")) {
                grsUrl = PropertyUtils.getProperty(PROP_NAME_WAllETPASS, WAllETPASS_SERVER_URL);
            } else if (TextUtils.equals(moduleServerName, "WALLET")) {
                grsUrl = PropertyUtils.getProperty(PROP_NAME_WALLET, WALLET_SERVER_URL);
            } else if (TextUtils.equals(moduleServerName, "TSM")) {
                grsUrl = PropertyUtils.getProperty(PROP_NAME_TSM, TSM_SERVER_URL);
            } else {
                LogC.i("AddressNameMgr,getGrsUrlSync(),not transportation or bank card.", false);
            }
        }
        return grsUrl;
    }

    public String getAddressName(String addressName, String moduleName, Map<String, String> conditionMap) {
        LogC.i("Begin to getAddressName", false);
        String serverAddressName = null;
        Map<String, List<AddressName>> addressNameCfgMap = getServerAddressCfgMap();
        if (addressNameCfgMap.containsKey(moduleName)) {
            serverAddressName = getServerAddressName(addressName, conditionMap, addressNameCfgMap.get(moduleName));
        }
        if (serverAddressName == null) {
            serverAddressName = getServerAddressName(addressName, conditionMap, addressNameCfgMap.get("Default"));
        }
        if (serverAddressName == null) {
            return addressName;
        }
        LogC.i("End to getAddressName", false);
        return serverAddressName;
    }

    private String getServerAddressName(String addressName, Map<String, String> conditionMap, List<AddressName> addressNameCfgList) {
        LogC.i("Begin to getServerAddressName", false);
        if (addressNameCfgList == null || addressName == null) {
            return null;
        }
        for (AddressName addressNameCfg : addressNameCfgList) {
            if (addressName.equalsIgnoreCase(addressNameCfg.getAddressName())) {
                if (conditionMap == null) {
                    return addressNameCfg.getServerAddressName();
                }
                if (addressNameCfg.conditionMatch(conditionMap)) {
                    return addressNameCfg.getServerAddressName();
                }
            }
        }
        return null;
    }

    private Map<String, List<AddressName>> getServerAddressCfgMap() {
        initConfig(null);
        return this.serverAddressCfgMap;
    }
}
