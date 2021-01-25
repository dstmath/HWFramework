package com.huawei.nb.utils.logger;

import com.huawei.android.app.HmfLog;

public class ODMFLogAdapter implements LogAdapter {
    private boolean hmfLogSupported = true;

    @Override // com.huawei.nb.utils.logger.LogAdapter
    public void v(String str, String str2) {
        log(str, str2);
    }

    @Override // com.huawei.nb.utils.logger.LogAdapter
    public void d(String str, String str2) {
        log(str, str2);
    }

    @Override // com.huawei.nb.utils.logger.LogAdapter
    public void i(String str, String str2) {
        log(str, str2);
    }

    @Override // com.huawei.nb.utils.logger.LogAdapter
    public void w(String str, String str2) {
        log(str, str2);
    }

    @Override // com.huawei.nb.utils.logger.LogAdapter
    public void e(String str, String str2) {
        log(str, str2);
    }

    private void log(String str, String str2) {
        if (this.hmfLogSupported) {
            try {
                HmfLog.i(1, str, str2);
            } catch (Throwable unused) {
                this.hmfLogSupported = false;
                DSLog.e("Hmf log is not supported.", new Object[0]);
            }
        }
    }
}
