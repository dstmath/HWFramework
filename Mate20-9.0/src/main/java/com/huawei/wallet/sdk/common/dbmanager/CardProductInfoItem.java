package com.huawei.wallet.sdk.common.dbmanager;

import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.buscard.base.util.MoneyUtil;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.CardProductInfoServerItem;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class CardProductInfoItem {
    private String area;
    private int cardNumLength;
    private String[] commonRechargeAmounts;
    private String description;
    private String fontColor;
    private int fullCardNoLength;
    private boolean isSupportTransferInRecharge;
    private String issueCardActCd;
    private int issueCardDiscountCost = -1;
    private String[] issueCardRechargeAmounts;
    private int issueCardStdCost;
    private String issuerId;
    private String mktInfo;
    private int moveCost;
    private String moveInActCd;
    private String moveOutManageCd;
    private String pictureUrl;
    private String productId;
    private String productName;
    private String rechargeActCd;
    private String[] rechargeDiscountAmounts;
    private String reserved1;
    private String reserved2;
    private String reserved3;
    private String reserved4;
    private String reserved5;
    private String reserved6;
    private String reservedInfo;
    private String snbCardId;
    private String snbCityBusCode;
    private boolean supportQueryC8FileStatus;
    private boolean supportQueryInOutStationStatus;
    private boolean supportQueryRideTimes;
    private int supportTransferVersionCode;
    private long timeStamp;
    private int type;
    private String version;

    public CardProductInfoItem() {
    }

    public CardProductInfoItem(CardProductInfoServerItem item) {
        this.productId = item.getProductId();
        this.productName = item.getProductName();
        this.pictureUrl = item.getPictureUrl();
        this.description = item.getDescription();
        this.type = item.getType();
        this.timeStamp = item.getTimeStamp();
        this.version = item.getVersion();
        this.issuerId = item.getIssuerId();
        this.mktInfo = item.getMktInfo();
        this.reservedInfo = item.getReservedInfo();
        this.area = item.getArea();
        this.fontColor = item.getFontColor();
        this.reserved1 = item.getReserved1();
        this.reserved2 = item.getReserved2();
        this.reserved3 = item.getReserved3();
        this.reserved4 = item.getReserved4();
        this.reserved5 = item.getReserved5();
        this.reserved6 = item.getReserved6();
        parseMktInfoJson();
        parseReservedJson();
        setSupportQueryStatusInfo();
    }

    public void parseMktInfoJson() {
        try {
            if (!StringUtil.isEmpty(this.mktInfo, true)) {
                JSONObject jo = new JSONObject(this.mktInfo);
                if (jo.has("issuecard_act_cd")) {
                    this.issueCardActCd = jo.getString("issuecard_act_cd");
                }
                if (jo.has("issuecard_discount_cost")) {
                    this.issueCardDiscountCost = MoneyUtil.convertYuanToFen(jo.getString("issuecard_discount_cost"));
                }
                if (jo.has("recharge_act_cd")) {
                    this.rechargeActCd = jo.getString("recharge_act_cd");
                }
                if (jo.has("recharge_discount_amounts")) {
                    String discountAmounts = jo.getString("recharge_discount_amounts");
                    if (!StringUtil.isEmpty(discountAmounts, true)) {
                        this.rechargeDiscountAmounts = discountAmounts.split(",");
                    }
                }
                if (jo.has("move_out_manage_cd")) {
                    this.moveOutManageCd = jo.getString("move_out_manage_cd");
                }
                if (jo.has("move_in_act_cd")) {
                    this.moveInActCd = jo.getString("move_in_act_cd");
                }
                if (jo.has("move_cost")) {
                    this.moveCost = MoneyUtil.convertYuanToFen(jo.getString("move_cost"));
                }
            }
        } catch (JSONException e) {
            LogX.e("parseMktInfoJson : " + e.getMessage(), true);
        }
    }

    public void parseReservedJson() {
        try {
            if (!StringUtil.isEmpty(this.reservedInfo, true)) {
                JSONObject jo = new JSONObject(this.reservedInfo);
                if (jo.has("new_issuecard_recharge")) {
                    String issueCardRecharge = jo.getString("new_issuecard_recharge");
                    if (!StringUtil.isEmpty(issueCardRecharge, true)) {
                        this.issueCardRechargeAmounts = issueCardRecharge.split(",");
                    }
                }
                if (jo.has("common_recharge")) {
                    String commonRecharge = jo.getString("common_recharge");
                    if (!StringUtil.isEmpty(commonRecharge, true)) {
                        this.commonRechargeAmounts = commonRecharge.split(",");
                    }
                }
                if (jo.has("issuecard_std_cost")) {
                    int amount = MoneyUtil.convertYuanToFen(jo.getString("issuecard_std_cost"));
                    this.issueCardStdCost = amount < 0 ? 0 : amount;
                }
                LogX.d("CardProductInfoItem--issueCardDiscountCost:" + this.issueCardDiscountCost);
                if (jo.has(SNBConstant.FIELD_CARD_ID)) {
                    String cardId = jo.getString(SNBConstant.FIELD_CARD_ID);
                    if (!StringUtil.isEmpty(cardId, true)) {
                        this.snbCardId = cardId;
                    }
                }
                if (jo.has(SNBConstant.FIELD_CITY_BUS_CODE)) {
                    String busCode = jo.getString(SNBConstant.FIELD_CITY_BUS_CODE);
                    if (!StringUtil.isEmpty(busCode, true)) {
                        this.snbCityBusCode = busCode;
                    }
                }
                if (jo.has("card_no_length")) {
                    this.fullCardNoLength = jo.getInt("card_no_length");
                }
                if (jo.has("cardnum_length")) {
                    this.cardNumLength = jo.getInt("cardnum_length");
                }
                parseTransferCfg(jo);
            }
        } catch (JSONException e) {
            LogX.e("parseReservedJson : " + e.getMessage(), true);
        }
    }

    private void parseTransferCfg(JSONObject jo) throws JSONException {
        if (jo.has("transfer_cfg")) {
            JSONObject transferCfg = jo.getJSONObject("transfer_cfg");
            if (transferCfg.has("phone")) {
                this.supportTransferVersionCode = transferCfg.getInt("phone");
            }
            if (transferCfg.has("support_transferIn_recharge")) {
                this.isSupportTransferInRecharge = transferCfg.getBoolean("support_transferIn_recharge");
            } else {
                this.isSupportTransferInRecharge = false;
            }
        }
    }

    public void setSupportQueryStatusInfo() {
        for (String s : CardProductInfoItemHelper.getReservedNField(this)) {
            if (s.contains(Constants.FIELD_APPLET_CONFIG_C8_FILE_STATUS)) {
                LogX.d("CardProductInfoItem--setSupportQueryStatusInfo, contians c8 file status. productId = " + this.productId + ", s = " + s);
                setSupportQueryC8FileStatus(true);
            }
            if (s.contains(Constants.FIELD_APPLET_CONFIG_IN_OUT_STATION_STATUS)) {
                LogX.d("CardProductInfoItem--setSupportQueryStatusInfo, contians in out station status. productId = " + this.productId + ", s = " + s);
                setSupportQueryInOutStationStatus(true);
            }
            if (s.contains(Constants.FIELD_APPLET_CONFIG_RIDE_TIMES)) {
                LogX.d("CardProductInfoItem--setSupportQueryStatusInfo, contians in ride times. productId = " + this.productId + ", s = " + s);
                setSupportQueryRideTimes(true);
            }
        }
    }

    public Boolean isSupportQueryC8FileStatus() {
        return Boolean.valueOf(this.supportQueryC8FileStatus);
    }

    public Boolean isSupportQueryInOutStationStatus() {
        return Boolean.valueOf(this.supportQueryInOutStationStatus);
    }

    private void setSupportQueryC8FileStatus(boolean supportQueryC8FileStatus2) {
        this.supportQueryC8FileStatus = supportQueryC8FileStatus2;
    }

    public String getProductId() {
        return this.productId;
    }

    private void setSupportQueryInOutStationStatus(boolean supportQueryInOutStationStatus2) {
        this.supportQueryInOutStationStatus = supportQueryInOutStationStatus2;
    }

    public void setProductId(String productId2) {
        this.productId = productId2;
    }

    public String getProductName() {
        return this.productName;
    }

    public String getPictureUrl() {
        return this.pictureUrl;
    }

    public void setProductName(String productName2) {
        this.productName = productName2;
    }

    public void setPictureUrl(String pictureUrl2) {
        this.pictureUrl = pictureUrl2;
    }

    public void setDescription(String description2) {
        this.description = description2;
    }

    public String getDescription() {
        return this.description;
    }

    public int getType() {
        return this.type;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public void setTimeStamp(long timeStamp2) {
        this.timeStamp = timeStamp2;
    }

    public void setVersion(String version2) {
        this.version = version2;
    }

    public String getVersion() {
        return this.version;
    }

    public String getIssuerId() {
        return this.issuerId;
    }

    public void setIssuerId(String issuerId2) {
        this.issuerId = issuerId2;
    }

    public void setMktInfo(String mktInfo2) {
        this.mktInfo = mktInfo2;
    }

    public void setReservedInfo(String reservedInfo2) {
        this.reservedInfo = reservedInfo2;
    }

    public String getIssueCardActCd() {
        return this.issueCardActCd;
    }

    public int getIssueCardDiscountCost() {
        return this.issueCardDiscountCost;
    }

    public int getIssueCardStdCost() {
        return this.issueCardStdCost;
    }

    public String getRechargeActCd() {
        return this.rechargeActCd;
    }

    public String getSnbCardId() {
        return this.snbCardId;
    }

    public String getSnbCityBusCode() {
        return this.snbCityBusCode;
    }

    public String getMktInfo() {
        return this.mktInfo;
    }

    public String getReservedInfo() {
        return this.reservedInfo;
    }

    public String[] getIssueCardRechargeAmounts() {
        if (this.issueCardRechargeAmounts == null) {
            return null;
        }
        return (String[]) this.issueCardRechargeAmounts.clone();
    }

    public String[] getCommonRechargeAmounts() {
        if (this.commonRechargeAmounts == null) {
            return null;
        }
        return (String[]) this.commonRechargeAmounts.clone();
    }

    public String[] getRechargeDiscountAmounts() {
        if (this.rechargeDiscountAmounts == null) {
            return null;
        }
        return (String[]) this.rechargeDiscountAmounts.clone();
    }

    public String getFontColor() {
        return this.fontColor;
    }

    public void setFontColor(String fontColor2) {
        this.fontColor = fontColor2;
    }

    public int getFullCardNoLength() {
        return this.fullCardNoLength;
    }

    public void setFullCardNoLength(int fullCardNoLength2) {
        this.fullCardNoLength = fullCardNoLength2;
    }

    public int getCardNumLength() {
        return this.cardNumLength;
    }

    public void setCardNumLength(int cardNumLength2) {
        this.cardNumLength = cardNumLength2;
    }

    public void setReserved1(String reserved12) {
        this.reserved1 = reserved12;
    }

    public String getReserved1() {
        return this.reserved1;
    }

    public String getReserved2() {
        return this.reserved2;
    }

    public void setReserved2(String reserved22) {
        this.reserved2 = reserved22;
    }

    public String getReserved3() {
        return this.reserved3;
    }

    public String getReserved4() {
        return this.reserved4;
    }

    public void setReserved3(String reserved32) {
        this.reserved3 = reserved32;
    }

    public void setReserved4(String reserved42) {
        this.reserved4 = reserved42;
    }

    public String getReserved5() {
        return this.reserved5;
    }

    public void setReserved5(String reserved52) {
        this.reserved5 = reserved52;
    }

    public String getReserved6() {
        return this.reserved6;
    }

    public void setReserved6(String reserved62) {
        this.reserved6 = reserved62;
    }

    public String getArea() {
        return this.area;
    }

    public void setArea(String area2) {
        this.area = area2;
    }

    public String getMoveInActCd() {
        return this.moveInActCd;
    }

    public String getMoveOutManageCd() {
        return this.moveOutManageCd;
    }

    public int getMoveCost() {
        return this.moveCost;
    }

    public int getSupportTransferVersionCode() {
        return this.supportTransferVersionCode;
    }

    public void setSupportTransferVersionCode(int supportTransferVersionCode2) {
        this.supportTransferVersionCode = supportTransferVersionCode2;
    }

    public boolean isSupportTransferInRecharge() {
        return this.isSupportTransferInRecharge;
    }

    public void setSupportTransferInRecharge(boolean supportTransferInRecharge) {
        this.isSupportTransferInRecharge = supportTransferInRecharge;
    }

    public void setSupportQueryRideTimes(boolean supportQueryRideTimes2) {
        this.supportQueryRideTimes = supportQueryRideTimes2;
    }

    public boolean isSupportQueryRideTimes() {
        return this.supportQueryRideTimes;
    }
}
