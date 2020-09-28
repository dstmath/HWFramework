package com.huawei.android.util;

import android.util.LocalLog;
import com.huawei.internal.util.IndentingPrintWriterEx;
import java.io.FileDescriptor;

public final class LocalLogEx {
    private LocalLog mLocalLog;

    public LocalLogEx(int maxLines) {
        this.mLocalLog = new LocalLog(maxLines);
    }

    public void log(String msg) {
        LocalLog localLog = this.mLocalLog;
        if (localLog != null) {
            localLog.log(msg);
        }
    }

    public LocalLog getLocalLog() {
        return this.mLocalLog;
    }

    public void dump(FileDescriptor fd, IndentingPrintWriterEx pw, String[] args) {
        LocalLog localLog = this.mLocalLog;
        if (localLog != null) {
            localLog.dump(fd, pw.getIndentingPrintWriter(), args);
        }
    }
}
