package com.huawei.android.bastet;

import android.os.Handler;

public class BastetEmailExchange extends BastetEmail {
    private ExchangeHttpHeader mHttpHeader;
    private ExchangeWbInfo mWbInfo;

    public BastetEmailExchange(EmailLoginInfo login, ExchangeHttpHeader header, ExchangeWbInfo wb, Handler handler) {
        super(login, null, 3, handler);
        this.mHttpHeader = header;
        this.mWbInfo = wb;
    }

    public void startProxy() throws Exception {
        if (this.mProxyId <= 0) {
            throw new Exception();
        }
        this.mIBastetManager.setExchangeHttpHeader(this.mProxyId, this.mHttpHeader.getVersion(), this.mHttpHeader.getUserAgent(), this.mHttpHeader.getEncoding(), this.mHttpHeader.getPolicyKey(), this.mHttpHeader.getHostName());
        this.mIBastetManager.updateExchangeWebXmlInfo(this.mProxyId, this.mWbInfo.getCollectionId(), this.mWbInfo.getSyncKey(), this.mWbInfo.getSyncType());
        this.mIBastetManager.startBastetProxy(this.mProxyId);
    }
}
