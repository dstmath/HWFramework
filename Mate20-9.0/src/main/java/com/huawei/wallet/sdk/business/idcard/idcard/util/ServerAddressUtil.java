package com.huawei.wallet.sdk.business.idcard.idcard.util;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressName;
import com.huawei.wallet.sdk.business.idcard.idcard.constant.ServerAddressConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.oversea.OverSeasManager;
import com.huawei.wallet.sdk.common.apdu.properties.WalletSystemProperties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerAddressUtil {
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String VERSION = "v1";
    private static volatile ServerAddressUtil instance;
    private Map<String, List<AddressName>> serverAddressCfgMap;

    public static ServerAddressUtil getInstance() {
        if (instance == null) {
            synchronized (SYNC_LOCK) {
                if (instance == null) {
                    instance = new ServerAddressUtil();
                }
            }
        }
        return instance;
    }

    private static String getUrlForEid(Context context, String moduleServerName) {
        String grsUrl = OverSeasManager.getInstance(context).getGrsUrlSync(moduleServerName);
        if (TextUtils.isEmpty(grsUrl)) {
            return WalletSystemProperties.getInstance().getProperty("URL_EID_CARD_SERVER", "https://identity-drcn.wallet.hicloud.com/WalletIdentityService/api");
        }
        return grsUrl;
    }

    private static String getUrlForCtid(Context context, String moduleServerName) {
        String grsUrl = OverSeasManager.getInstance(context).getGrsUrlSync(moduleServerName);
        if (TextUtils.isEmpty(grsUrl)) {
            return WalletSystemProperties.getInstance().getProperty("URL_CTID_CARD_SERVER", "https://ctid-drcn.wallet.hicloud.com/WalletIdentityService/api");
        }
        return grsUrl;
    }

    private void initConfig(Context context) {
        this.serverAddressCfgMap = new HashMap();
        List<AddressName> eidCardList = new ArrayList<>();
        eidCardList.add(new AddressName("idrandomnum", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("bind", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("readotherdevcard", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("appeidcode", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("checkfaceinfo", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("activeeidcard", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("activerstrpt", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName(ServerAddressConstant.IDCARD_CMD_CANCELEID, ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName(ServerAddressConstant.IDCARD_CMD_READCARDLIST, ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("app", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("personalization", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("apdu", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName(ServerAddressConstant.IDCARD_CMD_DELAPP, ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        eidCardList.add(new AddressName("paraauth", ServerAddressConstant.EID_CARD_MODULE_NAME, ServerAddressConstant.EID_CARD_MODULE_NAME));
        this.serverAddressCfgMap.put(ServerAddressConstant.EID_CARD_MODULE_NAME, eidCardList);
        List<AddressName> ctidCardList = new ArrayList<>();
        ctidCardList.add(new AddressName("idrandomnum", ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        ctidCardList.add(new AddressName("bind", ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        ctidCardList.add(new AddressName(ServerAddressConstant.IDCARD_CMD_READCARDLIST, ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        ctidCardList.add(new AddressName("app", ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        ctidCardList.add(new AddressName("personalization", ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        ctidCardList.add(new AddressName("apdu", ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        ctidCardList.add(new AddressName(ServerAddressConstant.IDCARD_CMD_DELAPP, ServerAddressConstant.CTID_CARD_MODULE_NAME, ServerAddressConstant.CTID_CARD_MODULE_NAME));
        this.serverAddressCfgMap.put(ServerAddressConstant.CTID_CARD_MODULE_NAME, ctidCardList);
    }

    public String getAddress(String cmdName, String moduleName, Map<String, String> conditionMap, Context context) {
        return getAddress(cmdName, moduleName, conditionMap, context, getUrlParam(context, moduleName));
    }

    private String getAddress(String cmdName, String moduleName, Map<String, String> map, Context mContext, String urlParam) {
        if (instance.getServerAddressCfgMap() == null) {
            initConfig(mContext);
        }
        if (TextUtils.equals(moduleName, ServerAddressConstant.EID_CARD_MODULE_NAME)) {
            String serverAddressName = getAddressName(cmdName, ServerAddressConstant.EID_CARD_MODULE_NAME, null);
            if (TextUtils.isEmpty(serverAddressName)) {
                return "";
            }
            return getUrlForEid(mContext, serverAddressName) + "/" + VERSION + "/" + cmdName + urlParam;
        } else if (!TextUtils.equals(moduleName, ServerAddressConstant.CTID_CARD_MODULE_NAME)) {
            return "";
        } else {
            String serverAddressName2 = getAddressName(cmdName, ServerAddressConstant.CTID_CARD_MODULE_NAME, null);
            if (TextUtils.isEmpty(serverAddressName2)) {
                return "";
            }
            return getUrlForCtid(mContext, serverAddressName2) + "/" + VERSION + "/" + cmdName + urlParam;
        }
    }

    private String getUrlParam(Context context, String moduleName) {
        if (!ServerAddressConstant.CTID_CARD_MODULE_NAME.equals(moduleName) || !WalletSystemProperties.getInstance().getProperty("ENVIRONMENT", "product").equals("develop")) {
            return "?clientVer=801082040";
        }
        return "?clientVer=2.0";
    }

    public String getAddressName(String addressName, String moduleName, Map<String, String> conditionMap) {
        String serverAddressName = null;
        Map<String, List<AddressName>> addressNameCfgMap = getServerAddressCfgMap();
        if (addressNameCfgMap.containsKey(moduleName)) {
            serverAddressName = getServerAddressName(addressName, conditionMap, addressNameCfgMap.get(moduleName));
        }
        if (serverAddressName == null) {
            serverAddressName = getServerAddressName(addressName, conditionMap, addressNameCfgMap.get("Default"));
        }
        return serverAddressName == null ? addressName : serverAddressName;
    }

    private String getServerAddressName(String addressName, Map<String, String> conditionMap, List<AddressName> addressNameCfgList) {
        if (addressNameCfgList == null || addressName == null) {
            return null;
        }
        for (AddressName addressNameCfg : addressNameCfgList) {
            if (addressName.equalsIgnoreCase(addressNameCfg.getAddressName()) && addressNameCfg.conditionMatch(conditionMap)) {
                return addressNameCfg.getServerAddressName();
            }
        }
        return null;
    }

    private Map<String, List<AddressName>> getServerAddressCfgMap() {
        return this.serverAddressCfgMap;
    }
}
