package android.system;

import libcore.util.Objects;

public final class StructCapUserData {
    public final int effective;
    public final int inheritable;
    public final int permitted;

    public StructCapUserData(int effective2, int permitted2, int inheritable2) {
        this.effective = effective2;
        this.permitted = permitted2;
        this.inheritable = inheritable2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
