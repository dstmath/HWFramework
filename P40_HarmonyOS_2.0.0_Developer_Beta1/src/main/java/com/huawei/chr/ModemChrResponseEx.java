package com.huawei.chr;

import java.util.ArrayList;
import vendor.huawei.hardware.modemchr.V1_0.IModemchrResponse;

public class ModemChrResponseEx {
    private IModemchrResponse modemChrResponse = new IModemchrResponse.Stub() {
        /* class com.huawei.chr.ModemChrResponseEx.AnonymousClass1 */

        public void onChrReport(ArrayList<Byte> var1, int var2) {
            ModemChrResponseEx.this.onChrReport(var1, var2);
        }

        public void onChrReportEx(ArrayList<Byte> var1, int var2, ArrayList<Byte> var3, int var4) {
            ModemChrResponseEx.this.onChrReportEx(var1, var2, var3, var4);
        }

        public void onStateNotify(int var1, int var2) {
            ModemChrResponseEx.this.onStateNotify(var1, var2);
        }
    };

    /* access modifiers changed from: package-private */
    public IModemchrResponse getModemChrResponse() {
        return this.modemChrResponse;
    }

    public void onChrReport(ArrayList<Byte> arrayList, int msgLen) {
    }

    public void onChrReportEx(ArrayList<Byte> arrayList, int resMsgLen, ArrayList<Byte> arrayList2, int reqMsgLen) {
    }

    public void onStateNotify(int stateType, int stateValue) {
    }
}
