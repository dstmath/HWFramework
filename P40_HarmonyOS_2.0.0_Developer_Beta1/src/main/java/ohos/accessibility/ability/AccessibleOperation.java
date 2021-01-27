package ohos.accessibility.ability;

public class AccessibleOperation {
    private final CharSequence mDescriptionInfo;
    private final int mOperationType;

    public AccessibleOperation(int i, CharSequence charSequence) {
        this.mOperationType = i;
        this.mDescriptionInfo = charSequence;
    }

    public int getOperationType() {
        return this.mOperationType;
    }

    public CharSequence getDescriptionInfo() {
        return this.mDescriptionInfo;
    }

    public int hashCode() {
        return this.mOperationType;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return getClass() == obj.getClass() && this.mOperationType == ((AccessibleOperation) obj).mOperationType;
    }
}
