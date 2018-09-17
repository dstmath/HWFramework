package com.android.internal.telephony.cat;

/* compiled from: CommandDetails */
class IconId extends ValueObject {
    int recordNumber;
    boolean selfExplanatory;

    IconId() {
    }

    ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ICON_ID;
    }
}
