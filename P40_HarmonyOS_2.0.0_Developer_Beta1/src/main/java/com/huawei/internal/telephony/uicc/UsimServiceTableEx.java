package com.huawei.internal.telephony.uicc;

import com.android.internal.telephony.uicc.UsimServiceTable;

public class UsimServiceTableEx {
    UsimServiceTable mUsimServiceTable;

    public Object getUsimServiceTable() {
        return this.mUsimServiceTable;
    }

    public void setUsimServiceTable(UsimServiceTable usimServiceTable) {
        this.mUsimServiceTable = usimServiceTable;
    }

    public boolean isAvailable(UsimServiceEx service) {
        UsimServiceTable usimServiceTable = this.mUsimServiceTable;
        if (usimServiceTable == null || service == null) {
            return false;
        }
        return usimServiceTable.isAvailable(service.getValue());
    }

    public enum UsimServiceEx {
        SPN(UsimServiceTable.UsimService.SPN),
        PLMN_NETWORK_NAME(UsimServiceTable.UsimService.PLMN_NETWORK_NAME);
        
        private final UsimServiceTable.UsimService value;

        private UsimServiceEx(UsimServiceTable.UsimService value2) {
            this.value = value2;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private UsimServiceTable.UsimService getValue() {
            return this.value;
        }
    }
}
