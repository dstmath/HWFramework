package com.android.internal.telephony.uicc;

import android.annotation.UnsupportedAppUsage;
import android.telephony.Rlog;

public abstract class IccServiceTable {
    @UnsupportedAppUsage
    protected final byte[] mServiceTable;

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    public abstract String getTag();

    /* access modifiers changed from: protected */
    public abstract Object[] getValues();

    protected IccServiceTable(byte[] table) {
        this.mServiceTable = table;
    }

    /* access modifiers changed from: protected */
    public boolean isAvailable(int service) {
        int offset = service / 8;
        byte[] bArr = this.mServiceTable;
        if (offset >= bArr.length) {
            String tag = getTag();
            Rlog.e(tag, "isAvailable for service " + (service + 1) + " fails, max service is " + (this.mServiceTable.length * 8));
            return false;
        } else if ((bArr[offset] & (1 << (service % 8))) != 0) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        Object[] values = getValues();
        int numBytes = this.mServiceTable.length;
        StringBuilder sb = new StringBuilder(getTag());
        sb.append('[');
        sb.append(numBytes * 8);
        StringBuilder builder = sb.append("]={ ");
        boolean addComma = false;
        for (int i = 0; i < numBytes; i++) {
            byte currentByte = this.mServiceTable[i];
            for (int bit = 0; bit < 8; bit++) {
                if (((1 << bit) & currentByte) != 0) {
                    if (addComma) {
                        builder.append(", ");
                    } else {
                        addComma = true;
                    }
                    int ordinal = (i * 8) + bit;
                    if (ordinal < values.length) {
                        builder.append(values[ordinal]);
                    } else {
                        builder.append('#');
                        builder.append(ordinal + 1);
                    }
                }
            }
        }
        builder.append(" }");
        return builder.toString();
    }
}
