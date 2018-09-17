package sun.nio.ch;

import java.nio.ByteOrder;
import sun.misc.Unsafe;

class NativeObject {
    static final /* synthetic */ boolean -assertionsDisabled = (NativeObject.class.desiredAssertionStatus() ^ 1);
    private static ByteOrder byteOrder = null;
    private static int pageSize = -1;
    protected static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long address;
    protected long allocationAddress;

    NativeObject(long address) {
        this.allocationAddress = address;
        this.address = address;
    }

    NativeObject(long address, long offset) {
        this.allocationAddress = address;
        this.address = address + offset;
    }

    protected NativeObject(int size, boolean pageAligned) {
        if (pageAligned) {
            int ps = pageSize();
            long a = unsafe.allocateMemory((long) (size + ps));
            this.allocationAddress = a;
            this.address = (((long) ps) + a) - (((long) (ps - 1)) & a);
            return;
        }
        this.allocationAddress = unsafe.allocateMemory((long) size);
        this.address = this.allocationAddress;
    }

    long address() {
        return this.address;
    }

    long allocationAddress() {
        return this.allocationAddress;
    }

    NativeObject subObject(int offset) {
        return new NativeObject(((long) offset) + this.address);
    }

    NativeObject getObject(int offset) {
        long newAddress;
        switch (addressSize()) {
            case 4:
                newAddress = (long) (unsafe.getInt(((long) offset) + this.address) & -1);
                break;
            case 8:
                newAddress = unsafe.getLong(((long) offset) + this.address);
                break;
            default:
                throw new InternalError("Address size not supported");
        }
        return new NativeObject(newAddress);
    }

    void putObject(int offset, NativeObject ob) {
        switch (addressSize()) {
            case 4:
                putInt(offset, (int) (ob.address & -1));
                return;
            case 8:
                putLong(offset, ob.address);
                return;
            default:
                throw new InternalError("Address size not supported");
        }
    }

    final byte getByte(int offset) {
        return unsafe.getByte(((long) offset) + this.address);
    }

    final void putByte(int offset, byte value) {
        unsafe.putByte(((long) offset) + this.address, value);
    }

    final short getShort(int offset) {
        return unsafe.getShort(((long) offset) + this.address);
    }

    final void putShort(int offset, short value) {
        unsafe.putShort(((long) offset) + this.address, value);
    }

    final char getChar(int offset) {
        return unsafe.getChar(((long) offset) + this.address);
    }

    final void putChar(int offset, char value) {
        unsafe.putChar(((long) offset) + this.address, value);
    }

    final int getInt(int offset) {
        return unsafe.getInt(((long) offset) + this.address);
    }

    final void putInt(int offset, int value) {
        unsafe.putInt(((long) offset) + this.address, value);
    }

    final long getLong(int offset) {
        return unsafe.getLong(((long) offset) + this.address);
    }

    final void putLong(int offset, long value) {
        unsafe.putLong(((long) offset) + this.address, value);
    }

    final float getFloat(int offset) {
        return unsafe.getFloat(((long) offset) + this.address);
    }

    final void putFloat(int offset, float value) {
        unsafe.putFloat(((long) offset) + this.address, value);
    }

    final double getDouble(int offset) {
        return unsafe.getDouble(((long) offset) + this.address);
    }

    final void putDouble(int offset, double value) {
        unsafe.putDouble(((long) offset) + this.address, value);
    }

    static int addressSize() {
        return unsafe.addressSize();
    }

    static ByteOrder byteOrder() {
        if (byteOrder != null) {
            return byteOrder;
        }
        long a = unsafe.allocateMemory(8);
        try {
            unsafe.putLong(a, 72623859790382856L);
            switch (unsafe.getByte(a)) {
                case (byte) 1:
                    byteOrder = ByteOrder.BIG_ENDIAN;
                    break;
                case (byte) 8:
                    byteOrder = ByteOrder.LITTLE_ENDIAN;
                    break;
                default:
                    if (!-assertionsDisabled) {
                        throw new AssertionError();
                    }
                    break;
            }
            unsafe.freeMemory(a);
            return byteOrder;
        } catch (Throwable th) {
            unsafe.freeMemory(a);
        }
    }

    static int pageSize() {
        if (pageSize == -1) {
            pageSize = unsafe.pageSize();
        }
        return pageSize;
    }
}
