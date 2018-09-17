package java.util.zip;

class ZStreamRef {
    private long address;

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
