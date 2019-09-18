package com.huawei.wallet.sdk.business.idcard.walletbase.model;

public class WalletCardBaseInfo implements IWalletCardBaseInfo {
    private String action;
    private String balance;
    private Object cardObject;
    private String expireDate;
    private boolean isDefault;
    private String name;
    private String number;
    private String pictureLocalPath;
    private int status;
    private int type;

    public WalletCardBaseInfo() {
    }

    public WalletCardBaseInfo(String name2, String number2, int type2, String path, int status2, Object obj) {
        this.name = name2 == null ? "" : name2;
        this.number = number2 == null ? "" : number2;
        this.type = type2;
        this.pictureLocalPath = path == null ? "" : path;
        this.status = status2;
        this.cardObject = obj;
        this.balance = "";
        this.expireDate = "";
        this.action = "";
    }

    public void setName(String name2) {
        this.name = name2 == null ? "" : name2;
    }

    public void setNumber(String number2) {
        this.number = number2 == null ? "" : number2;
    }

    public void setType(int type2) {
        this.type = type2;
    }

    public void setDefault(boolean aDefault) {
        this.isDefault = aDefault;
    }

    public void setPictureLocalPath(String pictureLocalPath2) {
        this.pictureLocalPath = pictureLocalPath2 == null ? "" : pictureLocalPath2;
    }

    public void setStatus(int status2) {
        this.status = status2;
    }

    public void setBalance(String balance2) {
        this.balance = balance2 == null ? "" : balance2;
    }

    public void setExpireDate(String expireDate2) {
        this.expireDate = expireDate2 == null ? "" : expireDate2;
    }

    public void setAction(String action2) {
        this.action = action2 == null ? "" : action2;
    }

    public void setCardObject(Object cardObject2) {
        this.cardObject = cardObject2;
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public int getStatus() {
        return this.status;
    }

    public String getAction() {
        return this.action;
    }

    public Object getObject() {
        return this.cardObject;
    }
}
