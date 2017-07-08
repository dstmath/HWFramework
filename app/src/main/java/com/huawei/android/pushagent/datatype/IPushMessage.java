package com.huawei.android.pushagent.datatype;

import java.io.Serializable;

public interface IPushMessage extends Serializable {
    byte[] encode();

    byte j();
}
