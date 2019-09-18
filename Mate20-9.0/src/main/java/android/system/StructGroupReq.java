package android.system;

import java.net.InetAddress;
import libcore.util.Objects;

public final class StructGroupReq {
    public final InetAddress gr_group;
    public final int gr_interface;

    public StructGroupReq(int gr_interface2, InetAddress gr_group2) {
        this.gr_interface = gr_interface2;
        this.gr_group = gr_group2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
