package com.android.okhttp.okio;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

final class SegmentedByteString extends ByteString {
    final transient int[] directory;
    final transient byte[][] segments;

    SegmentedByteString(Buffer buffer, int byteCount) {
        super(null);
        Util.checkOffsetAndCount(buffer.size, 0, (long) byteCount);
        int offset = 0;
        int segmentCount = 0;
        Segment s = buffer.head;
        while (offset < byteCount) {
            if (s.limit == s.pos) {
                throw new AssertionError("s.limit == s.pos");
            }
            offset += s.limit - s.pos;
            segmentCount++;
            s = s.next;
        }
        this.segments = new byte[segmentCount][];
        this.directory = new int[(segmentCount * 2)];
        offset = 0;
        segmentCount = 0;
        s = buffer.head;
        while (offset < byteCount) {
            this.segments[segmentCount] = s.data;
            offset += s.limit - s.pos;
            this.directory[segmentCount] = offset;
            this.directory[this.segments.length + segmentCount] = s.pos;
            s.shared = true;
            segmentCount++;
            s = s.next;
        }
    }

    public String utf8() {
        return toByteString().utf8();
    }

    public String base64() {
        return toByteString().base64();
    }

    public String hex() {
        return toByteString().hex();
    }

    public ByteString toAsciiLowercase() {
        return toByteString().toAsciiLowercase();
    }

    public ByteString toAsciiUppercase() {
        return toByteString().toAsciiUppercase();
    }

    public ByteString md5() {
        return toByteString().md5();
    }

    public ByteString sha256() {
        return toByteString().sha256();
    }

    public String base64Url() {
        return toByteString().base64Url();
    }

    public ByteString substring(int beginIndex) {
        return toByteString().substring(beginIndex);
    }

    public ByteString substring(int beginIndex, int endIndex) {
        return toByteString().substring(beginIndex, endIndex);
    }

    public byte getByte(int pos) {
        Util.checkOffsetAndCount((long) this.directory[this.segments.length - 1], (long) pos, 1);
        int segment = segment(pos);
        return this.segments[segment][(pos - (segment == 0 ? 0 : this.directory[segment - 1])) + this.directory[this.segments.length + segment]];
    }

    private int segment(int pos) {
        int i = Arrays.binarySearch(this.directory, 0, this.segments.length, pos + 1);
        return i >= 0 ? i : ~i;
    }

    public int size() {
        return this.directory[this.segments.length - 1];
    }

    public byte[] toByteArray() {
        byte[] result = new byte[this.directory[this.segments.length - 1]];
        int segmentOffset = 0;
        int segmentCount = this.segments.length;
        for (int s = 0; s < segmentCount; s++) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            System.arraycopy(this.segments[s], segmentPos, result, segmentOffset, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }
        return result;
    }

    public void write(OutputStream out) throws IOException {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        }
        int segmentOffset = 0;
        int segmentCount = this.segments.length;
        for (int s = 0; s < segmentCount; s++) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            out.write(this.segments[s], segmentPos, nextSegmentOffset - segmentOffset);
            segmentOffset = nextSegmentOffset;
        }
    }

    void write(Buffer buffer) {
        int segmentOffset = 0;
        int segmentCount = this.segments.length;
        for (int s = 0; s < segmentCount; s++) {
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            Segment segment = new Segment(this.segments[s], segmentPos, (segmentPos + nextSegmentOffset) - segmentOffset);
            if (buffer.head == null) {
                segment.prev = segment;
                segment.next = segment;
                buffer.head = segment;
            } else {
                buffer.head.prev.push(segment);
            }
            segmentOffset = nextSegmentOffset;
        }
        buffer.size += (long) segmentOffset;
    }

    public boolean rangeEquals(int offset, ByteString other, int otherOffset, int byteCount) {
        if (offset > size() - byteCount) {
            return false;
        }
        int s = segment(offset);
        while (byteCount > 0) {
            int segmentOffset = s == 0 ? 0 : this.directory[s - 1];
            int stepSize = Math.min(byteCount, (segmentOffset + (this.directory[s] - segmentOffset)) - offset);
            if (!other.rangeEquals(otherOffset, this.segments[s], (offset - segmentOffset) + this.directory[this.segments.length + s], stepSize)) {
                return false;
            }
            offset += stepSize;
            otherOffset += stepSize;
            byteCount -= stepSize;
            s++;
        }
        return true;
    }

    public boolean rangeEquals(int offset, byte[] other, int otherOffset, int byteCount) {
        if (offset > size() - byteCount || otherOffset > other.length - byteCount) {
            return false;
        }
        int s = segment(offset);
        while (byteCount > 0) {
            int segmentOffset = s == 0 ? 0 : this.directory[s - 1];
            int stepSize = Math.min(byteCount, (segmentOffset + (this.directory[s] - segmentOffset)) - offset);
            if (!Util.arrayRangeEquals(this.segments[s], (offset - segmentOffset) + this.directory[this.segments.length + s], other, otherOffset, stepSize)) {
                return false;
            }
            offset += stepSize;
            otherOffset += stepSize;
            byteCount -= stepSize;
            s++;
        }
        return true;
    }

    private ByteString toByteString() {
        return new ByteString(toByteArray());
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        boolean rangeEquals;
        if ((o instanceof ByteString) && ((ByteString) o).size() == size()) {
            rangeEquals = rangeEquals(0, (ByteString) o, 0, size());
        } else {
            rangeEquals = false;
        }
        return rangeEquals;
    }

    public int hashCode() {
        int result = this.hashCode;
        if (result != 0) {
            return result;
        }
        result = 1;
        int segmentOffset = 0;
        int segmentCount = this.segments.length;
        for (int s = 0; s < segmentCount; s++) {
            byte[] segment = this.segments[s];
            int segmentPos = this.directory[segmentCount + s];
            int nextSegmentOffset = this.directory[s];
            for (int i = segmentPos; i < segmentPos + (nextSegmentOffset - segmentOffset); i++) {
                result = (result * 31) + segment[i];
            }
            segmentOffset = nextSegmentOffset;
        }
        this.hashCode = result;
        return result;
    }

    public String toString() {
        return toByteString().toString();
    }

    private Object writeReplace() {
        return toByteString();
    }
}
