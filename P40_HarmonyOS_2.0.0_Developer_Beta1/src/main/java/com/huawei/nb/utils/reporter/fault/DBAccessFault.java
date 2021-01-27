package com.huawei.nb.utils.reporter.fault;

import com.huawei.nb.utils.reporter.Reporter;

public class DBAccessFault extends Fault {
    private DBAccessFault(String str, String str2, String str3, String str4) {
        this.parameters.put(0, DBAccessFault.class.getSimpleName());
        this.parameters.put(2, str2);
        this.parameters.put(6, str);
        this.parameters.put(7, str3);
        this.parameters.put(15, str4);
        this.keyMessage = str + str3;
    }

    public static void report(String str, String str2, String str3, String str4) {
        Reporter.f(new DBAccessFault(str, str2, str3, str4));
    }
}
