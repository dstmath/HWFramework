package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.buscard.base.model.UnionPayInfo;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.ApplyOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.QueryOrder;
import com.huawei.wallet.sdk.business.buscard.base.spi.model.TransferOrder;
import java.util.List;

public class TrafficOrder extends HianalyticsBaseRespInfo {
    public static final String REPORT_ORDER_TYPE_RECHARGE = "order_type_recharge";
    private List<ApplyOrder> applyOrders;
    private int balance;
    private String cityCode;
    private boolean hasUnusedIssueOrder;
    private String hwOrderNo;
    private boolean isDuplicateApply;
    private boolean isNewPayVersion = false;
    private TransferOrder mTransferOrder = null;
    private String openType;
    private String orderType;
    private PayInfo payInfo = null;
    private int payType;
    private String phoneNum;
    private List<QueryOrder> queryOrders;
    private String spId;
    private String spIdForOpen;
    private String uiMode;
    private UnionPayInfo unionPayInfo;
    private WXPayInfo wxPayInfo;

    public String getCityCode() {
        return this.cityCode;
    }

    public void setCityCode(String cityCode2) {
        this.cityCode = cityCode2;
    }

    public String getIsUiMode() {
        return this.uiMode;
    }

    public void setIsUiMode(String uiMode2) {
        this.uiMode = uiMode2;
    }

    public String getSpIdForOpen() {
        return this.spIdForOpen;
    }

    public void setSpIdForOpen(String spIdForOpen2) {
        this.spIdForOpen = spIdForOpen2;
    }

    public String getHwOrderNo() {
        return this.hwOrderNo;
    }

    public void setHwOrderNo(String hwOrderNo2) {
        this.hwOrderNo = hwOrderNo2;
    }

    public PayInfo getPayInfo() {
        return this.payInfo;
    }

    public void setPayInfo(PayInfo payInfo2) {
        this.payInfo = payInfo2;
    }

    public int getPayType() {
        return this.payType;
    }

    public void setPayType(int payType2) {
        this.payType = payType2;
    }

    public WXPayInfo getWxPayInfo() {
        return this.wxPayInfo;
    }

    public void setWXPayInfo(WXPayInfo wxPayInfo2) {
        this.wxPayInfo = wxPayInfo2;
    }

    public String getPhoneNum() {
        return this.phoneNum;
    }

    public void setPhoneNum(String phoneNum2) {
        this.phoneNum = phoneNum2;
    }

    public boolean getHasUnusedIssueOrder() {
        return this.hasUnusedIssueOrder;
    }

    public void setHasUnusedIssueOrder(boolean hasUnusedIssueOrder2) {
        this.hasUnusedIssueOrder = hasUnusedIssueOrder2;
    }

    public String getSpId() {
        return this.spId;
    }

    public void setSpId(String spId2) {
        this.spId = spId2;
    }

    public boolean isDuplicateApply() {
        return this.isDuplicateApply;
    }

    public void setDuplicateApply(boolean duplicateApply) {
        this.isDuplicateApply = duplicateApply;
    }

    public List<QueryOrder> getQueryOrders() {
        return this.queryOrders;
    }

    public void setQueryOrders(List<QueryOrder> queryOrders2) {
        this.queryOrders = queryOrders2;
    }

    public List<ApplyOrder> getApplyOrders() {
        return this.applyOrders;
    }

    public void setApplyOrders(List<ApplyOrder> applyOrders2) {
        this.applyOrders = applyOrders2;
    }

    public boolean isNewPayVersion() {
        return this.isNewPayVersion;
    }

    public void setNewPayVersion(boolean newPayVersion) {
        this.isNewPayVersion = newPayVersion;
    }

    public UnionPayInfo getUnionPayInfo() {
        return this.unionPayInfo;
    }

    public void setUnionPayInfo(UnionPayInfo unionPayInfo2) {
        this.unionPayInfo = unionPayInfo2;
    }

    public String getOpenType() {
        return this.openType;
    }

    public void setOpenType(String openType2) {
        this.openType = openType2;
    }

    public String getOrderType() {
        if (this.orderType == null) {
            return REPORT_ORDER_TYPE_RECHARGE;
        }
        return this.orderType;
    }

    public void setOrderType(String orderType2) {
        this.orderType = orderType2;
    }

    public void setBalance(int balance2) {
        this.balance = balance2;
    }

    public int getBalance() {
        return this.balance;
    }

    public TransferOrder getTransferOrder() {
        return this.mTransferOrder;
    }

    public void setTransferOrder(TransferOrder mTransferOrder2) {
        this.mTransferOrder = mTransferOrder2;
    }
}
