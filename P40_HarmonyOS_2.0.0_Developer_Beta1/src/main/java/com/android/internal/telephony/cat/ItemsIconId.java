package com.android.internal.telephony.cat;

/* compiled from: CommandDetails */
class ItemsIconId extends ValueObject {
    int[] recordNumbers;
    boolean selfExplanatory;

    ItemsIconId() {
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.ValueObject
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ITEM_ICON_ID_LIST;
    }
}
