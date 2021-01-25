package org.bouncycastle.cms;

public class PasswordRecipientId extends RecipientId {
    public PasswordRecipientId() {
        super(3);
    }

    @Override // org.bouncycastle.cms.RecipientId, org.bouncycastle.util.Selector, java.lang.Object
    public Object clone() {
        return new PasswordRecipientId();
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        return obj instanceof PasswordRecipientId;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return 3;
    }

    @Override // org.bouncycastle.util.Selector
    public boolean match(Object obj) {
        return obj instanceof PasswordRecipientInformation;
    }
}
