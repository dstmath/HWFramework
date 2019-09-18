package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.logger.DSLog;

public class HttpsFault extends Fault {
    private HttpsFault(String detail) {
        this.parameters.put(0, HttpsFault.class.getSimpleName());
        this.parameters.put(6, "Coordinator");
        this.parameters.put(7, "HttpsError");
        this.parameters.put(15, detail);
        this.keyMessage = "Coordinator";
    }

    public static void report(String detail) {
        DSLog.e(" Https fault occurred! ", new Object[0]);
    }
}
