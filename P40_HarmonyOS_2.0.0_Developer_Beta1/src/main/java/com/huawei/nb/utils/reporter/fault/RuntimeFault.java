package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public class RuntimeFault extends Fault {
    private RuntimeFault(String str, String str2, String str3, String str4, String str5) {
        this.parameters.put(0, "RuntimeFault");
        this.parameters.put(2, str2);
        this.parameters.put(6, str);
        this.parameters.put(7, str3);
        this.parameters.put(9, str4);
        this.parameters.put(15, str5);
        this.keyMessage = str + str3 + str4;
    }

    public static void report(String str, String str2, String str3, String str4, String str5) {
        Reporter.f(new RuntimeFault(str, str2, str3, str4, str5));
    }
}
