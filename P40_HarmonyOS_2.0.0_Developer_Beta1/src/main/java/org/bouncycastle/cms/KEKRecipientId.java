package org.bouncycastle.cms;

import org.bouncycastle.util.Arrays;

public class KEKRecipientId extends RecipientId {
    private byte[] keyIdentifier;

    public KEKRecipientId(byte[] bArr) {
        super(1);
        this.keyIdentifier = bArr;
    }

    @Override // org.bouncycastle.cms.RecipientId, org.bouncycastle.util.Selector, java.lang.Object
    public Object clone() {
        return new KEKRecipientId(this.keyIdentifier);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (!(obj instanceof KEKRecipientId)) {
            return false;
        }
        return Arrays.areEqual(this.keyIdentifier, ((KEKRecipientId) obj).keyIdentifier);
    }

    public byte[] getKeyIdentifier() {
        return Arrays.clone(this.keyIdentifier);
    }

    @Override // java.lang.Object
    public int hashCode() {
        return Arrays.hashCode(this.keyIdentifier);
    }

    @Override // org.bouncycastle.util.Selector
    public boolean match(Object obj) {
        if (obj instanceof byte[]) {
            return Arrays.areEqual(this.keyIdentifier, (byte[]) obj);
        }
        if (obj instanceof KEKRecipientInformation) {
            return ((KEKRecipientInformation) obj).getRID().equals(this);
        }
        return false;
    }
}
