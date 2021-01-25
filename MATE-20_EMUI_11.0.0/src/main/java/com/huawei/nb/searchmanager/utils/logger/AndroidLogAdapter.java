package com.huawei.nb.searchmanager.utils.logger;

import android.util.Log;

/* access modifiers changed from: package-private */
public class AndroidLogAdapter implements LogAdapter {
    AndroidLogAdapter() {
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.LogAdapter
    public void v(String str, String str2) {
        Log.v(str, str2);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.LogAdapter
    public void d(String str, String str2) {
        Log.d(str, str2);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.LogAdapter
    public void i(String str, String str2) {
        Log.i(str, str2);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.LogAdapter
    public void w(String str, String str2) {
        Log.w(str, str2);
    }

    @Override // com.huawei.nb.searchmanager.utils.logger.LogAdapter
    public void e(String str, String str2) {
        Log.e(str, str2);
    }
}
