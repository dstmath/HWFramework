package com.huawei.security.hccm.param;

import com.huawei.security.hccm.param.ProtocolParam;
import java.security.InvalidParameterException;

public abstract class ProtocolParam<T extends ProtocolParam<T>> {
    public static final int PROTOCOL_CPM = 1;
    public static final int PROTOCOL_SCEP = 2;
    protected int mProtocol;

    public T getInstance(int protocolType) {
        if (protocolType == 1) {
            this.mProtocol = 1;
        } else if (protocolType == 2) {
            this.mProtocol = 2;
        } else {
            throw new InvalidParameterException("Unsupported protocol: #protocolType" + protocolType);
        }
        return this;
    }

    public int getProtocolType() {
        return this.mProtocol;
    }
}
