package java.util.zip;

class ZStreamRef {
    private volatile long address;

    ZStreamRef(long address) {
        this.address = address;
    }

    long address() {
        return this.address;
    }

    void clear() {
        this.address = 0;
    }
}
