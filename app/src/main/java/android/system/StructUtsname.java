package android.system;

import libcore.util.Objects;

public final class StructUtsname {
    public final String machine;
    public final String nodename;
    public final String release;
    public final String sysname;
    public final String version;

    public StructUtsname(String sysname, String nodename, String release, String version, String machine) {
        this.sysname = sysname;
        this.nodename = nodename;
        this.release = release;
        this.version = version;
        this.machine = machine;
    }

    public String toString() {
        return Objects.toString(this);
    }
}
