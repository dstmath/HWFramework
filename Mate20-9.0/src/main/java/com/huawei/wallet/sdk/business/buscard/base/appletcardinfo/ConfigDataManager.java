package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.buscard.BuscardCloudTransferHelper;
import com.huawei.wallet.sdk.business.buscard.base.model.ApduSet;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.util.OmaUtil;
import com.huawei.wallet.sdk.common.dbmanager.CardProductInfoItem;
import com.huawei.wallet.sdk.common.dbmanager.CardProductInfoItemHelper;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ConfigDataManager implements ConfigData {
    private static final String APDU_QUERY_APPLET_VERSION = "8006DF1300";
    private static final String APDU_QUERY_CARD_NUM = "00B2011400";
    private static final String APDU_QUERY_LAST_TRANSACTION_INFO = "80CADF0200";
    private static final byte[] SYNC_LOCK = new byte[0];
    private static final String TAG = "ConfigData";
    private static volatile ConfigDataManager sInstance;
    private ApduSet currentApduSet;
    private Map<String, List<HciConfigInfo>> hciConfigDatas = new HashMap();
    private Map<String, List<ApduCommand>> localApduRepo = new HashMap();
    private Context mContext;
    private Map<String, ApduSet> oriApduDatas = new HashMap();

    private ConfigDataManager(Context context) {
        this.mContext = context;
    }

    public static ConfigDataManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SYNC_LOCK) {
                if (sInstance == null) {
                    sInstance = new ConfigDataManager(context);
                }
            }
        }
        return sInstance;
    }

    private void prepareApdus(String productId) throws AppletCardException {
        ApduSet set = this.oriApduDatas.get(productId);
        if (set == null) {
            parseJson2ApduSet(productId);
            set = this.oriApduDatas.get(productId);
        }
        this.currentApduSet = set;
    }

    public boolean isSameApduNumAndDate(String productId) throws AppletCardException {
        prepareApdus(productId);
        return this.currentApduSet.isSameApduNumAndDate();
    }

    public List<HciConfigInfo> getHciParseOperation(String productId) throws AppletCardException {
        List<HciConfigInfo> configData = this.hciConfigDatas.get(productId);
        if (configData != null) {
            return configData;
        }
        List<String> oriData = null;
        CardProductInfoItem itemInfo = BuscardCloudTransferHelper.getProductInfo(productId);
        if (itemInfo != null) {
            oriData = CardProductInfoItemHelper.getReservedNField(itemInfo);
        }
        List<HciConfigInfo> configData2 = new HciConfigDataParser(this.mContext, oriData).parseHciConfigData(productId);
        this.hciConfigDatas.put(productId, configData2);
        return configData2;
    }

    public List<ApduCommand> getApudList(String productId, int type) throws AppletCardException {
        LogX.d("ConfigData getApudList begin. productId : " + productId + " type : " + type);
        prepareApdus(productId);
        List<ApduCommand> apdus = null;
        switch (type) {
            case 0:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_STATUS);
                break;
            case 1:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_NUM);
                break;
            case 2:
                apdus = this.currentApduSet.getApduByType("date");
                break;
            case 3:
                apdus = this.currentApduSet.getApduByType("amount");
                break;
            case 4:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_RECORDS);
                break;
            case 6:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_C8_FILE_STATUS);
                break;
            case 7:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_IN_OUT_STATION_STATUS);
                break;
            case 10:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_RIDE_TIMES);
                break;
            case 11:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_CONSUME_RECORDS);
                break;
            case 12:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_LOGIC_NUM);
                break;
            case 13:
                apdus = this.currentApduSet.getApduByType(Constants.FIELD_APPLET_CONFIG_FM_CARD_INFO);
                break;
        }
        if (apdus != null || type == 0) {
            LogX.d("ConfigData getApudList end. productId : " + productId + " type : " + type);
            return apdus;
        }
        throw new AppletCardException(2, "apdu is null for card " + productId + " apdu type : " + type);
    }

    public List<ApduCommand> getLocalApudList(String aid, int type) throws AppletCardException {
        if (!StringUtil.isEmpty(aid, true)) {
            String k = aid + "_" + type;
            List<ApduCommand> apdus = this.localApduRepo.get(k);
            if (apdus != null) {
                return apdus;
            }
            if (5 == type) {
                apdus = new ArrayList<>();
                apdus.add(new ApduCommand(1, OmaUtil.getSelectApdu(aid), "9000"));
                apdus.add(new ApduCommand(2, APDU_QUERY_CARD_NUM, "9000"));
                this.localApduRepo.put(k, apdus);
            } else if (8 == type) {
                apdus = new ArrayList<>();
                apdus.add(new ApduCommand(1, OmaUtil.getSelectApdu(aid), "9000"));
                apdus.add(new ApduCommand(2, APDU_QUERY_APPLET_VERSION, "9000"));
                this.localApduRepo.put(k, apdus);
            } else if (9 == type) {
                apdus = new ArrayList<>();
                apdus.add(new ApduCommand(1, OmaUtil.getSelectApdu(aid), "9000"));
                apdus.add(new ApduCommand(2, APDU_QUERY_LAST_TRANSACTION_INFO, "9000"));
                this.localApduRepo.put(k, apdus);
            }
            return apdus;
        }
        throw new AppletCardException(1, " ConfigDataManager getLocalApudList param aid is null");
    }

    private void parseJson2ApduSet(String productId) throws AppletCardException {
        LogX.i("parseJson2ApduSet begin for " + productId);
        CardProductInfoItem infoItem = BuscardCloudTransferHelper.getProductInfo(productId);
        if (infoItem != null) {
            ApduSet set = new AppletInfoConfigDataParser(this.mContext, CardProductInfoItemHelper.getReservedNField(infoItem)).parseJson2ApduSet(productId);
            LogX.i("parseJson2ApduSet end for " + productId);
            this.oriApduDatas.put(productId, set);
            return;
        }
        throw new AppletCardException(AppletCardResult.RESULT_FAILED_INNER_EXCEPTION, "ConfigData parseJson2ApduSet product info for " + productId + " does not exists.");
    }

    public List<ApduCommand> getLocalApudStatus(String aid) throws AppletCardException {
        if (!StringUtil.isEmpty(aid, true)) {
            String k = aid;
            List<ApduCommand> apdus = this.localApduRepo.get(k);
            if (apdus != null) {
                return apdus;
            }
            List<ApduCommand> apdus2 = new ArrayList<>();
            apdus2.add(new ApduCommand(1, OmaUtil.getSelectApdu(aid), "9000"));
            this.localApduRepo.put(k, apdus2);
            return apdus2;
        }
        throw new AppletCardException(1, " ConfigDataManager getLocalApudList param aid is null");
    }
}
