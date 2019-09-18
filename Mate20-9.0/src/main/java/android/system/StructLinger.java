package android.system;

import libcore.util.Objects;

public final class StructLinger {
    public final int l_linger;
    public final int l_onoff;

    public StructLinger(int l_onoff2, int l_linger2) {
        this.l_onoff = l_onoff2;
        this.l_linger = l_linger2;
    }

    public boolean isOn() {
        return this.l_onoff != 0;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
