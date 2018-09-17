package sun.security.ssl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import sun.misc.HexDumpEncoder;

final class EngineWriter {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private static final Debug debug = null;
    private boolean outboundClosed;
    private LinkedList<Object> outboundList;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.EngineWriter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.EngineWriter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.EngineWriter.<clinit>():void");
    }

    EngineWriter() {
        this.outboundClosed = false;
        this.outboundList = new LinkedList();
    }

    private HandshakeStatus getOutboundData(ByteBuffer dstBB) {
        ByteBuffer msg = this.outboundList.removeFirst();
        if (-assertionsDisabled || (msg instanceof ByteBuffer)) {
            ByteBuffer bbIn = msg;
            if (!-assertionsDisabled) {
                if ((dstBB.remaining() >= bbIn.remaining() ? 1 : null) == null) {
                    throw new AssertionError();
                }
            }
            dstBB.put(bbIn);
            if (!hasOutboundDataInternal()) {
                return null;
            }
            if (this.outboundList.getFirst() != HandshakeStatus.FINISHED) {
                return HandshakeStatus.NEED_WRAP;
            }
            this.outboundList.removeFirst();
            return HandshakeStatus.FINISHED;
        }
        throw new AssertionError();
    }

    synchronized void writeRecord(EngineOutputRecord outputRecord, MAC writeMAC, CipherBox writeCipher) throws IOException {
        if (this.outboundClosed) {
            throw new IOException("writer side was already closed.");
        }
        outputRecord.write(writeMAC, writeCipher);
        if (outputRecord.isFinishedMsg()) {
            this.outboundList.addLast(HandshakeStatus.FINISHED);
        }
    }

    private void dumpPacket(EngineArgs ea, boolean hsData) {
        try {
            HexDumpEncoder hd = new HexDumpEncoder();
            ByteBuffer bb = ea.netData.duplicate();
            int pos = bb.position();
            bb.position(pos - ea.deltaNet());
            bb.limit(pos);
            System.out.println("[Raw write" + (hsData ? "" : " (bb)") + "]: length = " + bb.remaining());
            hd.encodeBuffer(bb, System.out);
        } catch (IOException e) {
        }
    }

    synchronized HandshakeStatus writeRecord(EngineOutputRecord outputRecord, EngineArgs ea, MAC writeMAC, CipherBox writeCipher) throws IOException {
        if (hasOutboundDataInternal()) {
            HandshakeStatus hss = getOutboundData(ea.netData);
            if (debug != null && Debug.isOn("packet")) {
                dumpPacket(ea, true);
            }
            return hss;
        } else if (this.outboundClosed) {
            throw new IOException("The write side was already closed");
        } else {
            outputRecord.write(ea, writeMAC, writeCipher);
            if (debug != null && Debug.isOn("packet")) {
                dumpPacket(ea, false);
            }
            return null;
        }
    }

    void putOutboundData(ByteBuffer bytes) {
        this.outboundList.addLast(bytes);
    }

    synchronized void putOutboundDataSync(ByteBuffer bytes) throws IOException {
        if (this.outboundClosed) {
            throw new IOException("Write side already closed");
        }
        this.outboundList.addLast(bytes);
    }

    private boolean hasOutboundDataInternal() {
        return this.outboundList.size() != 0;
    }

    synchronized boolean hasOutboundData() {
        return hasOutboundDataInternal();
    }

    synchronized boolean isOutboundDone() {
        boolean z = false;
        synchronized (this) {
            if (this.outboundClosed && !hasOutboundDataInternal()) {
                z = true;
            }
        }
        return z;
    }

    synchronized void closeOutbound() {
        this.outboundClosed = true;
    }
}
