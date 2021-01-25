package ohos.com.sun.org.apache.bcel.internal.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public final class ByteSequence extends DataInputStream {
    private ByteArrayStream byte_stream = ((ByteArrayStream) this.in);

    public ByteSequence(byte[] bArr) {
        super(new ByteArrayStream(bArr));
    }

    public final int getIndex() {
        return this.byte_stream.getPosition();
    }

    /* access modifiers changed from: package-private */
    public final void unreadByte() {
        this.byte_stream.unreadByte();
    }

    /* access modifiers changed from: private */
    public static final class ByteArrayStream extends ByteArrayInputStream {
        ByteArrayStream(byte[] bArr) {
            super(bArr);
        }

        /* access modifiers changed from: package-private */
        public final int getPosition() {
            return this.pos;
        }

        /* access modifiers changed from: package-private */
        public final void unreadByte() {
            if (this.pos > 0) {
                this.pos--;
            }
        }
    }
}
