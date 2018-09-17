package android.system;

import libcore.util.Objects;

public final class StructCapUserData {
    public final int effective;
    public final int inheritable;
    public final int permitted;

    public StructCapUserData(int effective, int permitted, int inheritable) {
        this.effective = effective;
        this.permitted = permitted;
        this.inheritable = inheritable;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
