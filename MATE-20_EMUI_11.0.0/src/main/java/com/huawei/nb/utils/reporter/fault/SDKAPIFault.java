package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public final class SDKAPIFault extends Fault {
    private SDKAPIFault(String str) {
        this.parameters.put(0, SDKAPIFault.class.getSimpleName());
        this.parameters.put(6, "Coordinator");
        this.parameters.put(7, "SDKAPIErrorError");
        this.parameters.put(15, str);
        this.keyMessage = "SDKAPIErrorError";
    }

    public static void report(String str) {
        Reporter.f(new SDKAPIFault(str));
    }
}
