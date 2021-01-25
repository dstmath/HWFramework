package com.huawei.ace.activity;

import android.util.Log;
import com.huawei.ace.runtime.ILogger;

public class Logger implements ILogger {
    @Override // com.huawei.ace.runtime.ILogger
    public boolean isDebuggable() {
        return false;
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void d(String str, String str2) {
        Log.d(str, str2);
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void i(String str, String str2) {
        Log.i(str, str2);
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void w(String str, String str2) {
        Log.w(str, str2);
    }

    @Override // com.huawei.ace.runtime.ILogger
    public void e(String str, String str2) {
        Log.e(str, str2);
    }
}
