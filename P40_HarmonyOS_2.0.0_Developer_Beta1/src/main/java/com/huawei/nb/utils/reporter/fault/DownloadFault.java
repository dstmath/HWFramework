package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.logger.DSLog;

public final class DownloadFault extends Fault {
    private DownloadFault(String str) {
        this.parameters.put(0, DownloadFault.class.getSimpleName());
        this.parameters.put(6, "Coordinator");
        this.parameters.put(7, "DownloadError");
        this.parameters.put(15, str);
        this.keyMessage = "DownloadError";
    }

    public static void report(String str) {
        DSLog.e(" Download fault occurred! ", new Object[0]);
    }
}
