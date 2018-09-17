package android.system;

import libcore.util.Objects;

public final class StructLinger {
    public final int l_linger;
    public final int l_onoff;

    public StructLinger(int l_onoff, int l_linger) {
        this.l_onoff = l_onoff;
        this.l_linger = l_linger;
    }

    public boolean isOn() {
        return this.l_onoff != 0;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
