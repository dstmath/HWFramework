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

    /* access modifiers changed from: package-private */
    public String name() {
        return Util.toString(this.name);
    }

    /* access modifiers changed from: package-private */
    public String fstype() {
        if (this.fstypeAsString == null) {
            this.fstypeAsString = Util.toString(this.fstype);
        }
        return this.fstypeAsString;
    }

    /* access modifiers changed from: package-private */
    public byte[] dir() {
        return this.dir;
    }

    /* access modifiers changed from: package-private */
    public long dev() {
        return this.dev;
    }

    /* access modifiers changed from: package-private */
    public boolean hasOption(String requested) {
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

    /* access modifiers changed from: package-private */
    public boolean isIgnored() {
        return hasOption("ignore");
    }

    /* access modifiers changed from: package-private */
    public boolean isReadOnly() {
        return hasOption("ro");
    }
}
