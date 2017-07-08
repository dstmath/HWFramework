package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;

final class EngineOutputRecord extends OutputRecord {
    static final /* synthetic */ boolean -assertionsDisabled = false;
    private SSLEngineImpl engine;
    private boolean finishedMsg;
    private EngineWriter writer;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.ssl.EngineOutputRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.ssl.EngineOutputRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ssl.EngineOutputRecord.<clinit>():void");
    }

    EngineOutputRecord(byte type, SSLEngineImpl engine) {
        super(type, recordSize(type));
        this.finishedMsg = false;
        this.engine = engine;
        this.writer = engine.writer;
    }

    private static int recordSize(byte type) {
        switch (type) {
            case Record.trailerSize /*20*/:
            case (byte) 21:
                return Record.maxAlertRecordSize;
            case ZipConstants.LOCLEN /*22*/:
                return Record.maxRecordSize;
            case SecureRandom.DEFAULT_SDK_TARGET_FOR_CRYPTO_PROVIDER_WORKAROUND /*23*/:
                return 0;
            default:
                throw new RuntimeException("Unknown record type: " + type);
        }
    }

    void setFinishedMsg() {
        this.finishedMsg = true;
    }

    public void flush() throws IOException {
        this.finishedMsg = false;
    }

    boolean isFinishedMsg() {
        return this.finishedMsg;
    }

    private void addMAC(MAC signer, ByteBuffer bb) throws IOException {
        if (signer.MAClen() != 0) {
            byte[] hash = signer.compute(contentType(), bb, false);
            bb.limit(bb.limit() + hash.length);
            bb.put(hash);
        }
    }

    void encrypt(CipherBox box, ByteBuffer bb) {
        box.encrypt(bb);
    }

    void writeBuffer(OutputStream s, byte[] buf, int off, int len, int debugOffset) throws IOException {
        this.writer.putOutboundData((ByteBuffer) ByteBuffer.allocate(len).put(buf, 0, len).flip());
    }

    void write(MAC writeMAC, CipherBox writeCipher) throws IOException {
        switch (contentType()) {
            case Record.trailerSize /*20*/:
            case (byte) 21:
            case ZipConstants.LOCLEN /*22*/:
                if (!isEmpty()) {
                    addMAC(writeMAC);
                    encrypt(writeCipher);
                    write((OutputStream) null, false, (ByteArrayOutputStream) null);
                }
            default:
                throw new RuntimeException("unexpected byte buffers");
        }
    }

    void write(EngineArgs ea, MAC writeMAC, CipherBox writeCipher) throws IOException {
        int i = 0;
        if (!-assertionsDisabled) {
            if (contentType() == 23) {
                i = 1;
            }
            if (i == 0) {
                throw new AssertionError();
            }
        }
        if (writeMAC != MAC.NULL && ea.getAppRemaining() != 0) {
            int length;
            if (this.engine.needToSplitPayload(writeCipher, this.protocolVersion)) {
                write(ea, writeMAC, writeCipher, 1);
                ea.resetLim();
                length = Math.min(ea.getAppRemaining(), (int) Record.maxDataSizeMinusOneByteRecord);
            } else {
                length = Math.min(ea.getAppRemaining(), (int) Record.maxDataSize);
            }
            if (length > 0) {
                write(ea, writeMAC, writeCipher, length);
            }
        }
    }

    void write(EngineArgs ea, MAC writeMAC, CipherBox writeCipher, int length) throws IOException {
        ByteBuffer dstBB = ea.netData;
        int dstPos = dstBB.position();
        int dstLim = dstBB.limit();
        int dstData = dstPos + 5;
        dstBB.position(dstData);
        ea.gather(length);
        dstBB.limit(dstBB.position());
        dstBB.position(dstData);
        addMAC(writeMAC, dstBB);
        dstBB.limit(dstBB.position());
        dstBB.position(dstData);
        encrypt(writeCipher, dstBB);
        if (debug != null && ((Debug.isOn("record") || Debug.isOn("handshake")) && ((debug != null && Debug.isOn("record")) || contentType() == 20))) {
            System.out.println(Thread.currentThread().getName() + ", WRITE: " + this.protocolVersion + " " + InputRecord.contentName(contentType()) + ", length = " + length);
        }
        int packetLength = dstBB.limit() - dstData;
        dstBB.put(dstPos, contentType());
        dstBB.put(dstPos + 1, this.protocolVersion.major);
        dstBB.put(dstPos + 2, this.protocolVersion.minor);
        dstBB.put(dstPos + 3, (byte) (packetLength >> 8));
        dstBB.put(dstPos + 4, (byte) packetLength);
        dstBB.limit(dstLim);
    }
}
