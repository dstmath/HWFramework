package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public final class AuthorizationFault extends Fault {
    private AuthorizationFault(String str) {
        this.parameters.put(0, AuthorizationFault.class.getSimpleName());
        this.parameters.put(6, "Coordinator");
        this.parameters.put(7, "AuthorizationError");
        this.parameters.put(15, str);
        this.keyMessage = "AuthorizationError";
    }

    public static void report(String str) {
        Reporter.f(new AuthorizationFault(str));
    }
}
