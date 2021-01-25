package com.huawei.internal.telephony;

import android.common.HwFrameworkFactory;
import android.util.LogException;

public class LogExceptionEx {
    private static final int INVALID_MSG = -1;
    private LogException mLogException = null;

    public void setLogException() {
        this.mLogException = HwFrameworkFactory.getLogException();
    }

    public LogException getInstance() {
        if (this.mLogException == null) {
            this.mLogException = HwFrameworkFactory.getLogException();
        }
        return this.mLogException;
    }

    public int msg(String category, int level, String header, String body) {
        LogException logException = this.mLogException;
        if (logException != null) {
            return logException.msg(category, level, header, body);
        }
        return -1;
    }
}
