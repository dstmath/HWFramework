package android.system;

import java.net.InetAddress;
import libcore.util.Objects;

public final class StructGroupReq {
    public final InetAddress gr_group;
    public final int gr_interface;

    public StructGroupReq(int gr_interface, InetAddress gr_group) {
        this.gr_interface = gr_interface;
        this.gr_group = gr_group;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
