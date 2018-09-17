package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class HSWanMetricsElement extends ANQPElement {
    private final boolean mCapped;
    private final int mDlLoad;
    private final long mDlSpeed;
    private final int mLMD;
    private final LinkStatus mStatus;
    private final boolean mSymmetric;
    private final int mUlLoad;
    private final long mUlSpeed;

    public enum LinkStatus {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.anqp.HSWanMetricsElement.LinkStatus.<clinit>():void");
        }
    }

    public HSWanMetricsElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        boolean z = true;
        super(infoID);
        if (payload.remaining() != 13) {
            throw new ProtocolException("Bad WAN metrics length: " + payload.remaining());
        }
        boolean z2;
        int status = payload.get() & Constants.BYTE_MASK;
        this.mStatus = LinkStatus.values()[status & 3];
        if ((status & 4) != 0) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mSymmetric = z2;
        if ((status & 8) == 0) {
            z = false;
        }
        this.mCapped = z;
        this.mDlSpeed = ((long) payload.getInt()) & Constants.INT_MASK;
        this.mUlSpeed = ((long) payload.getInt()) & Constants.INT_MASK;
        this.mDlLoad = payload.get() & Constants.BYTE_MASK;
        this.mUlLoad = payload.get() & Constants.BYTE_MASK;
        this.mLMD = payload.getShort() & Constants.SHORT_MASK;
    }

    public LinkStatus getStatus() {
        return this.mStatus;
    }

    public boolean isSymmetric() {
        return this.mSymmetric;
    }

    public boolean isCapped() {
        return this.mCapped;
    }

    public long getDlSpeed() {
        return this.mDlSpeed;
    }

    public long getUlSpeed() {
        return this.mUlSpeed;
    }

    public int getDlLoad() {
        return this.mDlLoad;
    }

    public int getUlLoad() {
        return this.mUlLoad;
    }

    public int getLMD() {
        return this.mLMD;
    }

    public String toString() {
        return String.format("HSWanMetrics{mStatus=%s, mSymmetric=%s, mCapped=%s, mDlSpeed=%d, mUlSpeed=%d, mDlLoad=%f, mUlLoad=%f, mLMD=%d}", new Object[]{this.mStatus, Boolean.valueOf(this.mSymmetric), Boolean.valueOf(this.mCapped), Long.valueOf(this.mDlSpeed), Long.valueOf(this.mUlSpeed), Double.valueOf((((double) this.mDlLoad) * 100.0d) / 256.0d), Double.valueOf((((double) this.mUlLoad) * 100.0d) / 256.0d), Integer.valueOf(this.mLMD)});
    }
}
