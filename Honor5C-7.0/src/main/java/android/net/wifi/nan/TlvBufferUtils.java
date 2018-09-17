package android.net.wifi.nan;

import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.util.Iterator;
import libcore.io.Memory;

public class TlvBufferUtils {

    public static class TlvConstructor {
        private byte[] mArray;
        private int mArrayLength;
        private int mLengthSize;
        private int mPosition;
        private int mTypeSize;

        public TlvConstructor(int typeSize, int lengthSize) {
            if (typeSize < 0 || typeSize > 2 || lengthSize <= 0 || lengthSize > 2) {
                throw new IllegalArgumentException("Invalid sizes - typeSize=" + typeSize + ", lengthSize=" + lengthSize);
            }
            this.mTypeSize = typeSize;
            this.mLengthSize = lengthSize;
        }

        public TlvConstructor wrap(byte[] array) {
            this.mArray = array;
            this.mArrayLength = array.length;
            return this;
        }

        public TlvConstructor allocate(int capacity) {
            this.mArray = new byte[capacity];
            this.mArrayLength = capacity;
            return this;
        }

        public TlvConstructor putByte(int type, byte b) {
            checkLength(1);
            addHeader(type, 1);
            byte[] bArr = this.mArray;
            int i = this.mPosition;
            this.mPosition = i + 1;
            bArr[i] = b;
            return this;
        }

        public TlvConstructor putByteArray(int type, byte[] array, int offset, int length) {
            checkLength(length);
            addHeader(type, length);
            System.arraycopy(array, offset, this.mArray, this.mPosition, length);
            this.mPosition += length;
            return this;
        }

        public TlvConstructor putByteArray(int type, byte[] array) {
            return putByteArray(type, array, 0, array.length);
        }

        public TlvConstructor putZeroLengthElement(int type) {
            checkLength(0);
            addHeader(type, 0);
            return this;
        }

        public TlvConstructor putShort(int type, short data) {
            checkLength(2);
            addHeader(type, 2);
            Memory.pokeShort(this.mArray, this.mPosition, data, ByteOrder.BIG_ENDIAN);
            this.mPosition += 2;
            return this;
        }

        public TlvConstructor putInt(int type, int data) {
            checkLength(4);
            addHeader(type, 4);
            Memory.pokeInt(this.mArray, this.mPosition, data, ByteOrder.BIG_ENDIAN);
            this.mPosition += 4;
            return this;
        }

        public TlvConstructor putString(int type, String data) {
            return putByteArray(type, data.getBytes(), 0, data.length());
        }

        public byte[] getArray() {
            return this.mArray;
        }

        public int getActualLength() {
            return this.mPosition;
        }

        private void checkLength(int dataLength) {
            if (((this.mPosition + this.mTypeSize) + this.mLengthSize) + dataLength > this.mArrayLength) {
                throw new BufferOverflowException();
            }
        }

        private void addHeader(int type, int length) {
            if (this.mTypeSize == 1) {
                this.mArray[this.mPosition] = (byte) type;
            } else if (this.mTypeSize == 2) {
                Memory.pokeShort(this.mArray, this.mPosition, (short) type, ByteOrder.BIG_ENDIAN);
            }
            this.mPosition += this.mTypeSize;
            if (this.mLengthSize == 1) {
                this.mArray[this.mPosition] = (byte) length;
            } else if (this.mLengthSize == 2) {
                Memory.pokeShort(this.mArray, this.mPosition, (short) length, ByteOrder.BIG_ENDIAN);
            }
            this.mPosition += this.mLengthSize;
        }
    }

    public static class TlvElement {
        public int mLength;
        public int mOffset;
        public byte[] mRefArray;
        public int mType;

        private TlvElement(int type, int length, byte[] refArray, int offset) {
            this.mType = type;
            this.mLength = length;
            this.mRefArray = refArray;
            this.mOffset = offset;
        }

        public byte getByte() {
            if (this.mLength == 1) {
                return this.mRefArray[this.mOffset];
            }
            throw new IllegalArgumentException("Accesing a byte from a TLV element of length " + this.mLength);
        }

        public short getShort() {
            if (this.mLength == 2) {
                return Memory.peekShort(this.mRefArray, this.mOffset, ByteOrder.BIG_ENDIAN);
            }
            throw new IllegalArgumentException("Accesing a short from a TLV element of length " + this.mLength);
        }

        public int getInt() {
            if (this.mLength == 4) {
                return Memory.peekInt(this.mRefArray, this.mOffset, ByteOrder.BIG_ENDIAN);
            }
            throw new IllegalArgumentException("Accesing an int from a TLV element of length " + this.mLength);
        }

        public String getString() {
            return new String(this.mRefArray, this.mOffset, this.mLength);
        }
    }

    public static class TlvIterable implements Iterable<TlvElement> {
        private byte[] mArray;
        private int mArrayLength;
        private int mLengthSize;
        private int mTypeSize;

        public TlvIterable(int typeSize, int lengthSize, byte[] array, int length) {
            if (typeSize < 0 || typeSize > 2 || lengthSize <= 0 || lengthSize > 2) {
                throw new IllegalArgumentException("Invalid sizes - typeSize=" + typeSize + ", lengthSize=" + lengthSize);
            }
            this.mTypeSize = typeSize;
            this.mLengthSize = lengthSize;
            this.mArray = array;
            this.mArrayLength = length;
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[");
            boolean first = true;
            for (TlvElement tlv : this) {
                if (!first) {
                    builder.append(",");
                }
                first = false;
                builder.append(" (");
                if (this.mTypeSize != 0) {
                    builder.append("T=").append(tlv.mType).append(",");
                }
                builder.append("L=").append(tlv.mLength).append(") ");
                if (tlv.mLength == 0) {
                    builder.append("<null>");
                } else if (tlv.mLength == 1) {
                    builder.append(tlv.getByte());
                } else if (tlv.mLength == 2) {
                    builder.append(tlv.getShort());
                } else if (tlv.mLength == 4) {
                    builder.append(tlv.getInt());
                } else {
                    builder.append("<bytes>");
                }
                if (tlv.mLength != 0) {
                    builder.append(" (S='").append(tlv.getString()).append("')");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        public Iterator<TlvElement> iterator() {
            return new Iterator<TlvElement>() {
                private int mOffset;

                {
                    this.mOffset = 0;
                }

                public boolean hasNext() {
                    return this.mOffset < TlvIterable.this.mArrayLength;
                }

                public TlvElement next() {
                    int type = 0;
                    if (TlvIterable.this.mTypeSize == 1) {
                        type = TlvIterable.this.mArray[this.mOffset];
                    } else if (TlvIterable.this.mTypeSize == 2) {
                        type = Memory.peekShort(TlvIterable.this.mArray, this.mOffset, ByteOrder.BIG_ENDIAN);
                    }
                    this.mOffset += TlvIterable.this.mTypeSize;
                    int length = 0;
                    if (TlvIterable.this.mLengthSize == 1) {
                        length = TlvIterable.this.mArray[this.mOffset];
                    } else if (TlvIterable.this.mLengthSize == 2) {
                        length = Memory.peekShort(TlvIterable.this.mArray, this.mOffset, ByteOrder.BIG_ENDIAN);
                    }
                    this.mOffset += TlvIterable.this.mLengthSize;
                    TlvElement tlv = new TlvElement(length, TlvIterable.this.mArray, this.mOffset, null);
                    this.mOffset += length;
                    return tlv;
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private TlvBufferUtils() {
    }
}
