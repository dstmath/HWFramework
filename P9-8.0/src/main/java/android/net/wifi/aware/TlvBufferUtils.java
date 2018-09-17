package android.net.wifi.aware;

import java.nio.BufferOverflowException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
            this.mArrayLength = array == null ? 0 : array.length;
            return this;
        }

        public TlvConstructor allocate(int capacity) {
            this.mArray = new byte[capacity];
            this.mArrayLength = capacity;
            return this;
        }

        public TlvConstructor allocateAndPut(List<byte[]> list) {
            if (list != null) {
                int size = 0;
                for (byte[] field : list) {
                    size += this.mTypeSize + this.mLengthSize;
                    if (field != null) {
                        size += field.length;
                    }
                }
                allocate(size);
                for (byte[] field2 : list) {
                    putByteArray(0, field2);
                }
            }
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
            if (length != 0) {
                System.arraycopy(array, offset, this.mArray, this.mPosition, length);
            }
            this.mPosition += length;
            return this;
        }

        public TlvConstructor putByteArray(int type, byte[] array) {
            return putByteArray(type, array, 0, array == null ? 0 : array.length);
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
            byte[] bytes = null;
            int length = 0;
            if (data != null) {
                bytes = data.getBytes();
                length = bytes.length;
            }
            return putByteArray(type, bytes, 0, length);
        }

        public byte[] getArray() {
            return Arrays.copyOf(this.mArray, getActualLength());
        }

        private int getActualLength() {
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
        public int length;
        public int offset;
        public byte[] refArray;
        public int type;

        /* synthetic */ TlvElement(int type, int length, byte[] refArray, int offset, TlvElement -this4) {
            this(type, length, refArray, offset);
        }

        private TlvElement(int type, int length, byte[] refArray, int offset) {
            this.type = type;
            this.length = length;
            this.refArray = refArray;
            this.offset = offset;
            if (offset + length > refArray.length) {
                throw new BufferOverflowException();
            }
        }

        public byte getByte() {
            if (this.length == 1) {
                return this.refArray[this.offset];
            }
            throw new IllegalArgumentException("Accesing a byte from a TLV element of length " + this.length);
        }

        public short getShort() {
            if (this.length == 2) {
                return Memory.peekShort(this.refArray, this.offset, ByteOrder.BIG_ENDIAN);
            }
            throw new IllegalArgumentException("Accesing a short from a TLV element of length " + this.length);
        }

        public int getInt() {
            if (this.length == 4) {
                return Memory.peekInt(this.refArray, this.offset, ByteOrder.BIG_ENDIAN);
            }
            throw new IllegalArgumentException("Accesing an int from a TLV element of length " + this.length);
        }

        public String getString() {
            return new String(this.refArray, this.offset, this.length);
        }
    }

    public static class TlvIterable implements Iterable<TlvElement> {
        private byte[] mArray;
        private int mArrayLength;
        private int mLengthSize;
        private int mTypeSize;

        public TlvIterable(int typeSize, int lengthSize, byte[] array) {
            int i = 0;
            if (typeSize < 0 || typeSize > 2 || lengthSize <= 0 || lengthSize > 2) {
                throw new IllegalArgumentException("Invalid sizes - typeSize=" + typeSize + ", lengthSize=" + lengthSize);
            }
            this.mTypeSize = typeSize;
            this.mLengthSize = lengthSize;
            this.mArray = array;
            if (array != null) {
                i = array.length;
            }
            this.mArrayLength = i;
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
                    builder.append("T=").append(tlv.type).append(",");
                }
                builder.append("L=").append(tlv.length).append(") ");
                if (tlv.length == 0) {
                    builder.append("<null>");
                } else if (tlv.length == 1) {
                    builder.append(tlv.getByte());
                } else if (tlv.length == 2) {
                    builder.append(tlv.getShort());
                } else if (tlv.length == 4) {
                    builder.append(tlv.getInt());
                } else {
                    builder.append("<bytes>");
                }
                if (tlv.length != 0) {
                    builder.append(" (S='").append(tlv.getString()).append("')");
                }
            }
            builder.append("]");
            return builder.toString();
        }

        public List<byte[]> toList() {
            List<byte[]> list = new ArrayList();
            for (TlvElement tlv : this) {
                list.add(Arrays.copyOfRange(tlv.refArray, tlv.offset, tlv.offset + tlv.length));
            }
            return list;
        }

        public Iterator<TlvElement> iterator() {
            return new Iterator<TlvElement>() {
                private int mOffset = 0;

                public boolean hasNext() {
                    return this.mOffset < TlvIterable.this.mArrayLength;
                }

                public TlvElement next() {
                    if (hasNext()) {
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
                        TlvElement tlv = new TlvElement(type, length, TlvIterable.this.mArray, this.mOffset, null);
                        this.mOffset += length;
                        return tlv;
                    }
                    throw new NoSuchElementException();
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private TlvBufferUtils() {
    }

    public static boolean isValid(byte[] array, int typeSize, int lengthSize) {
        boolean z = true;
        if (typeSize < 0 || typeSize > 2) {
            throw new IllegalArgumentException("Invalid arguments - typeSize must be 0, 1, or 2: typeSize=" + typeSize);
        } else if (lengthSize <= 0 || lengthSize > 2) {
            throw new IllegalArgumentException("Invalid arguments - lengthSize must be 1 or 2: lengthSize=" + lengthSize);
        } else if (array == null) {
            return true;
        } else {
            int nextTlvIndex = 0;
            while ((nextTlvIndex + typeSize) + lengthSize <= array.length) {
                nextTlvIndex += typeSize;
                if (lengthSize == 1) {
                    nextTlvIndex += array[nextTlvIndex] + lengthSize;
                } else {
                    nextTlvIndex += Memory.peekShort(array, nextTlvIndex, ByteOrder.BIG_ENDIAN) + lengthSize;
                }
            }
            if (nextTlvIndex != array.length) {
                z = false;
            }
            return z;
        }
    }
}
