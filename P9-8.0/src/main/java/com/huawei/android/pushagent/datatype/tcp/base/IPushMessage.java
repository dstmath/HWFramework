package com.huawei.android.pushagent.datatype.tcp.base;

import java.io.Serializable;

public interface IPushMessage extends Serializable {
    byte[] vs();

    byte vt();
}
