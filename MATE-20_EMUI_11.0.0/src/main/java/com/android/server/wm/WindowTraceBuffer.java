package com.android.server.wm;

import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Predicate;

/* access modifiers changed from: package-private */
public class WindowTraceBuffer {
    private static final long MAGIC_NUMBER_VALUE = 4990904633914181975L;
    private final Queue<ProtoOutputStream> mBuffer = new ArrayDeque();
    private int mBufferCapacity;
    private final Object mBufferLock = new Object();
    private int mBufferUsedSize;

    WindowTraceBuffer(int bufferCapacity) {
        this.mBufferCapacity = bufferCapacity;
        resetBuffer();
    }

    /* access modifiers changed from: package-private */
    public int getAvailableSpace() {
        return this.mBufferCapacity - this.mBufferUsedSize;
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.mBuffer.size();
    }

    /* access modifiers changed from: package-private */
    public void setCapacity(int capacity) {
        this.mBufferCapacity = capacity;
    }

    /* access modifiers changed from: package-private */
    public void add(ProtoOutputStream proto) {
        int protoLength = proto.getRawSize();
        if (protoLength <= this.mBufferCapacity) {
            synchronized (this.mBufferLock) {
                discardOldest(protoLength);
                this.mBuffer.add(proto);
                this.mBufferUsedSize += protoLength;
                this.mBufferLock.notify();
            }
            return;
        }
        throw new IllegalStateException("Trace object too large for the buffer. Buffer size:" + this.mBufferCapacity + " Object size: " + protoLength);
    }

    /* access modifiers changed from: package-private */
    public boolean contains(byte[] other) {
        return this.mBuffer.stream().anyMatch(new Predicate(other) {
            /* class com.android.server.wm.$$Lambda$WindowTraceBuffer$N2ubPF2l5_1sFrDHIeldAcm7Q30 */
            private final /* synthetic */ byte[] f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return WindowTraceBuffer.lambda$contains$0(this.f$0, (ProtoOutputStream) obj);
            }
        });
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x004e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0053, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0054, code lost:
        r2.addSuppressed(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0057, code lost:
        throw r3;
     */
    public void writeTraceToFile(File traceFile) throws IOException {
        synchronized (this.mBufferLock) {
            traceFile.delete();
            OutputStream os = new FileOutputStream(traceFile);
            traceFile.setReadable(true, false);
            ProtoOutputStream proto = new ProtoOutputStream();
            proto.write(1125281431553L, MAGIC_NUMBER_VALUE);
            os.write(proto.getBytes());
            for (ProtoOutputStream protoOutputStream : this.mBuffer) {
                os.write(protoOutputStream.getBytes());
            }
            os.flush();
            os.close();
        }
    }

    private void discardOldest(int protoLength) {
        long availableSpace = (long) getAvailableSpace();
        while (availableSpace < ((long) protoLength)) {
            ProtoOutputStream item = this.mBuffer.poll();
            if (item != null) {
                this.mBufferUsedSize -= item.getRawSize();
                availableSpace = (long) getAvailableSpace();
            } else {
                throw new IllegalStateException("No element to discard from buffer");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resetBuffer() {
        synchronized (this.mBufferLock) {
            this.mBuffer.clear();
            this.mBufferUsedSize = 0;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public int getBufferSize() {
        return this.mBufferUsedSize;
    }

    /* access modifiers changed from: package-private */
    public String getStatus() {
        String str;
        synchronized (this.mBufferLock) {
            str = "Buffer size: " + this.mBufferCapacity + " bytes\nBuffer usage: " + this.mBufferUsedSize + " bytes\nElements in the buffer: " + this.mBuffer.size();
        }
        return str;
    }
}
