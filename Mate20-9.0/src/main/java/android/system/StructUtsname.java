package android.system;

import libcore.util.Objects;

public final class StructUtsname {
    public final String machine;
    public final String nodename;
    public final String release;
    public final String sysname;
    public final String version;

    public StructUtsname(String sysname2, String nodename2, String release2, String version2, String machine2) {
        this.sysname = sysname2;
        this.nodename = nodename2;
        this.release = release2;
        this.version = version2;
        this.machine = machine2;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
