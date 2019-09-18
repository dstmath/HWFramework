package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public class DBAccessFault extends Fault {
    private DBAccessFault(String componentInfo, String functionName, String typeInfo, String logInfo) {
        this.parameters.put(0, DBAccessFault.class.getSimpleName());
        this.parameters.put(2, functionName);
        this.parameters.put(6, componentInfo);
        this.parameters.put(7, typeInfo);
        this.parameters.put(15, logInfo);
        this.keyMessage = componentInfo + typeInfo;
    }

    public static void report(String componentInfo, String functionName, String typeInfo, String logInfo) {
        Reporter.f(new DBAccessFault(componentInfo, functionName, typeInfo, logInfo));
    }
}
