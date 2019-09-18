package com.huawei.wallet.sdk.business.bankcard.response;

import com.huawei.wallet.sdk.business.bankcard.modle.CardStatusItem;
import com.huawei.wallet.sdk.common.apdu.response.CardServerBaseResponse;
import java.util.List;

public class CardStatusQueryResponse extends CardServerBaseResponse {
    public static final String DEV_STATUS_LOCK = "9";
    public static final String DEV_STATUS_LOST = "2";
    public static final String DEV_STATUS_REPAIR = "4";
    private long cardCount;
    private List<CardStatusItem> cloudItems;
    private String createtime;
    private String devStatus;
    private long inCloudCount;
    private List<CardStatusItem> items;
    private String reserved;

    public long getInCloudCount() {
        return this.inCloudCount;
    }

    public void setInCloudCount(long inCloudCount2) {
        this.inCloudCount = inCloudCount2;
    }

    public List<CardStatusItem> getCloudItems() {
        return this.cloudItems;
    }

    public void setCloudItems(List<CardStatusItem> cloudItems2) {
        this.cloudItems = cloudItems2;
    }

    public String getDevStatus() {
        return this.devStatus;
    }

    public void setDevStatus(String devStatus2) {
        this.devStatus = devStatus2;
    }

    public long getCardCount() {
        return this.cardCount;
    }

    public void setCardCount(long cardCount2) {
        this.cardCount = cardCount2;
    }

    public List<CardStatusItem> getItems() {
        return this.items;
    }

    public void setItems(List<CardStatusItem> items2) {
        this.items = items2;
    }

    public String getCreatetime() {
        return this.createtime;
    }

    public void setCreatetime(String createtime2) {
        this.createtime = createtime2;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved2) {
        this.reserved = reserved2;
    }
}
