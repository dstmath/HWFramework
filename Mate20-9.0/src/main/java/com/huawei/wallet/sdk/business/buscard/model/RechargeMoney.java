package com.huawei.wallet.sdk.business.buscard.model;

import com.huawei.wallet.sdk.business.buscard.base.util.MoneyUtil;

public class RechargeMoney {
    private int payMoney;
    private int rechargeMoney;

    public int getRechargeMoney() {
        return this.rechargeMoney;
    }

    public void setRechargeMoney(int rechargeMoney2) {
        this.rechargeMoney = rechargeMoney2;
    }

    public int getPayMoney() {
        return this.payMoney;
    }

    public void setPayMoney(int payMoney2) {
        this.payMoney = payMoney2;
    }

    public boolean isPayMoneyRechargeable() {
        return this.payMoney > 0;
    }

    public String getPayMoneyDispStr() {
        return MoneyUtil.convertFenToYuan((long) this.payMoney);
    }

    public String getRechargeMoneyDispStr() {
        return (this.rechargeMoney / 100) + "";
    }
}
