package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;

/* compiled from: CommandDetails */
class IconId extends ValueObject {
    @UnsupportedAppUsage
    int recordNumber;
    boolean selfExplanatory;

    IconId() {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.ValueObject
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ICON_ID;
    }
}
