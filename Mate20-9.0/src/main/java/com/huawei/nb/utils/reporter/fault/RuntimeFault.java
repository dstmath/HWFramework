package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public class RuntimeFault extends Fault {
    private RuntimeFault(String componentInfo, String function, String typeInfo, String target, String logInfo) {
        this.parameters.put(0, "RuntimeFault");
        this.parameters.put(2, function);
        this.parameters.put(6, componentInfo);
        this.parameters.put(7, typeInfo);
        this.parameters.put(9, target);
        this.parameters.put(15, logInfo);
        this.keyMessage = componentInfo + typeInfo + target;
    }

    public static void report(String componentInfo, String function, String typeInfo, String target, String logInfo) {
        Reporter.f(new RuntimeFault(componentInfo, function, typeInfo, target, logInfo));
    }
}
