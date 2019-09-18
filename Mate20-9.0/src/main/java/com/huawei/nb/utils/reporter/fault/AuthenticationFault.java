package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public class AuthenticationFault extends Fault {
    private AuthenticationFault(String componentInfo, String functionName, String typeInfo, String logInfo) {
        this.parameters.put(0, AuthenticationFault.class.getSimpleName());
        this.parameters.put(2, functionName);
        this.parameters.put(6, componentInfo);
        this.parameters.put(7, typeInfo);
        this.parameters.put(15, logInfo);
        this.keyMessage = componentInfo + typeInfo;
    }

    public static void report(String componentInfo, String functionName, String typeInfo, String logInfo) {
        Reporter.f(new AuthenticationFault(componentInfo, functionName, typeInfo, logInfo));
    }
}
