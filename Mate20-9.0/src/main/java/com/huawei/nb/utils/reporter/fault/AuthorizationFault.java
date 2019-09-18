package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public class AuthorizationFault extends Fault {
    private AuthorizationFault(String detail) {
        this.parameters.put(0, AuthorizationFault.class.getSimpleName());
        this.parameters.put(6, "Coordinator");
        this.parameters.put(7, "AuthorizationError");
        this.parameters.put(15, detail);
        this.keyMessage = "AuthorizationError";
    }

    public static void report(String detail) {
        Reporter.f(new AuthorizationFault(detail));
    }
}
