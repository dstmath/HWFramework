package com.android.framework.protobuf;

import com.android.framework.protobuf.ByteString;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

/* access modifiers changed from: package-private */
public final class RopeByteString extends ByteString {
    private static final int[] minLengthByDepth;
    private static final long serialVersionUID = 1;
    private final ByteString left;
    private final int leftLength;
    private final ByteString right;
    private final int totalLength;
    private final int treeDepth;

    static {
        List<Integer> numbers = new ArrayList<>();
        int f1 = 1;
        int f2 = 1;
        while (f2 > 0) {
            numbers.add(Integer.valueOf(f2));
            int temp = f1 + f2;
            f1 = f2;
            f2 = temp;
        }
        numbers.add(Integer.MAX_VALUE);
        minLengthByDepth = new int[numbers.size()];
        int i = 0;
        while (true) {
            int[] iArr = minLengthByDepth;
            if (i < iArr.length) {
                iArr[i] = numbers.get(i).intValue();
                i++;
            } else {
                return;
            }
        }
    }

    private RopeByteString(ByteString left2, ByteString right2) {
        this.left = left2;
        this.right = right2;
        this.leftLength = left2.size();
        this.totalLength = this.leftLength + right2.size();
        this.treeDepth = Math.max(left2.getTreeDepth(), right2.getTreeDepth()) + 1;
    }

    static ByteString concatenate(ByteString left2, ByteString right2) {
        if (right2.size() == 0) {
            return left2;
        }
        if (left2.size() == 0) {
            return right2;
        }
        int newLength = left2.size() + right2.size();
        if (newLength < 128) {
            return concatenateBytes(left2, right2);
        }
        if (left2 instanceof RopeByteString) {
            RopeByteString leftRope = (RopeByteString) left2;
            if (leftRope.right.size() + right2.size() < 128) {
                return new RopeByteString(leftRope.left, concatenateBytes(leftRope.right, right2));
            } else if (leftRope.left.getTreeDepth() > leftRope.right.getTreeDepth() && leftRope.getTreeDepth() > right2.getTreeDepth()) {
                return new RopeByteString(leftRope.left, new RopeByteString(leftRope.right, right2));
            }
        }
        if (newLength >= minLengthByDepth[Math.max(left2.getTreeDepth(), right2.getTreeDepth()) + 1]) {
            return new RopeByteString(left2, right2);
        }
        return new Balancer().balance(left2, right2);
    }

    private static ByteString concatenateBytes(ByteString left2, ByteString right2) {
        int leftSize = left2.size();
        int rightSize = right2.size();
        byte[] bytes = new byte[(leftSize + rightSize)];
        left2.copyTo(bytes, 0, 0, leftSize);
        right2.copyTo(bytes, 0, leftSize, rightSize);
        return ByteString.wrap(bytes);
    }

    static RopeByteString newInstanceForTest(ByteString left2, ByteString right2) {
        return new RopeByteString(left2, right2);
    }

    @Override // com.android.framework.protobuf.ByteString
    public byte byteAt(int index) {
        checkIndex(index, this.totalLength);
        int i = this.leftLength;
        if (index < i) {
            return this.left.byteAt(index);
        }
        return this.right.byteAt(index - i);
    }

    @Override // com.android.framework.protobuf.ByteString
    public int size() {
        return this.totalLength;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.framework.protobuf.ByteString
    public int getTreeDepth() {
        return this.treeDepth;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.framework.protobuf.ByteString
    public boolean isBalanced() {
        return this.totalLength >= minLengthByDepth[this.treeDepth];
    }

    @Override // com.android.framework.protobuf.ByteString
    public ByteString substring(int beginIndex, int endIndex) {
        int length = checkRange(beginIndex, endIndex, this.totalLength);
        if (length == 0) {
            return ByteString.EMPTY;
        }
        if (length == this.totalLength) {
            return this;
        }
        int i = this.leftLength;
        if (endIndex <= i) {
            return this.left.substring(beginIndex, endIndex);
        }
        if (beginIndex >= i) {
            return this.right.substring(beginIndex - i, endIndex - i);
        }
        return new RopeByteString(this.left.substring(beginIndex), this.right.substring(0, endIndex - this.leftLength));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.framework.protobuf.ByteString
    public void copyToInternal(byte[] target, int sourceOffset, int targetOffset, int numberToCopy) {
        int i = sourceOffset + numberToCopy;
        int i2 = this.leftLength;
        if (i <= i2) {
            this.left.copyToInternal(target, sourceOffset, targetOffset, numberToCopy);
        } else if (sourceOffset >= i2) {
            this.right.copyToInternal(target, sourceOffset - i2, targetOffset, numberToCopy);
        } else {
            int leftLength2 = i2 - sourceOffset;
            this.left.copyToInternal(target, sourceOffset, targetOffset, leftLength2);
            this.right.copyToInternal(target, 0, targetOffset + leftLength2, numberToCopy - leftLength2);
        }
    }

    @Override // com.android.framework.protobuf.ByteString
    public void copyTo(ByteBuffer target) {
        this.left.copyTo(target);
        this.right.copyTo(target);
    }

    @Override // com.android.framework.protobuf.ByteString
    public ByteBuffer asReadOnlyByteBuffer() {
        return ByteBuffer.wrap(toByteArray()).asReadOnlyBuffer();
    }

    @Override // com.android.framework.protobuf.ByteString
    public List<ByteBuffer> asReadOnlyByteBufferList() {
        List<ByteBuffer> result = new ArrayList<>();
        PieceIterator pieces = new PieceIterator(this);
        while (pieces.hasNext()) {
            result.add(pieces.next().asReadOnlyByteBuffer());
        }
        return result;
    }

    @Override // com.android.framework.protobuf.ByteString
    public void writeTo(OutputStream outputStream) throws IOException {
        this.left.writeTo(outputStream);
        this.right.writeTo(outputStream);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.framework.protobuf.ByteString
    public void writeToInternal(OutputStream out, int sourceOffset, int numberToWrite) throws IOException {
        int i = sourceOffset + numberToWrite;
        int i2 = this.leftLength;
        if (i <= i2) {
            this.left.writeToInternal(out, sourceOffset, numberToWrite);
        } else if (sourceOffset >= i2) {
            this.right.writeToInternal(out, sourceOffset - i2, numberToWrite);
        } else {
            int numberToWriteInLeft = i2 - sourceOffset;
            this.left.writeToInternal(out, sourceOffset, numberToWriteInLeft);
            this.right.writeToInternal(out, 0, numberToWrite - numberToWriteInLeft);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.framework.protobuf.ByteString
    public void writeTo(ByteOutput output) throws IOException {
        this.left.writeTo(output);
        this.right.writeTo(output);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.framework.protobuf.ByteString
    public String toStringInternal(Charset charset) {
        return new String(toByteArray(), charset);
    }

    @Override // com.android.framework.protobuf.ByteString
    public boolean isValidUtf8() {
        int leftPartial = this.left.partialIsValidUtf8(0, 0, this.leftLength);
        ByteString byteString = this.right;
        if (byteString.partialIsValidUtf8(leftPartial, 0, byteString.size()) == 0) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.framework.protobuf.ByteString
    public int partialIsValidUtf8(int state, int offset, int length) {
        int toIndex = offset + length;
        int i = this.leftLength;
        if (toIndex <= i) {
            return this.left.partialIsValidUtf8(state, offset, length);
        }
        if (offset >= i) {
            return this.right.partialIsValidUtf8(state, offset - i, length);
        }
        int leftLength2 = i - offset;
        return this.right.partialIsValidUtf8(this.left.partialIsValidUtf8(state, offset, leftLength2), 0, length - leftLength2);
    }

    @Override // com.android.framework.protobuf.ByteString
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ByteString)) {
            return false;
        }
        ByteString otherByteString = (ByteString) other;
        if (this.totalLength != otherByteString.size()) {
            return false;
        }
        if (this.totalLength == 0) {
            return true;
        }
        int thisHash = peekCachedHashCode();
        int thatHash = otherByteString.peekCachedHashCode();
        if (thisHash == 0 || thatHash == 0 || thisHash == thatHash) {
            return equalsFragments(otherByteString);
        }
        return false;
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:19:0x001b */
    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:23:0x001b */
    private boolean equalsFragments(ByteString other) {
        boolean stillEqual;
        int thisOffset = 0;
        Iterator<ByteString.LeafByteString> thisIter = new PieceIterator(this);
        ByteString.LeafByteString thisString = thisIter.next();
        int thatOffset = 0;
        Iterator<ByteString.LeafByteString> thatIter = new PieceIterator(other);
        ByteString.LeafByteString thatString = thatIter.next();
        int pos = 0;
        while (true) {
            int thisRemaining = thisString.size() - thisOffset;
            int thatRemaining = thatString.size() - thatOffset;
            int bytesToCompare = Math.min(thisRemaining, thatRemaining);
            if (thisOffset == 0) {
                stillEqual = thisString.equalsRange(thatString, thatOffset, bytesToCompare);
            } else {
                stillEqual = thatString.equalsRange(thisString, thisOffset, bytesToCompare);
            }
            if (!stillEqual) {
                return false;
            }
            pos += bytesToCompare;
            int i = this.totalLength;
            if (pos < i) {
                if (bytesToCompare == thisRemaining) {
                    thisOffset = 0;
                    thisString = thisIter.next();
                } else {
                    thisOffset += bytesToCompare;
                    thisString = thisString;
                }
                if (bytesToCompare == thatRemaining) {
                    thatOffset = 0;
                    thatString = thatIter.next();
                } else {
                    thatOffset += bytesToCompare;
                }
            } else if (pos == i) {
                return true;
            } else {
                throw new IllegalStateException();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.framework.protobuf.ByteString
    public int partialHash(int h, int offset, int length) {
        int toIndex = offset + length;
        int i = this.leftLength;
        if (toIndex <= i) {
            return this.left.partialHash(h, offset, length);
        }
        if (offset >= i) {
            return this.right.partialHash(h, offset - i, length);
        }
        int leftLength2 = i - offset;
        return this.right.partialHash(this.left.partialHash(h, offset, leftLength2), 0, length - leftLength2);
    }

    @Override // com.android.framework.protobuf.ByteString
    public CodedInputStream newCodedInput() {
        return CodedInputStream.newInstance(new RopeInputStream());
    }

    @Override // com.android.framework.protobuf.ByteString
    public InputStream newInput() {
        return new RopeInputStream();
    }

    /* access modifiers changed from: private */
    public static class Balancer {
        private final Stack<ByteString> prefixesStack;

        private Balancer() {
            this.prefixesStack = new Stack<>();
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private ByteString balance(ByteString left, ByteString right) {
            doBalance(left);
            doBalance(right);
            ByteString partialString = this.prefixesStack.pop();
            while (!this.prefixesStack.isEmpty()) {
                partialString = new RopeByteString(this.prefixesStack.pop(), partialString);
            }
            return partialString;
        }

        private void doBalance(ByteString root) {
            if (root.isBalanced()) {
                insert(root);
            } else if (root instanceof RopeByteString) {
                RopeByteString rbs = (RopeByteString) root;
                doBalance(rbs.left);
                doBalance(rbs.right);
            } else {
                throw new IllegalArgumentException("Has a new type of ByteString been created? Found " + root.getClass());
            }
        }

        private void insert(ByteString byteString) {
            int depthBin = getDepthBinForLength(byteString.size());
            int binEnd = RopeByteString.minLengthByDepth[depthBin + 1];
            if (this.prefixesStack.isEmpty() || this.prefixesStack.peek().size() >= binEnd) {
                this.prefixesStack.push(byteString);
                return;
            }
            int binStart = RopeByteString.minLengthByDepth[depthBin];
            ByteString newTree = this.prefixesStack.pop();
            while (!this.prefixesStack.isEmpty() && this.prefixesStack.peek().size() < binStart) {
                newTree = new RopeByteString(this.prefixesStack.pop(), newTree);
            }
            ByteString newTree2 = new RopeByteString(newTree, byteString);
            while (!this.prefixesStack.isEmpty()) {
                if (this.prefixesStack.peek().size() >= RopeByteString.minLengthByDepth[getDepthBinForLength(newTree2.size()) + 1]) {
                    break;
                }
                newTree2 = new RopeByteString(this.prefixesStack.pop(), newTree2);
            }
            this.prefixesStack.push(newTree2);
        }

        private int getDepthBinForLength(int length) {
            int depth = Arrays.binarySearch(RopeByteString.minLengthByDepth, length);
            if (depth < 0) {
                return (-(depth + 1)) - 1;
            }
            return depth;
        }
    }

    /* access modifiers changed from: private */
    public static class PieceIterator implements Iterator<ByteString.LeafByteString> {
        private final Stack<RopeByteString> breadCrumbs;
        private ByteString.LeafByteString next;

        private PieceIterator(ByteString root) {
            this.breadCrumbs = new Stack<>();
            this.next = getLeafByLeft(root);
        }

        private ByteString.LeafByteString getLeafByLeft(ByteString root) {
            ByteString pos = root;
            while (pos instanceof RopeByteString) {
                RopeByteString rbs = (RopeByteString) pos;
                this.breadCrumbs.push(rbs);
                pos = rbs.left;
            }
            return (ByteString.LeafByteString) pos;
        }

        private ByteString.LeafByteString getNextNonEmptyLeaf() {
            while (!this.breadCrumbs.isEmpty()) {
                ByteString.LeafByteString result = getLeafByLeft(this.breadCrumbs.pop().right);
                if (!result.isEmpty()) {
                    return result;
                }
            }
            return null;
        }

        public boolean hasNext() {
            return this.next != null;
        }

        @Override // java.util.Iterator
        public ByteString.LeafByteString next() {
            if (this.next != null) {
                ByteString.LeafByteString result = this.next;
                this.next = getNextNonEmptyLeaf();
                return result;
            }
            throw new NoSuchElementException();
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: package-private */
    public Object writeReplace() {
        return ByteString.wrap(toByteArray());
    }

    private void readObject(ObjectInputStream in) throws IOException {
        throw new InvalidObjectException("RopeByteStream instances are not to be serialized directly");
    }

    private class RopeInputStream extends InputStream {
        private ByteString.LeafByteString currentPiece;
        private int currentPieceIndex;
        private int currentPieceOffsetInRope;
        private int currentPieceSize;
        private int mark;
        private PieceIterator pieceIterator;

        public RopeInputStream() {
            initialize();
        }

        @Override // java.io.InputStream
        public int read(byte[] b, int offset, int length) {
            if (b == null) {
                throw new NullPointerException();
            } else if (offset >= 0 && length >= 0 && length <= b.length - offset) {
                return readSkipInternal(b, offset, length);
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        @Override // java.io.InputStream
        public long skip(long length) {
            if (length >= 0) {
                if (length > 2147483647L) {
                    length = 2147483647L;
                }
                return (long) readSkipInternal(null, 0, (int) length);
            }
            throw new IndexOutOfBoundsException();
        }

        private int readSkipInternal(byte[] b, int offset, int length) {
            int bytesRemaining = length;
            while (true) {
                if (bytesRemaining <= 0) {
                    break;
                }
                advanceIfCurrentPieceFullyRead();
                if (this.currentPiece != null) {
                    int count = Math.min(this.currentPieceSize - this.currentPieceIndex, bytesRemaining);
                    if (b != null) {
                        this.currentPiece.copyTo(b, this.currentPieceIndex, offset, count);
                        offset += count;
                    }
                    this.currentPieceIndex += count;
                    bytesRemaining -= count;
                } else if (bytesRemaining == length) {
                    return -1;
                }
            }
            return length - bytesRemaining;
        }

        @Override // java.io.InputStream
        public int read() throws IOException {
            advanceIfCurrentPieceFullyRead();
            ByteString.LeafByteString leafByteString = this.currentPiece;
            if (leafByteString == null) {
                return -1;
            }
            int i = this.currentPieceIndex;
            this.currentPieceIndex = i + 1;
            return leafByteString.byteAt(i) & 255;
        }

        @Override // java.io.InputStream
        public int available() throws IOException {
            return RopeByteString.this.size() - (this.currentPieceOffsetInRope + this.currentPieceIndex);
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readAheadLimit) {
            this.mark = this.currentPieceOffsetInRope + this.currentPieceIndex;
        }

        @Override // java.io.InputStream
        public synchronized void reset() {
            initialize();
            readSkipInternal(null, 0, this.mark);
        }

        private void initialize() {
            this.pieceIterator = new PieceIterator(RopeByteString.this);
            this.currentPiece = this.pieceIterator.next();
            this.currentPieceSize = this.currentPiece.size();
            this.currentPieceIndex = 0;
            this.currentPieceOffsetInRope = 0;
        }

        private void advanceIfCurrentPieceFullyRead() {
            int i;
            if (this.currentPiece != null && this.currentPieceIndex == (i = this.currentPieceSize)) {
                this.currentPieceOffsetInRope += i;
                this.currentPieceIndex = 0;
                if (this.pieceIterator.hasNext()) {
                    this.currentPiece = this.pieceIterator.next();
                    this.currentPieceSize = this.currentPiece.size();
                    return;
                }
                this.currentPiece = null;
                this.currentPieceSize = 0;
            }
        }
    }
}
