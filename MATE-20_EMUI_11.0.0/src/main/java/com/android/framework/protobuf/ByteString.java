package com.android.framework.protobuf;

import com.android.internal.telephony.cdma.HwCustPlusAndIddNddConvertUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class ByteString implements Iterable<Byte>, Serializable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    static final int CONCATENATE_BY_COPY_SIZE = 128;
    public static final ByteString EMPTY = new LiteralByteString(Internal.EMPTY_BYTE_ARRAY);
    static final int MAX_READ_FROM_CHUNK_SIZE = 8192;
    static final int MIN_READ_FROM_CHUNK_SIZE = 256;
    private static final ByteArrayCopier byteArrayCopier;
    private int hash = 0;

    /* access modifiers changed from: private */
    public interface ByteArrayCopier {
        byte[] copyFrom(byte[] bArr, int i, int i2);
    }

    public interface ByteIterator extends Iterator<Byte> {
        byte nextByte();
    }

    public abstract ByteBuffer asReadOnlyByteBuffer();

    public abstract List<ByteBuffer> asReadOnlyByteBufferList();

    public abstract byte byteAt(int i);

    public abstract void copyTo(ByteBuffer byteBuffer);

    /* access modifiers changed from: protected */
    public abstract void copyToInternal(byte[] bArr, int i, int i2, int i3);

    @Override // java.lang.Object
    public abstract boolean equals(Object obj);

    /* access modifiers changed from: protected */
    public abstract int getTreeDepth();

    /* access modifiers changed from: protected */
    public abstract boolean isBalanced();

    public abstract boolean isValidUtf8();

    public abstract CodedInputStream newCodedInput();

    public abstract InputStream newInput();

    /* access modifiers changed from: protected */
    public abstract int partialHash(int i, int i2, int i3);

    /* access modifiers changed from: protected */
    public abstract int partialIsValidUtf8(int i, int i2, int i3);

    public abstract int size();

    public abstract ByteString substring(int i, int i2);

    /* access modifiers changed from: protected */
    public abstract String toStringInternal(Charset charset);

    /* access modifiers changed from: package-private */
    public abstract void writeTo(ByteOutput byteOutput) throws IOException;

    public abstract void writeTo(OutputStream outputStream) throws IOException;

    /* access modifiers changed from: package-private */
    public abstract void writeToInternal(OutputStream outputStream, int i, int i2) throws IOException;

    static {
        boolean isAndroid = true;
        try {
            Class.forName("android.content.Context");
        } catch (ClassNotFoundException e) {
            isAndroid = false;
        }
        byteArrayCopier = isAndroid ? new SystemByteArrayCopier() : new ArraysByteArrayCopier();
    }

    private static final class SystemByteArrayCopier implements ByteArrayCopier {
        private SystemByteArrayCopier() {
        }

        @Override // com.android.framework.protobuf.ByteString.ByteArrayCopier
        public byte[] copyFrom(byte[] bytes, int offset, int size) {
            byte[] copy = new byte[size];
            System.arraycopy(bytes, offset, copy, 0, size);
            return copy;
        }
    }

    private static final class ArraysByteArrayCopier implements ByteArrayCopier {
        private ArraysByteArrayCopier() {
        }

        @Override // com.android.framework.protobuf.ByteString.ByteArrayCopier
        public byte[] copyFrom(byte[] bytes, int offset, int size) {
            return Arrays.copyOfRange(bytes, offset, offset + size);
        }
    }

    ByteString() {
    }

    /* Return type fixed from 'com.android.framework.protobuf.ByteString$ByteIterator' to match base method */
    @Override // java.lang.Iterable
    public final Iterator<Byte> iterator() {
        return new ByteIterator() {
            /* class com.android.framework.protobuf.ByteString.AnonymousClass1 */
            private final int limit = ByteString.this.size();
            private int position = 0;

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.position < this.limit;
            }

            @Override // java.util.Iterator
            public Byte next() {
                return Byte.valueOf(nextByte());
            }

            @Override // com.android.framework.protobuf.ByteString.ByteIterator
            public byte nextByte() {
                try {
                    ByteString byteString = ByteString.this;
                    int i = this.position;
                    this.position = i + 1;
                    return byteString.byteAt(i);
                } catch (IndexOutOfBoundsException e) {
                    throw new NoSuchElementException(e.getMessage());
                }
            }

            @Override // java.util.Iterator
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public final boolean isEmpty() {
        return size() == 0;
    }

    public final ByteString substring(int beginIndex) {
        return substring(beginIndex, size());
    }

    public final boolean startsWith(ByteString prefix) {
        if (size() < prefix.size() || !substring(0, prefix.size()).equals(prefix)) {
            return false;
        }
        return true;
    }

    public final boolean endsWith(ByteString suffix) {
        return size() >= suffix.size() && substring(size() - suffix.size()).equals(suffix);
    }

    public static ByteString copyFrom(byte[] bytes, int offset, int size) {
        return new LiteralByteString(byteArrayCopier.copyFrom(bytes, offset, size));
    }

    public static ByteString copyFrom(byte[] bytes) {
        return copyFrom(bytes, 0, bytes.length);
    }

    static ByteString wrap(byte[] bytes) {
        return new LiteralByteString(bytes);
    }

    static ByteString wrap(byte[] bytes, int offset, int length) {
        return new BoundedByteString(bytes, offset, length);
    }

    public static ByteString copyFrom(ByteBuffer bytes, int size) {
        byte[] copy = new byte[size];
        bytes.get(copy);
        return new LiteralByteString(copy);
    }

    public static ByteString copyFrom(ByteBuffer bytes) {
        return copyFrom(bytes, bytes.remaining());
    }

    public static ByteString copyFrom(String text, String charsetName) throws UnsupportedEncodingException {
        return new LiteralByteString(text.getBytes(charsetName));
    }

    public static ByteString copyFrom(String text, Charset charset) {
        return new LiteralByteString(text.getBytes(charset));
    }

    public static ByteString copyFromUtf8(String text) {
        return new LiteralByteString(text.getBytes(Internal.UTF_8));
    }

    public static ByteString readFrom(InputStream streamToDrain) throws IOException {
        return readFrom(streamToDrain, 256, 8192);
    }

    public static ByteString readFrom(InputStream streamToDrain, int chunkSize) throws IOException {
        return readFrom(streamToDrain, chunkSize, chunkSize);
    }

    public static ByteString readFrom(InputStream streamToDrain, int minChunkSize, int maxChunkSize) throws IOException {
        Collection<ByteString> results = new ArrayList<>();
        int chunkSize = minChunkSize;
        while (true) {
            ByteString chunk = readChunk(streamToDrain, chunkSize);
            if (chunk == null) {
                return copyFrom(results);
            }
            results.add(chunk);
            chunkSize = Math.min(chunkSize * 2, maxChunkSize);
        }
    }

    private static ByteString readChunk(InputStream in, int chunkSize) throws IOException {
        byte[] buf = new byte[chunkSize];
        int bytesRead = 0;
        while (bytesRead < chunkSize) {
            int count = in.read(buf, bytesRead, chunkSize - bytesRead);
            if (count == -1) {
                break;
            }
            bytesRead += count;
        }
        if (bytesRead == 0) {
            return null;
        }
        return copyFrom(buf, 0, bytesRead);
    }

    public final ByteString concat(ByteString other) {
        if (Integer.MAX_VALUE - size() >= other.size()) {
            return RopeByteString.concatenate(this, other);
        }
        throw new IllegalArgumentException("ByteString would be too long: " + size() + HwCustPlusAndIddNddConvertUtils.PLUS_PREFIX + other.size());
    }

    public static ByteString copyFrom(Iterable<ByteString> byteStrings) {
        int tempSize;
        if (!(byteStrings instanceof Collection)) {
            tempSize = 0;
            Iterator<ByteString> iter = byteStrings.iterator();
            while (iter.hasNext()) {
                iter.next();
                tempSize++;
            }
        } else {
            tempSize = ((Collection) byteStrings).size();
        }
        if (tempSize == 0) {
            return EMPTY;
        }
        return balancedConcat(byteStrings.iterator(), tempSize);
    }

    /* JADX INFO: Multiple debug info for r0v1 int: [D('halfLength' int), D('result' com.android.framework.protobuf.ByteString)] */
    /* JADX INFO: Multiple debug info for r0v2 com.android.framework.protobuf.ByteString: [D('halfLength' int), D('result' com.android.framework.protobuf.ByteString)] */
    private static ByteString balancedConcat(Iterator<ByteString> iterator, int length) {
        if (length == 1) {
            return iterator.next();
        }
        int halfLength = length >>> 1;
        return balancedConcat(iterator, halfLength).concat(balancedConcat(iterator, length - halfLength));
    }

    public void copyTo(byte[] target, int offset) {
        copyTo(target, 0, offset, size());
    }

    public final void copyTo(byte[] target, int sourceOffset, int targetOffset, int numberToCopy) {
        checkRange(sourceOffset, sourceOffset + numberToCopy, size());
        checkRange(targetOffset, targetOffset + numberToCopy, target.length);
        if (numberToCopy > 0) {
            copyToInternal(target, sourceOffset, targetOffset, numberToCopy);
        }
    }

    public final byte[] toByteArray() {
        int size = size();
        if (size == 0) {
            return Internal.EMPTY_BYTE_ARRAY;
        }
        byte[] result = new byte[size];
        copyToInternal(result, 0, 0, size);
        return result;
    }

    /* access modifiers changed from: package-private */
    public final void writeTo(OutputStream out, int sourceOffset, int numberToWrite) throws IOException {
        checkRange(sourceOffset, sourceOffset + numberToWrite, size());
        if (numberToWrite > 0) {
            writeToInternal(out, sourceOffset, numberToWrite);
        }
    }

    public final String toString(String charsetName) throws UnsupportedEncodingException {
        try {
            return toString(Charset.forName(charsetName));
        } catch (UnsupportedCharsetException e) {
            UnsupportedEncodingException exception = new UnsupportedEncodingException(charsetName);
            exception.initCause(e);
            throw exception;
        }
    }

    public final String toString(Charset charset) {
        return size() == 0 ? "" : toStringInternal(charset);
    }

    public final String toStringUtf8() {
        return toString(Internal.UTF_8);
    }

    static abstract class LeafByteString extends ByteString {
        /* access modifiers changed from: package-private */
        public abstract boolean equalsRange(ByteString byteString, int i, int i2);

        LeafByteString() {
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString
        public final int getTreeDepth() {
            return 0;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString
        public final boolean isBalanced() {
            return true;
        }
    }

    @Override // java.lang.Object
    public final int hashCode() {
        int h = this.hash;
        if (h == 0) {
            int size = size();
            h = partialHash(size, 0, size);
            if (h == 0) {
                h = 1;
            }
            this.hash = h;
        }
        return h;
    }

    public static Output newOutput(int initialCapacity) {
        return new Output(initialCapacity);
    }

    public static Output newOutput() {
        return new Output(128);
    }

    public static final class Output extends OutputStream {
        private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
        private byte[] buffer;
        private int bufferPos;
        private final ArrayList<ByteString> flushedBuffers;
        private int flushedBuffersTotalBytes;
        private final int initialCapacity;

        Output(int initialCapacity2) {
            if (initialCapacity2 >= 0) {
                this.initialCapacity = initialCapacity2;
                this.flushedBuffers = new ArrayList<>();
                this.buffer = new byte[initialCapacity2];
                return;
            }
            throw new IllegalArgumentException("Buffer size < 0");
        }

        @Override // java.io.OutputStream
        public synchronized void write(int b) {
            if (this.bufferPos == this.buffer.length) {
                flushFullBuffer(1);
            }
            byte[] bArr = this.buffer;
            int i = this.bufferPos;
            this.bufferPos = i + 1;
            bArr[i] = (byte) b;
        }

        @Override // java.io.OutputStream
        public synchronized void write(byte[] b, int offset, int length) {
            if (length <= this.buffer.length - this.bufferPos) {
                System.arraycopy(b, offset, this.buffer, this.bufferPos, length);
                this.bufferPos += length;
            } else {
                int copySize = this.buffer.length - this.bufferPos;
                System.arraycopy(b, offset, this.buffer, this.bufferPos, copySize);
                int length2 = length - copySize;
                flushFullBuffer(length2);
                System.arraycopy(b, offset + copySize, this.buffer, 0, length2);
                this.bufferPos = length2;
            }
        }

        public synchronized ByteString toByteString() {
            flushLastBuffer();
            return ByteString.copyFrom(this.flushedBuffers);
        }

        private byte[] copyArray(byte[] buffer2, int length) {
            byte[] result = new byte[length];
            System.arraycopy(buffer2, 0, result, 0, Math.min(buffer2.length, length));
            return result;
        }

        public void writeTo(OutputStream out) throws IOException {
            ByteString[] cachedFlushBuffers;
            byte[] cachedBuffer;
            int cachedBufferPos;
            synchronized (this) {
                cachedFlushBuffers = (ByteString[]) this.flushedBuffers.toArray(new ByteString[this.flushedBuffers.size()]);
                cachedBuffer = this.buffer;
                cachedBufferPos = this.bufferPos;
            }
            for (ByteString byteString : cachedFlushBuffers) {
                byteString.writeTo(out);
            }
            out.write(copyArray(cachedBuffer, cachedBufferPos));
        }

        public synchronized int size() {
            return this.flushedBuffersTotalBytes + this.bufferPos;
        }

        public synchronized void reset() {
            this.flushedBuffers.clear();
            this.flushedBuffersTotalBytes = 0;
            this.bufferPos = 0;
        }

        @Override // java.lang.Object
        public String toString() {
            return String.format("<ByteString.Output@%s size=%d>", Integer.toHexString(System.identityHashCode(this)), Integer.valueOf(size()));
        }

        private void flushFullBuffer(int minSize) {
            this.flushedBuffers.add(new LiteralByteString(this.buffer));
            this.flushedBuffersTotalBytes += this.buffer.length;
            this.buffer = new byte[Math.max(this.initialCapacity, Math.max(minSize, this.flushedBuffersTotalBytes >>> 1))];
            this.bufferPos = 0;
        }

        private void flushLastBuffer() {
            int i = this.bufferPos;
            byte[] bArr = this.buffer;
            if (i >= bArr.length) {
                this.flushedBuffers.add(new LiteralByteString(bArr));
                this.buffer = EMPTY_BYTE_ARRAY;
            } else if (i > 0) {
                this.flushedBuffers.add(new LiteralByteString(copyArray(bArr, i)));
            }
            this.flushedBuffersTotalBytes += this.bufferPos;
            this.bufferPos = 0;
        }
    }

    static CodedBuilder newCodedBuilder(int size) {
        return new CodedBuilder(size);
    }

    /* access modifiers changed from: package-private */
    public static final class CodedBuilder {
        private final byte[] buffer;
        private final CodedOutputStream output;

        private CodedBuilder(int size) {
            this.buffer = new byte[size];
            this.output = CodedOutputStream.newInstance(this.buffer);
        }

        public ByteString build() {
            this.output.checkNoSpaceLeft();
            return new LiteralByteString(this.buffer);
        }

        public CodedOutputStream getCodedOutput() {
            return this.output;
        }
    }

    /* access modifiers changed from: protected */
    public final int peekCachedHashCode() {
        return this.hash;
    }

    static void checkIndex(int index, int size) {
        if (((size - (index + 1)) | index) >= 0) {
            return;
        }
        if (index < 0) {
            throw new ArrayIndexOutOfBoundsException("Index < 0: " + index);
        }
        throw new ArrayIndexOutOfBoundsException("Index > length: " + index + ", " + size);
    }

    static int checkRange(int startIndex, int endIndex, int size) {
        int length = endIndex - startIndex;
        if ((startIndex | endIndex | length | (size - endIndex)) >= 0) {
            return length;
        }
        if (startIndex < 0) {
            throw new IndexOutOfBoundsException("Beginning index: " + startIndex + " < 0");
        } else if (endIndex < startIndex) {
            throw new IndexOutOfBoundsException("Beginning index larger than ending index: " + startIndex + ", " + endIndex);
        } else {
            throw new IndexOutOfBoundsException("End index: " + endIndex + " >= " + size);
        }
    }

    @Override // java.lang.Object
    public final String toString() {
        return String.format("<ByteString@%s size=%d>", Integer.toHexString(System.identityHashCode(this)), Integer.valueOf(size()));
    }

    /* access modifiers changed from: private */
    public static class LiteralByteString extends LeafByteString {
        private static final long serialVersionUID = 1;
        protected final byte[] bytes;

        LiteralByteString(byte[] bytes2) {
            this.bytes = bytes2;
        }

        @Override // com.android.framework.protobuf.ByteString
        public byte byteAt(int index) {
            return this.bytes[index];
        }

        @Override // com.android.framework.protobuf.ByteString
        public int size() {
            return this.bytes.length;
        }

        @Override // com.android.framework.protobuf.ByteString
        public final ByteString substring(int beginIndex, int endIndex) {
            int length = checkRange(beginIndex, endIndex, size());
            if (length == 0) {
                return ByteString.EMPTY;
            }
            return new BoundedByteString(this.bytes, getOffsetIntoBytes() + beginIndex, length);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString
        public void copyToInternal(byte[] target, int sourceOffset, int targetOffset, int numberToCopy) {
            System.arraycopy(this.bytes, sourceOffset, target, targetOffset, numberToCopy);
        }

        @Override // com.android.framework.protobuf.ByteString
        public final void copyTo(ByteBuffer target) {
            target.put(this.bytes, getOffsetIntoBytes(), size());
        }

        @Override // com.android.framework.protobuf.ByteString
        public final ByteBuffer asReadOnlyByteBuffer() {
            return ByteBuffer.wrap(this.bytes, getOffsetIntoBytes(), size()).asReadOnlyBuffer();
        }

        @Override // com.android.framework.protobuf.ByteString
        public final List<ByteBuffer> asReadOnlyByteBufferList() {
            return Collections.singletonList(asReadOnlyByteBuffer());
        }

        @Override // com.android.framework.protobuf.ByteString
        public final void writeTo(OutputStream outputStream) throws IOException {
            outputStream.write(toByteArray());
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.framework.protobuf.ByteString
        public final void writeToInternal(OutputStream outputStream, int sourceOffset, int numberToWrite) throws IOException {
            outputStream.write(this.bytes, getOffsetIntoBytes() + sourceOffset, numberToWrite);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.framework.protobuf.ByteString
        public final void writeTo(ByteOutput output) throws IOException {
            output.writeLazy(this.bytes, getOffsetIntoBytes(), size());
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString
        public final String toStringInternal(Charset charset) {
            return new String(this.bytes, getOffsetIntoBytes(), size(), charset);
        }

        @Override // com.android.framework.protobuf.ByteString
        public final boolean isValidUtf8() {
            int offset = getOffsetIntoBytes();
            return Utf8.isValidUtf8(this.bytes, offset, size() + offset);
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString
        public final int partialIsValidUtf8(int state, int offset, int length) {
            int index = getOffsetIntoBytes() + offset;
            return Utf8.partialIsValidUtf8(state, this.bytes, index, index + length);
        }

        @Override // com.android.framework.protobuf.ByteString, java.lang.Object
        public final boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof ByteString) || size() != ((ByteString) other).size()) {
                return false;
            }
            if (size() == 0) {
                return true;
            }
            if (!(other instanceof LiteralByteString)) {
                return other.equals(this);
            }
            int thisHash = peekCachedHashCode();
            int thatHash = ((LiteralByteString) other).peekCachedHashCode();
            if (thisHash == 0 || thatHash == 0 || thisHash == thatHash) {
                return equalsRange((LiteralByteString) other, 0, size());
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.framework.protobuf.ByteString.LeafByteString
        public final boolean equalsRange(ByteString other, int offset, int length) {
            if (length > other.size()) {
                throw new IllegalArgumentException("Length too large: " + length + size());
            } else if (offset + length > other.size()) {
                throw new IllegalArgumentException("Ran off end of other: " + offset + ", " + length + ", " + other.size());
            } else if (!(other instanceof LiteralByteString)) {
                return other.substring(offset, offset + length).equals(substring(0, length));
            } else {
                LiteralByteString lbsOther = (LiteralByteString) other;
                byte[] thisBytes = this.bytes;
                byte[] otherBytes = lbsOther.bytes;
                int thisLimit = getOffsetIntoBytes() + length;
                int thisIndex = getOffsetIntoBytes();
                int otherIndex = lbsOther.getOffsetIntoBytes() + offset;
                while (thisIndex < thisLimit) {
                    if (thisBytes[thisIndex] != otherBytes[otherIndex]) {
                        return false;
                    }
                    thisIndex++;
                    otherIndex++;
                }
                return true;
            }
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString
        public final int partialHash(int h, int offset, int length) {
            return Internal.partialHash(h, this.bytes, getOffsetIntoBytes() + offset, length);
        }

        @Override // com.android.framework.protobuf.ByteString
        public final InputStream newInput() {
            return new ByteArrayInputStream(this.bytes, getOffsetIntoBytes(), size());
        }

        @Override // com.android.framework.protobuf.ByteString
        public final CodedInputStream newCodedInput() {
            return CodedInputStream.newInstance(this.bytes, getOffsetIntoBytes(), size(), true);
        }

        /* access modifiers changed from: protected */
        public int getOffsetIntoBytes() {
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public static final class BoundedByteString extends LiteralByteString {
        private static final long serialVersionUID = 1;
        private final int bytesLength;
        private final int bytesOffset;

        BoundedByteString(byte[] bytes, int offset, int length) {
            super(bytes);
            checkRange(offset, offset + length, bytes.length);
            this.bytesOffset = offset;
            this.bytesLength = length;
        }

        @Override // com.android.framework.protobuf.ByteString.LiteralByteString, com.android.framework.protobuf.ByteString
        public byte byteAt(int index) {
            checkIndex(index, size());
            return this.bytes[this.bytesOffset + index];
        }

        @Override // com.android.framework.protobuf.ByteString.LiteralByteString, com.android.framework.protobuf.ByteString
        public int size() {
            return this.bytesLength;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString.LiteralByteString
        public int getOffsetIntoBytes() {
            return this.bytesOffset;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.framework.protobuf.ByteString.LiteralByteString, com.android.framework.protobuf.ByteString
        public void copyToInternal(byte[] target, int sourceOffset, int targetOffset, int numberToCopy) {
            System.arraycopy(this.bytes, getOffsetIntoBytes() + sourceOffset, target, targetOffset, numberToCopy);
        }

        /* access modifiers changed from: package-private */
        public Object writeReplace() {
            return ByteString.wrap(toByteArray());
        }

        private void readObject(ObjectInputStream in) throws IOException {
            throw new InvalidObjectException("BoundedByteStream instances are not to be serialized directly");
        }
    }
}
