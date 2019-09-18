package com.huawei.security.hccm.param;

import java.security.InvalidParameterException;

public abstract class ProtocolParam {
    public static final int PROTOCOL_CPM = 1;
    public static final int PROTOCOL_SCEP = 2;
    protected int mProtocol;

    public ProtocolParam getInstance(int protocolType) {
        switch (protocolType) {
            case 1:
                this.mProtocol = 1;
                break;
            case 2:
                this.mProtocol = 2;
                break;
            default:
                throw new InvalidParameterException();
        }
        return this;
    }

    public int getProtocolType() {
        return this.mProtocol;
    }
}
