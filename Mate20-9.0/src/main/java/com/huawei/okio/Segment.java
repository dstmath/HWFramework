package com.huawei.okio;

import javax.annotation.Nullable;

final class Segment {
    static final int SHARE_MINIMUM = 1024;
    static final int SIZE = 8192;
    final byte[] data;
    int limit;
    Segment next;
    boolean owner;
    int pos;
    Segment prev;
    boolean shared;

    Segment() {
        this.data = new byte[8192];
        this.owner = true;
        this.shared = false;
    }

    Segment(byte[] data2, int pos2, int limit2, boolean shared2, boolean owner2) {
        this.data = data2;
        this.pos = pos2;
        this.limit = limit2;
        this.shared = shared2;
        this.owner = owner2;
    }

    /* access modifiers changed from: package-private */
    public final Segment sharedCopy() {
        this.shared = true;
        Segment segment = new Segment(this.data, this.pos, this.limit, true, false);
        return segment;
    }

    /* access modifiers changed from: package-private */
    public final Segment unsharedCopy() {
        Segment segment = new Segment((byte[]) this.data.clone(), this.pos, this.limit, false, true);
        return segment;
    }

    @Nullable
    public final Segment pop() {
        Segment result = this.next != this ? this.next : null;
        this.prev.next = this.next;
        this.next.prev = this.prev;
        this.next = null;
        this.prev = null;
        return result;
    }

    public final Segment push(Segment segment) {
        segment.prev = this;
        segment.next = this.next;
        this.next.prev = segment;
        this.next = segment;
        return segment;
    }

    public final Segment split(int byteCount) {
        Segment prefix;
        if (byteCount <= 0 || byteCount > this.limit - this.pos) {
            throw new IllegalArgumentException();
        }
        if (byteCount >= 1024) {
            prefix = sharedCopy();
        } else {
            prefix = SegmentPool.take();
            System.arraycopy(this.data, this.pos, prefix.data, 0, byteCount);
        }
        prefix.limit = prefix.pos + byteCount;
        this.pos += byteCount;
        this.prev.push(prefix);
        return prefix;
    }

    public final void compact() {
        if (this.prev == this) {
            throw new IllegalStateException();
        } else if (this.prev.owner) {
            int byteCount = this.limit - this.pos;
            if (byteCount <= (8192 - this.prev.limit) + (this.prev.shared ? 0 : this.prev.pos)) {
                writeTo(this.prev, byteCount);
                pop();
                SegmentPool.recycle(this);
            }
        }
    }

    public final void writeTo(Segment sink, int byteCount) {
        if (sink.owner) {
            if (sink.limit + byteCount > 8192) {
                if (sink.shared) {
                    throw new IllegalArgumentException();
                } else if ((sink.limit + byteCount) - sink.pos <= 8192) {
                    System.arraycopy(sink.data, sink.pos, sink.data, 0, sink.limit - sink.pos);
                    sink.limit -= sink.pos;
                    sink.pos = 0;
                } else {
                    throw new IllegalArgumentException();
                }
            }
            System.arraycopy(this.data, this.pos, sink.data, sink.limit, byteCount);
            sink.limit += byteCount;
            this.pos += byteCount;
            return;
        }
        throw new IllegalArgumentException();
    }
}
