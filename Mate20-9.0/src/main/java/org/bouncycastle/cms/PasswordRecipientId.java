package org.bouncycastle.cms;

public class PasswordRecipientId extends RecipientId {
    public PasswordRecipientId() {
        super(3);
    }

    public Object clone() {
        return new PasswordRecipientId();
    }

    public boolean equals(Object obj) {
        return obj instanceof PasswordRecipientId;
    }

    public int hashCode() {
        return 3;
    }

    public boolean match(Object obj) {
        return obj instanceof PasswordRecipientInformation;
    }
}
