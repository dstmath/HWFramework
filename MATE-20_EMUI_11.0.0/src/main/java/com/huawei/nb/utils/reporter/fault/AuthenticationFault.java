package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public final class AuthenticationFault extends Fault {
    private AuthenticationFault(String str, String str2, String str3, String str4) {
        this.parameters.put(0, AuthenticationFault.class.getSimpleName());
        this.parameters.put(2, str2);
        this.parameters.put(6, str);
        this.parameters.put(7, str3);
        this.parameters.put(15, str4);
        this.keyMessage = str + str3;
    }

    public static void report(String str, String str2, String str3, String str4) {
        Reporter.f(new AuthenticationFault(str, str2, str3, str4));
    }
}
