package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.logger.DSLog;

public class DownloadFault extends Fault {
    private DownloadFault(String detail) {
        this.parameters.put(0, DownloadFault.class.getSimpleName());
        this.parameters.put(6, "Coordinator");
        this.parameters.put(7, "DownloadError");
        this.parameters.put(15, detail);
        this.keyMessage = "DownloadError";
    }

    public static void report(String detail) {
        DSLog.e(" Download fault occurred! ", new Object[0]);
    }
}
