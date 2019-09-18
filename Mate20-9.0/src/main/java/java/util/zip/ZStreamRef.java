package java.util.zip;

class ZStreamRef {
    private volatile long address;

    ZStreamRef(long address2) {
        this.address = address2;
    }

    /* access modifiers changed from: package-private */
    public long address() {
        return this.address;
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.address = 0;
    }
}
