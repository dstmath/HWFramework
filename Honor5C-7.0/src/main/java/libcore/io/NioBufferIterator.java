package libcore.io;

public final class NioBufferIterator extends BufferIterator {
    private final long address;
    private int position;
    private final int size;
    private final boolean swap;

    NioBufferIterator(long address, int size, boolean swap) {
        this.address = address;
        this.size = size;
        this.swap = swap;
    }

    public void seek(int offset) {
        this.position = offset;
    }

    public void skip(int byteCount) {
        this.position += byteCount;
    }

    public void readByteArray(byte[] dst, int dstOffset, int byteCount) {
        Memory.peekByteArray(this.address + ((long) this.position), dst, dstOffset, byteCount);
        this.position += byteCount;
    }

    public byte readByte() {
        byte result = Memory.peekByte(this.address + ((long) this.position));
        this.position++;
        return result;
    }

    public int readInt() {
        int result = Memory.peekInt(this.address + ((long) this.position), this.swap);
        this.position += 4;
        return result;
    }

    public void readIntArray(int[] dst, int dstOffset, int intCount) {
        Memory.peekIntArray(this.address + ((long) this.position), dst, dstOffset, intCount, this.swap);
        this.position += intCount * 4;
    }

    public short readShort() {
        short result = Memory.peekShort(this.address + ((long) this.position), this.swap);
        this.position += 2;
        return result;
    }
}
