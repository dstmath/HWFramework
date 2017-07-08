package android.system;

import java.net.InetAddress;
import libcore.util.Objects;

public final class StructGroupSourceReq {
    public final InetAddress gsr_group;
    public final int gsr_interface;
    public final InetAddress gsr_source;

    public StructGroupSourceReq(int gsr_interface, InetAddress gsr_group, InetAddress gsr_source) {
        this.gsr_interface = gsr_interface;
        this.gsr_group = gsr_group;
        this.gsr_source = gsr_source;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
