package sun.nio.fs;

class UnixMountEntry {
    private long dev;
    private byte[] dir;
    private byte[] fstype;
    private volatile String fstypeAsString;
    private byte[] name;
    private volatile String optionsAsString;
    private byte[] opts;

    UnixMountEntry() {
    }

    String name() {
        return Util.toString(this.name);
    }

    String fstype() {
        if (this.fstypeAsString == null) {
            this.fstypeAsString = Util.toString(this.fstype);
        }
        return this.fstypeAsString;
    }

    byte[] dir() {
        return this.dir;
    }

    long dev() {
        return this.dev;
    }

    boolean hasOption(String requested) {
        if (this.optionsAsString == null) {
            this.optionsAsString = Util.toString(this.opts);
        }
        for (String opt : Util.split(this.optionsAsString, ',')) {
            if (opt.equals(requested)) {
                return true;
            }
        }
        return false;
    }

    boolean isIgnored() {
        return hasOption("ignore");
    }

    boolean isReadOnly() {
        return hasOption("ro");
    }
}
