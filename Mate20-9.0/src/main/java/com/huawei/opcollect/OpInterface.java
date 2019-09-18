package com.huawei.opcollect;

import android.content.Context;
import java.io.PrintWriter;

public interface OpInterface {
    void dump(PrintWriter printWriter);

    void initialize(Context context);

    void switchOff();

    void switchOn();
}
